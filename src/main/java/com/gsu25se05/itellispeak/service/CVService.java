package com.gsu25se05.itellispeak.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.cv.CVAnalysisResponseDTO;
import com.gsu25se05.itellispeak.dto.cv.GetAllCvDTO;
import com.gsu25se05.itellispeak.entity.*;
import com.gsu25se05.itellispeak.exception.ErrorCode;
import com.gsu25se05.itellispeak.exception.auth.AuthAppException;
import com.gsu25se05.itellispeak.exception.auth.NotLoginException;
import com.gsu25se05.itellispeak.repository.*;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import com.gsu25se05.itellispeak.utils.CloudinaryUtils;
import com.gsu25se05.itellispeak.utils.FileUtils;
import com.gsu25se05.itellispeak.utils.PdfToImageConverter;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CVService {
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private final CVEvaluateRepository cvEvaluateRepository;
    private final CVFeedbackCategoryRepository categoryRepository;
    private final CVFeedbackTipRepository tipRepository;
    private final CVExtractedInfoRepository cvExtractedInfoRepository;
    private final MemberCVRepository memberCVRepository;
    private final AccountUtils accountUtils;
    private final CloudinaryUtils cloudinaryUtils;
    private final UserRepository userRepository;
    private final UserUsageRepository userUsageRepository;

    public CVService(
            @Value("${genai.api.key}") String apiKey,
            CVEvaluateRepository cvEvaluateRepository,
            CVFeedbackCategoryRepository categoryRepository,
            CVFeedbackTipRepository tipRepository,
            CVExtractedInfoRepository cvExtractedInfoRepository,
            MemberCVRepository memberCVRepository,
            AccountUtils accountUtils,
            CloudinaryUtils cloudinaryUtils,
            UserRepository userRepository, UserUsageRepository userUsageRepository) {
        this.webClient = WebClient.builder()
                .baseUrl(API_URL + "?key=" + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.cvEvaluateRepository = cvEvaluateRepository;
        this.categoryRepository = categoryRepository;
        this.tipRepository = tipRepository;
        this.cvExtractedInfoRepository = cvExtractedInfoRepository;
        this.memberCVRepository = memberCVRepository;
        this.accountUtils = accountUtils;
        this.cloudinaryUtils = cloudinaryUtils;
        this.userRepository = userRepository;
        this.userUsageRepository = userUsageRepository;
    }

    private String sanitizeText(String text) {
        // Loại bỏ ký tự control ASCII không in được (mã < 32 trừ newline/tab)
        return text.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "")
                .replaceAll("�", "") // Loại ký tự lỗi font
                .replaceAll("\\p{C}", ""); // Ký tự "invisible" (Unicode control)
    }

    public Response<CVAnalysisResponseDTO> analyzeAndSaveFromFile(String cvTitle, MultipartFile file) throws Exception {

        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            throw new NotLoginException("Please log in to continue");
        }

        if (currentUser.getUserUsage().getCvAnalyzeUsed() >= currentUser.getAPackage().getCvAnalyzeCount()) {
            throw new AuthAppException(ErrorCode.OUT_OF_CV_ANALYZE_COUNT);
        }

        String rawText = FileUtils.extractTextFromCV(file);
        String cleanText = sanitizeText(rawText);

        //Save image to cloudinary
        String baseName = file.getOriginalFilename()
                .replaceAll(".pdf", "")
                .replaceAll("\\s+", "_");

        // Convert PDF -> MultipartFile image(s) in memory
        List<MultipartFile> imageFiles = PdfToImageConverter.convertPdfToMultipartImages(file.getInputStream(), baseName);

        // Upload lên Cloudinary
        StringBuilder imageUrls = new StringBuilder();

        for (MultipartFile img : imageFiles) {
            String url = cloudinaryUtils.uploadImage(img);
            if (!imageUrls.isEmpty()) {
                imageUrls.append(";");
            }
            imageUrls.append(url);
        }

        return analyzeAndSaveEvaluation(cleanText, imageUrls.toString(), cvTitle, currentUser);
    }

    @Transactional
    public Response<CVAnalysisResponseDTO> analyzeAndSaveEvaluation(String cvText, String imageURLs, String cvTitle, User user) throws Exception {
        String prompt = preparePrompt(cvText);
        String response = callGemini(prompt);
        String cleaned = cleanJson(response);

        JsonNode root;
        try {
            root = objectMapper.readTree(cleaned);
        } catch (Exception ex) {
            // AI không trả JSON hợp lệ
            return new Response<>(502, "Invalid AI response format", null);
        }

        // 1) Bắt buộc có trường supported (theo prompt IT-only)
        boolean supported = root.path("supported").asBoolean(false);
        if (!supported) {
            String detectedDomain = root.path("detectedDomain").asText("unknown");
            String msg = root.path("message").asText("This service only supports IT resumes.");
            return new Response<>(422, String.format("%s Detected domain: %s.", msg, detectedDomain), null);
        }

        // 2) Kiểm tra đủ cấu trúc trước khi dùng
        JsonNode feedbackNode = root.path("feedback");
        if (feedbackNode.isMissingNode()) {
            return new Response<>(502, "AI response missing 'feedback' object", null);
        }

        JsonNode infoNode = root.path("extractedInfo");
        if (infoNode.isMissingNode()) {
            return new Response<>(502, "AI response missing 'extractedInfo' object", null);
        }

        // Deactivate old CVs
        memberCVRepository.deactivateOldCVsByUser(user);

        // Create active CV
        MemberCV memberCV = new MemberCV();
        memberCV.setLinkToCv(imageURLs);
        memberCV.setUser(user);
        memberCV.setCvTitle(cvTitle);
        memberCV.setDeleted(false);
        memberCV.setCreateAt(LocalDateTime.now());
        memberCV.setUpdateAt(LocalDateTime.now());
        memberCV.setActive(true);
        memberCVRepository.save(memberCV);

        // Save CVEvaluate
        int overallScore = feedbackNode.path("overallScore").asInt(0);
        CVEvaluate cvEvaluate = new CVEvaluate();
        cvEvaluate.setMemberCV(memberCV);
        cvEvaluate.setOverallScore(overallScore);
        cvEvaluate.setCreateAt(LocalDateTime.now());
        cvEvaluate.setUpdateAt(LocalDateTime.now());
        cvEvaluate.setDeleted(false);
        cvEvaluateRepository.save(cvEvaluate);

        // Save categories & tips (an toàn)
        List<String> categories = List.of("ATS", "toneAndStyle", "content", "structure", "skills");
        for (String cat : categories) {
            JsonNode catNode = feedbackNode.path(cat);
            if (catNode.isMissingNode()) continue;

            CVFeedbackCategory category = new CVFeedbackCategory();
            category.setCvEvaluate(cvEvaluate);
            category.setCategoryName(cat);
            category.setScore(catNode.path("score").asInt(0));
            categoryRepository.save(category);

            JsonNode tipsNode = catNode.path("tips");
            if (tipsNode.isArray()) {
                for (JsonNode tipNode : tipsNode) {
                    CVFeedbackTip tip = new CVFeedbackTip();
                    tip.setFeedbackCategory(category);

                    String rawType = tipNode.path("type").asText("");
                    TipType tipType = TipType.fromString(rawType);
                    if (tipType == null) {
                        System.err.println("❌ Invalid tip type from AI: " + rawType);
                        continue;
                    }

                    tip.setType(tipType);
                    tip.setTip(tipNode.path("tip").asText(""));
                    tip.setExplanation(tipNode.path("explanation").asText(null));
                    tipRepository.save(tip);
                }
            }
        }

        // Save extracted info
        CVExtractedInfo extracted = new CVExtractedInfo();
        extracted.setMemberCV(memberCV);
        extracted.setFullName(infoNode.path("fullName").asText(""));
        extracted.setEmail(infoNode.path("email").asText(""));
        extracted.setPhone(infoNode.path("phone").asText(""));
        extracted.setTotalYearsExperience(infoNode.path("totalYearsExperience").asInt(0));
        extracted.setEducationLevel(infoNode.path("educationLevel").asText(""));
        extracted.setUniversity(infoNode.path("university").asText(""));
        extracted.setSkills(objectMapper.writeValueAsString(infoNode.path("skills").isMissingNode() ? List.of() : infoNode.path("skills")));
        extracted.setCertifications(infoNode.path("certifications").asText(""));
        extracted.setCareerGoals(infoNode.path("careerGoals").asText(""));
        extracted.setWorkExperience(infoNode.path("workExperience").asText(""));
        extracted.setCreateAt(LocalDateTime.now());
        extracted.setUpdateAt(LocalDateTime.now());

        CVAnalysisResponseDTO dto = new CVAnalysisResponseDTO(
                getCV(cvEvaluate.getId()).getData(),
                cvExtractedInfoRepository.save(extracted)
        );

        user.getUserUsage().setCvAnalyzeUsed(user.getUserUsage().getCvAnalyzeUsed() + 1);
        userRepository.save(user);
        userUsageRepository.save(user.getUserUsage());

        return new Response<>(200, "Analysis successful", dto);
    }


    private String preparePrompt(String cvText) {
        return String.format("""
            You are an expert in ATS (Applicant Tracking System) and CV/Resume analysis for the **IT/technology domain only**.

            ✅ Your task:
            - Detect whether the CV belongs to the IT domain (e.g., Software Engineer, Backend/Frontend, Full-stack, Mobile, DevOps/SRE, Cloud, Data Engineer/Scientist/Analyst, ML/AI, QA/QC/Automation, Security, System/Network Admin, Product/BA/PO in tech, Tech Lead/Architect).
            - If and only if the CV is IT-related, evaluate and score it and provide actionable tips.

            ❌ If the CV is **not IT-related**, DO NOT evaluate. Instead, return a minimal JSON indicating that the domain is unsupported.

            🔒 Output format rules:
            - Return **JSON only** (no extra text).
            - If unsupported (non-IT), return:
              {
                "supported": false,
                "detectedDomain": "<short domain>",
                "message": "This service only supports IT resumes."
              }

            - If supported (IT), return exactly this structure:
              {
                "supported": true,
                "feedback": {
                  "overallScore": <0-100>,
                  "ATS": {
                    "score": <0-100>,
                    "tips": [
                      { "type": "<good|improve|warning|dangerous|neutral|note>", "tip": "<short tip>", "explanation": "<short reason>" }
                    ]
                  },
                  "toneAndStyle": {
                    "score": <0-100>,
                    "tips": [ ... ]
                  },
                  "content": {
                    "score": <0-100>,
                    "tips": [ ... ]
                  },
                  "structure": {
                    "score": <0-100>,
                    "tips": [ ... ]
                  },
                  "skills": {
                    "score": <0-100>,
                    "tips": [ ... ]
                  }
                },
                "extractedInfo": {
                  "fullName": "<string>",
                  "email": "<string>",
                  "phone": "<string>",
                  "totalYearsExperience": <int>,
                  "educationLevel": "<string>",
                  "university": "<string>",
                  "skills": ["<skill1>", "<skill2>", "..."],
                  "certifications": "<string>",
                  "careerGoals": "<string>",
                  "workExperience": "<string or brief bullets>"
                }
              }

            Analysis guidance (when supported = true):
            - Be detailed and candid; low-quality CVs should receive low scores with clear reasons.
            - Use concise, actionable tips focused on IT hiring best practices and ATS passability.

            Here is the CV content to analyze:
            %s
            """, cvText);
    }



    private String callGemini(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", prompt))
                    ))
            );

            String response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode json = objectMapper.readTree(response);
            return json.at("/candidates/0/content/parts/0/text").asText("No response from AI.");
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred while calling Gemini API.";
        }
    }

    private String cleanJson(String text) {
        return text.replaceAll("(?i)```json", "").replaceAll("(?i)```", "").trim();
    }

    public Response<CVEvaluate> getCV(Long id) {
        CVEvaluate cvEvaluate = cvEvaluateRepository.findById(id).orElse(null);
        return new Response<>(200, "Success", cvEvaluate);
    }

    public Response<List<GetAllCvDTO>> getAllCvDTOsByUser() {

        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) return new Response<>(401, "Please log in to continue", null);

        List<MemberCV> cvs = memberCVRepository.findByUserUserIdAndIsDeletedFalse(currentUser.getUserId());

        List<GetAllCvDTO> dtos = cvs.stream().map(cv -> {
            // Lấy CVEvaluate mới nhất nếu có
            Optional<CVEvaluate> latestEvaluation = cv.getCvEvaluations().stream()
                    .filter(e -> !e.isDeleted())
                    .max(Comparator.comparing(CVEvaluate::getCreateAt));

            String overallScore = latestEvaluation.map(e -> e.getOverallScore().toString()).orElse("N/A");

            return new GetAllCvDTO(cv.getMemberCvId(), overallScore, cv.getLinkToCv(), cv.getCvTitle(), cv.getCreateAt(), cv.isActive());
        }).sorted(Comparator.comparing(GetAllCvDTO::getCvTitle, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
        return new Response<>(200, "Thành công", dtos);
    }
}
