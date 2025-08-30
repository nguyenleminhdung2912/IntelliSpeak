package com.gsu25se05.itellispeak.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.cv.*;
import com.gsu25se05.itellispeak.dto.interview_session.InterviewSessionDTO;
import com.gsu25se05.itellispeak.entity.*;
import com.gsu25se05.itellispeak.exception.ErrorCode;
import com.gsu25se05.itellispeak.exception.auth.AuthAppException;
import com.gsu25se05.itellispeak.exception.auth.NotLoginException;
import com.gsu25se05.itellispeak.repository.*;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import com.gsu25se05.itellispeak.utils.CloudinaryUtils;
import com.gsu25se05.itellispeak.utils.FileUtils;
import com.gsu25se05.itellispeak.utils.PdfToImageConverter;
import com.gsu25se05.itellispeak.utils.mapper.InterviewSessionMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;

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
    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewSessionMapper interviewSessionMapper;
    private final CompanyRepository companyRepository;
    private final CVSubmissionRepository cVSubmissionRepository;
    private final CompanyJDRepository companyJDRepository;

    public CVService(
            @Value("${genai.api.key}") String apiKey,
            CVEvaluateRepository cvEvaluateRepository,
            CVFeedbackCategoryRepository categoryRepository,
            CVFeedbackTipRepository tipRepository,
            CVExtractedInfoRepository cvExtractedInfoRepository,
            MemberCVRepository memberCVRepository,
            AccountUtils accountUtils,
            CloudinaryUtils cloudinaryUtils,
            UserRepository userRepository, UserUsageRepository userUsageRepository,
            InterviewSessionRepository interviewSessionRepository,
            InterviewSessionMapper interviewSessionMapper,
            CompanyRepository companyRepository,
            CVSubmissionRepository cVSubmissionRepository, CompanyJDRepository companyJDRepository) {
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
        this.interviewSessionRepository = interviewSessionRepository;
        this.interviewSessionMapper = interviewSessionMapper;
        this.companyRepository = companyRepository;
        this.cVSubmissionRepository = cVSubmissionRepository;
        this.companyJDRepository = companyJDRepository;
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

    private static String normalizeDomain(String d) {
        if (d == null) return null;
        d = d.toLowerCase();
        // Ánh xạ nhanh alias ↔ domain chuẩn
        if (d.contains("backend")) return "backend";
        if (d.contains("front")) return "frontend";
        if (d.contains("full")) return "fullstack";
        if (d.contains("devops") || d.contains("sre")) return "devops";
        if (d.contains("cloud")) return "cloud";
        if (d.contains("data") && !d.contains("database")) return "data";
        if (d.contains("ml") || d.contains("machine learning") || d.contains("ai")) return "ml";
        if (d.contains("qa") || d.contains("test") || d.contains("automation")) return "qa";
        if (d.contains("security") || d.contains("sec")) return "security";
        if (d.contains("mobile") || d.contains("android") || d.contains("ios")) return "mobile";
        if (d.contains("architect")) return "architect";
        if (d.contains("product") || d.contains("po")) return "product";
        if (d.contains("ba") || d.contains("business analyst")) return "ba";
        return d;
    }

    private List<InterviewSessionDTO> recommendSessions(String detectedDomain, List<String> skills, int limit) {
        // 1) Chuẩn hoá domain theo Topic thực tế trong DB (ví dụ "backend system" | "user interface" | "fullstack")
        final String domain = normalizeDomain(detectedDomain);
        final String domainLower = (domain == null) ? "" : domain.toLowerCase();

        // 2) Gom keyword từ skills (+ tách domain thành từ đơn), hạ chữ, distinct
        List<String> kw = new ArrayList<>();
        if (skills != null) {
            kw.addAll(
                    skills.stream()
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .map(String::toLowerCase)
                            .filter(s -> !s.isEmpty())
                            .distinct()
                            .toList()
            );
        }
        if (!domainLower.isBlank()) {
            Arrays.stream(domainLower.split("\\s+"))
                    .filter(s -> s.length() >= 3) // bỏ từ quá ngắn
                    .forEach(kw::add);
        }
        final List<String> keywords = kw.stream().distinct().toList();     // effectively final
        final Set<String> kwSet = new HashSet<>(keywords);                  // for scoring

        // 3) Specification: isDeleted=false AND (match theo topic/title/desc OR tags)
        Specification<InterviewSession> spec = (root, query, cb) -> {
            List<Predicate> ands = new ArrayList<>();
            ands.add(cb.isFalse(root.get("isDeleted")));

            // join topic luôn dùng
            Join<?, ?> tp = root.join("topic", JoinType.LEFT);

            List<Predicate> ors = new ArrayList<>();

            // 3.1 match theo TOPIC + DOMAIN + TITLE/DESCRIPTION chứa domain
            if (!domainLower.isBlank()) {
                String likeDomain = "%" + domainLower + "%";
                ors.add(cb.like(cb.lower(tp.get("title")), likeDomain));            // topic.title LIKE %domain%
                ors.add(cb.like(cb.lower(root.get("title")), likeDomain));          // title LIKE %domain%
                ors.add(cb.like(cb.lower(root.get("description")), likeDomain));    // description LIKE %domain%
            }

            // 3.2 match theo TITLE/DESCRIPTION với nhiều KEYWORDS
            if (!keywords.isEmpty()) {
                List<Predicate> kwOrs = new ArrayList<>();
                for (String k : keywords) {
                    String likeK = "%" + k + "%";
                    kwOrs.add(cb.like(cb.lower(root.get("title")), likeK));
                    kwOrs.add(cb.like(cb.lower(root.get("description")), likeK));
                }
                ors.add(cb.or(kwOrs.toArray(new Predicate[0])));

                // 3.3 match theo TAGS
                Join<?, ?> tg = root.join("tags", JoinType.LEFT);
                CriteriaBuilder.In<String> in = cb.in(cb.lower(tg.get("title")));
                for (String k : keywords) in.value(k);
                ors.add(in);
            }

            if (!ors.isEmpty()) {
                ands.add(cb.or(ors.toArray(new Predicate[0])));
            }
            return cb.and(ands.toArray(new Predicate[0]));
        };

        // 4) Lấy rộng hơn rồi chấm điểm để ưu tiên KHỚP TITLE
        var page = org.springframework.data.domain.PageRequest.of(0, Math.max(15, Math.max(5, limit)));
        List<InterviewSession> pool = interviewSessionRepository.findAll(spec, page).getContent();

        Comparator<InterviewSession> byScoreDesc = Comparator
                .comparingInt((InterviewSession s) -> {
                    int score = 0;
                    String title = Optional.ofNullable(s.getTitle()).orElse("").toLowerCase();
                    String desc = Optional.ofNullable(s.getDescription()).orElse("").toLowerCase();
                    String topic = Optional.ofNullable(s.getTopic()).map(Topic::getTitle).orElse("").toLowerCase();

                    // domain ưu tiên: title > topic > description
                    if (!domainLower.isBlank() && title.contains(domainLower)) score += 6;
                    if (!domainLower.isBlank() && topic.contains(domainLower)) score += 4;
                    if (!domainLower.isBlank() && desc.contains(domainLower)) score += 3;

                    // mỗi keyword: title +2, description +1
                    for (String k : kwSet) {
                        if (k.isBlank()) continue;
                        if (title.contains(k)) score += 2;
                        if (desc.contains(k)) score += 1;
                    }

                    // cộng thêm nếu tag trùng keyword
                    if (s.getTags() != null) {
                        for (Tag t : s.getTags()) {
                            String tt = Optional.ofNullable(t.getTitle()).orElse("").toLowerCase();
                            if (kwSet.contains(tt)) score += 2;
                        }
                    }
                    return score;
                })
                .reversed()
                .thenComparing(InterviewSession::getUpdateAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(InterviewSession::getCreateAt, Comparator.nullsLast(Comparator.reverseOrder()));

        List<InterviewSession> ranked = pool.stream()
                .sorted(byScoreDesc)
                .limit(limit)
                .toList();

        return ranked.stream()
                .map(interviewSessionMapper::toDTO)
                .toList();
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }


    private void updateProfileFromExtracted(User user, CVExtractedInfo extracted) {
        if (extracted == null) return;

        if (isNotBlank(extracted.getFullName())) {
            String[] parts = extracted.getFullName().trim().split("\\s+");
            if (parts.length > 1) {
                String lastName = parts[parts.length - 1];
                String firstName = String.join(" ", Arrays.copyOf(parts, parts.length - 1));
                if (isBlank(user.getFirstName())) user.setFirstName(firstName);
                if (isBlank(user.getLastName())) user.setLastName(lastName);
            } else {
                if (isBlank(user.getLastName())) user.setLastName(parts[0]);
            }
        }

        // Phone
        if (isNotBlank(extracted.getPhone()) && isBlank(user.getPhone())) {
            user.setPhone(extracted.getPhone());
        }

        // Website, LinkedIn, GitHub
        if (isNotBlank(extracted.getUniversity()) && isBlank(user.getBio())) {
            user.setBio("Studied at " + extracted.getUniversity());
        }

        if (isNotBlank(extracted.getCareerGoals()) && isBlank(user.getBio())) {
            user.setBio(extracted.getCareerGoals());
        }
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

        JsonNode infoNode = root.path("extractedInfo");
        JsonNode feedbackNode = root.path("feedback");
        // LẤY domain & skills từ JSON
        String detectedDomain = root.path("detectedDomain").asText(null);

        List<String> skills = new ArrayList<>();
        if (infoNode.has("skills") && infoNode.get("skills").isArray()) {
            infoNode.get("skills").forEach(n -> {
                if (n.isTextual()) skills.add(n.asText());
            });
        }

        // 2) Kiểm tra đủ cấu trúc trước khi dùng
        if (feedbackNode.isMissingNode()) {
            return new Response<>(502, "AI response missing 'feedback' object", null);
        }

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
        cvExtractedInfoRepository.save(extracted);

        // GỢI Ý SESSION (vd: 8 session)
        List<InterviewSessionDTO> recommended = recommendSessions(detectedDomain, skills, 8);

        CVAnalysisResponseDTO dto = new CVAnalysisResponseDTO(
                cvEvaluate,
                extracted,
                recommended
        );

        if (!memberCV.isProfileSynced()) {
            updateProfileFromExtracted(user, extracted);
            userRepository.save(user);
            memberCV.setProfileSynced(true);
            memberCVRepository.save(memberCV);
        }
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
                                "detectedDomain": "<short domain like: backend | frontend | fullstack | mobile | devops | cloud | data | ml | ai | qa | security | sysadmin | network | product | ba | po | architect | sre | ...>",
                                    "suggestedRoles": ["<short role1>", "<short role2>", "..."],
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

    public Response<CVEvaluateResponseDTO> getCV(Long id) {
        CVEvaluate cvEvaluate = cvEvaluateRepository.findById(id).orElse(null);
        if (cvEvaluate == null) {
            return new Response<>(404, "CV evaluation not found", null);
        }

        // Lấy extracted info mới nhất
        MemberCV memberCV = cvEvaluate.getMemberCV();
        CVExtractedInfo extracted = cvExtractedInfoRepository
                .findFirstByMemberCVOrderByCreateAtDesc(memberCV);

        List<InterviewSessionDTO> recommended = List.of();
        if (extracted != null) {
            // Không có detectedDomain thì để null hoặc đoán từ CV title/careerGoals
            String detectedDomain = null;

            if (extracted.getCareerGoals() != null && !extracted.getCareerGoals().isBlank()) {
                detectedDomain = extracted.getCareerGoals();
            } else if (memberCV.getCvTitle() != null && !memberCV.getCvTitle().isBlank()) {
                detectedDomain = memberCV.getCvTitle();
            }

            // Parse skills JSON đã lưu
            List<String> skills = parseSkillsJson(extracted.getSkills());

            recommended = recommendSessions(detectedDomain, skills, 8);
        }


        CVEvaluateResponseDTO dto = new CVEvaluateResponseDTO(cvEvaluate, recommended);

        return new Response<>(200, "Success", dto);
    }

    private List<String> parseSkillsJson(String json) {
        try {
            if (json == null || json.isBlank()) return List.of();
            List<String> arr = objectMapper.readValue(
                    json,
                    new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {
                    }
            );
            return arr.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim).map(String::toLowerCase)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }


    public Response<List<GetAllCvDTO>> getAllCvDTOsByUser() {

        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) return new Response<>(401, "Please log in to continue", null);

        List<MemberCV> cvs = memberCVRepository.findByUserUserIdAndIsDeletedFalseOrderByCreateAtDesc(currentUser.getUserId());

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

    @Transactional
    public String submitCvToCompany(Long companyId, Long companyJDId) {
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) throw new AuthAppException(ErrorCode.NOT_LOGIN);

        // 1. Get active CV
        MemberCV memberCV = memberCVRepository.findByUserAndIsDeletedFalseAndIsActiveTrue(currentUser)
                .orElseThrow(() -> new AuthAppException(ErrorCode.NO_CV_UPLOADED));

        // 2. Get CompanyJD
        CompanyJD companyJD = companyJDRepository.findById(companyJDId)
                .orElseThrow(() -> new AuthAppException(ErrorCode.JD_NOT_FOUND));

        // 3. Get Company and validate
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AuthAppException(ErrorCode.COMPANY_NOT_FOUND));

        // 4. Security/Integrity check: Ensure the JD belongs to the specified company
        if (!companyJD.getCompany().getCompanyId().equals(company.getCompanyId())) {
            throw new AuthAppException(ErrorCode.JD_NOT_FROM_COMPANY);
        }

        // 5. Check for existing submission for this CV to this specific JD
        Optional<CVSubmission> existingSubmissionOpt = cVSubmissionRepository.findByCompanyJDAndMemberCV(companyJD, memberCV);

        if (existingSubmissionOpt.isPresent()) {
            CVSubmission existingSubmission = existingSubmissionOpt.get();
            Boolean isViewed = existingSubmission.getIsViewed();
            if (isViewed == null) { // Pending
                throw new AuthAppException(ErrorCode.CV_IS_PENDING);
            } else if (Boolean.TRUE.equals(isViewed)) { // Accepted
                throw new AuthAppException(ErrorCode.CV_IS_ALREADY_ACCEPTED);
            }
            // If isViewed is false (rejected), allow resubmission by updating the existing record.
            existingSubmission.setSubmittedAt(LocalDateTime.now());
            existingSubmission.setIsViewed(null); // Reset status to pending
            cVSubmissionRepository.save(existingSubmission);
            return "CV " + memberCV.getCvTitle() + " successfully re-submitted for job: " + companyJD.getJobTitle();
        }

        // 6. No existing submission, create a new one, setting both company and companyJD
        CVSubmission submission = CVSubmission.builder()
                .company(company) // As requested
                .companyJD(companyJD) // The new link
                .memberCV(memberCV)
                .submittedAt(LocalDateTime.now())
                .isViewed(null)
                .build();
        cVSubmissionRepository.save(submission);

        return "CV " + memberCV.getCvTitle() + " successfully submitted for job: " + companyJD.getJobTitle();
    }

    public List<CandidateSubmittedCvDTO> getSubmittedCvsForCurrentUser() {
        // Assume you have a method to get the current user
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) throw new AuthAppException(ErrorCode.NOT_LOGIN);

        List<CVSubmission> submissions = cVSubmissionRepository.findByMemberCV_UserOrderBySubmittedAtDesc(currentUser);

        return submissions.stream().map(sub -> {
            CandidateSubmittedCvDTO dto = new CandidateSubmittedCvDTO();
            dto.setCvSubmissionId(sub.getId());
            dto.setMemberCvTitle(sub.getMemberCV().getCvTitle());
            dto.setMemberCvLinkToCv(sub.getMemberCV().getLinkToCv());
            dto.setCompanyId(sub.getCompany().getCompanyId());
            dto.setCompanyName(sub.getCompany().getName());
            dto.setIsViewed(sub.getIsViewed());
            dto.setCompanyLogoUrl(sub.getCompany().getLogoUrl());
            if (sub.getCompanyJD() != null) {
                dto.setJobTitle(sub.getCompanyJD().getJobTitle());
            }
            dto.setSubmittedAt(sub.getSubmittedAt());
            return dto;
        }).toList();
    }

    public List<HRViewSubmittedCvDTO> getSubmittedCvsForCompany() {
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) throw new AuthAppException(ErrorCode.NOT_LOGIN);

        if (!currentUser.getRole().equals(User.Role.HR)) {
            throw new AuthAppException(ErrorCode.ACCOUNT_NOT_HR);
        }

        if (currentUser.getHr() == null || currentUser.getHr().getCompany() == null) {
            throw new AuthAppException(ErrorCode.HR_NOT_FOUND);
        }

        Company company = currentUser.getHr().getCompany();
        List<CVSubmission> submissions = cVSubmissionRepository.findByCompanyOrderBySubmittedAtDesc(company);
        return submissions.stream().map(sub -> {
            HRViewSubmittedCvDTO dto = new HRViewSubmittedCvDTO();
            dto.setCvSubmissionId(sub.getId());
            dto.setUserId(sub.getMemberCV().getUser().getUserId());
            dto.setUserEmail(sub.getMemberCV().getUser().getEmail());
            dto.setUserPhone(sub.getMemberCV().getUser().getPhone());
            dto.setMemberCvTitle(sub.getMemberCV().getCvTitle());
            dto.setMemberCvLinkToCv(sub.getMemberCV().getLinkToCv());
            if (sub.getCompanyJD() != null) {
                dto.setJobTitle(sub.getCompanyJD().getJobTitle());
            }
            dto.setIsViewed(sub.getIsViewed());
            dto.setSubmittedAt(sub.getSubmittedAt());
            return dto;
        }).toList();
    }

    private CVSubmission getAndVerifySubmissionForHr(Long submissionId) {
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            throw new AuthAppException(ErrorCode.NOT_LOGIN);
        }
        if (!currentUser.getRole().equals(User.Role.HR)) {
            throw new AuthAppException(ErrorCode.ACCOUNT_NOT_HR);
        }
        Company hrCompany = currentUser.getHr().getCompany();
        if (hrCompany == null) {
            throw new AuthAppException(ErrorCode.HR_NOT_FOUND);
        }

        CVSubmission submission = cVSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new AuthAppException(ErrorCode.CV_SUBMISSION_NOT_FOUND));

        // Security check: ensure the HR belongs to the company the CV was submitted to.
        if (!submission.getCompany().getCompanyId().equals(hrCompany.getCompanyId())) {
            throw new AuthAppException(ErrorCode.ACTION_FORBIDDEN);
        }

        return submission;
    }

    @Transactional
    public void approveCvSubmission(Long submissionId) {
        CVSubmission submission = getAndVerifySubmissionForHr(submissionId);
        submission.setIsViewed(true);
        cVSubmissionRepository.save(submission);
    }

    @Transactional
    public void rejectCvSubmission(Long submissionId) {
        CVSubmission submission = getAndVerifySubmissionForHr(submissionId);
        submission.setIsViewed(false);
        cVSubmissionRepository.save(submission);
    }

    @Transactional
    public void setActiveCv(Long cvId) {
        // 1. Get current user
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            throw new AuthAppException(ErrorCode.NOT_LOGIN);
        }

        // 2. Find the target MemberCV
        MemberCV cvToActivate = memberCVRepository.findById(cvId)
                .orElseThrow(() -> new AuthAppException(ErrorCode.CV_NOT_FOUND));

        // 3. Verify ownership
        if (!cvToActivate.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new AuthAppException(ErrorCode.ACTION_FORBIDDEN);
        }

        // 4. Deactivate all other CVs for the user
        memberCVRepository.deactivateOldCVsByUser(currentUser);

        // 5. Activate the target CV and save
        cvToActivate.setActive(true);
        cvToActivate.setUpdateAt(LocalDateTime.now());
        memberCVRepository.save(cvToActivate);
    }
}
