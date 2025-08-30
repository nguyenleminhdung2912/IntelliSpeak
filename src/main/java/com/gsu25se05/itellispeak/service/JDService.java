package com.gsu25se05.itellispeak.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.jd.CvJdMatchResultDTO;
import com.gsu25se05.itellispeak.dto.jd.GetAllJdDTO;
import com.gsu25se05.itellispeak.entity.*;
import com.gsu25se05.itellispeak.exception.ErrorCode;
import com.gsu25se05.itellispeak.exception.auth.AuthAppException;
import com.gsu25se05.itellispeak.exception.auth.NotFoundException;
import com.gsu25se05.itellispeak.exception.auth.NotLoginException;
import com.gsu25se05.itellispeak.repository.*;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import com.gsu25se05.itellispeak.utils.CloudinaryUtils;
import com.gsu25se05.itellispeak.utils.FileUtils;
import com.gsu25se05.itellispeak.utils.PdfToImageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class JDService {

    private final JDRepository jdRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private final AccountUtils accountUtils;
    private final UserRepository userRepository;
    private final JDEvaluateRepository jdEvaluateRepository;
    private final CloudinaryUtils cloudinaryUtils;
    private final UserUsageRepository userUsageRepository;
    private final MemberCVRepository memberCVRepository;
    private final CVExtractedInfoRepository cvExtractedInfoRepository;

    public JDService(JDRepository jdRepository, @Value("${genai.api.key}") String apiKey, AccountUtils accountUtils, UserRepository userRepository, JDEvaluateRepository jdEvaluateRepository, CloudinaryUtils cloudinaryUtils, UserUsageRepository userUsageRepository, MemberCVRepository memberCVRepository, CVExtractedInfoRepository cvExtractedInfoRepository) {
        this.jdRepository = jdRepository;
        this.webClient = WebClient.builder()
                .baseUrl(API_URL + "?key=" + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.accountUtils = accountUtils;
        this.userRepository = userRepository;
        this.jdEvaluateRepository = jdEvaluateRepository;
        this.cloudinaryUtils = cloudinaryUtils;
        this.userUsageRepository = userUsageRepository;
        this.memberCVRepository = memberCVRepository;
        this.cvExtractedInfoRepository = cvExtractedInfoRepository;
    }

    private List<String> parseCvSkillsJson(String json) {
        try {
            if (json == null || json.isBlank()) return List.of();
            List<String> arr = objectMapper.readValue(
                    json,
                    new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}
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

    private static List<String> splitSkillsString(String raw) {
        if (raw == null) return List.of();
        String s = raw.toLowerCase();
        s = s.replaceAll("[/|;]", ",")
                .replaceAll("\\band\\b|\\bor\\b|\\+", ",");
        String[] parts = s.split("[,\\n]");
        return java.util.Arrays.stream(parts)
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .distinct()
                .toList();
    }

    private String jsonOnly(String s) {
        return s == null ? "" : s.replaceAll("(?i)```json", "")
                .replaceAll("(?i)```", "")
                .trim();
    }

    private String buildAiMatchPrompt(CVExtractedInfo cv, JD jd, List<String> cvSkills) {
        // Ghép JSON gọn cho AI (ít rủi ro prompt injection)
        String cvJson = """
    {
      "fullName": %s,
      "totalYearsExperience": %d,
      "educationLevel": %s,
      "skills": %s,
      "certifications": %s,
      "careerGoals": %s,
      "workExperience": %s
    }
    """.formatted(
                toJson(cv.getFullName()),
                cv.getTotalYearsExperience() == null ? 0 : cv.getTotalYearsExperience(),
                toJson(cv.getEducationLevel()),
                toJsonArray(cvSkills),
                toJson(cv.getCertifications()),
                toJson(cv.getCareerGoals()),
                toJson(cv.getWorkExperience())
        );

        List<String> must = splitSkillsString(jd.getMustHaveSkills());
        List<String> nice = splitSkillsString(jd.getNiceToHaveSkills());

        String jdJson = """
    {
      "jobTitle": %s,
      "summary": %s,
      "mustHaveSkills": %s,
      "niceToHaveSkills": %s,
      "suitableLevel": %s
    }
    """.formatted(
                toJson(jd.getJobTitle()),
                toJson(jd.getSummary()),
                toJsonArray(must),
                toJsonArray(nice),
                toJson(jd.getSuitableLevel())
        );

        return """
    You are an experienced IT recruiter. Compare the following CV against the JD and return a single valid JSON object only.

    Scoring rubric:
    - Overall score 0..100.
    - Consider: coverage of must-have skills (highest weight), nice-to-have skills, level fit (years vs. suitable level), and domain/job-title alignment.
    - Be strict on must-have: missing must-haves should lower the score significantly.

    Output JSON schema (exact keys):
    {
      "score": <int 0-100>,
      "verdict": "<STRONG_MATCH|PARTIAL_MATCH|LOW_MATCH>",
      "levelFit": <0..1>,
      "domainFit": <0..1>,
      "matchedMust": ["..."],
      "missingMust": ["..."],
      "matchedNice": ["..."],
      "extraCvSkills": ["..."],
      "reasons": ["..."],
      "recommendations": ["..."]
    }

    CV:
    %s

    JD:
    %s

    Return JSON only.
    """.formatted(cvJson, jdJson);
    }

    private String toJson(String s) {
        try {
            return objectMapper.writeValueAsString(s == null ? "" : s);
        } catch (Exception e) {
            return "\"\"";
        }
    }
    private String toJsonArray(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list == null ? List.of() : list);
        } catch (Exception e) {
            return "[]";
        }
    }

    public Response<CvJdMatchResultDTO> matchCurrentUsersActiveCvWithJdAI(Long jdId) throws Exception {
        User user = accountUtils.getCurrentAccount();
        if (user == null) throw new NotLoginException("Please log in to continue");

        // Lấy JD
        JD jd = jdRepository.findById(jdId)
                .orElseThrow(() -> new NotFoundException("JD not found with ID: " + jdId));

        // Lấy CV active mới nhất của user + extracted info
        MemberCV memberCV = memberCVRepository
                .findFirstByUserAndIsDeletedFalseAndIsActiveTrueOrderByUpdateAtDesc(user);
        if (memberCV == null) throw new NotFoundException("No active CV found for current user");

        CVExtractedInfo cv = cvExtractedInfoRepository
                .findFirstByMemberCVOrderByCreateAtDesc(memberCV);
        if (cv == null) throw new NotFoundException("Extracted CV info not found");

        // Đọc skills từ JSON string lưu trong DB
        final List<String> cvSkills = parseCvSkillsJson(cv.getSkills());

        // Build prompt & call Gemini
        String prompt = buildAiMatchPrompt(cv, jd, cvSkills);
        String aiText = callGemini(prompt);
        String cleaned = jsonOnly(aiText);

        // Parse kết quả AI
        CvJdMatchResultDTO dto;
        try {
            JsonNode root = objectMapper.readTree(cleaned);

            dto = CvJdMatchResultDTO.builder()
                    .score(root.path("score").isInt() ? root.get("score").asInt() : null)
                    .verdict(root.path("verdict").asText(null))
                    .levelFit(root.path("levelFit").isNumber() ? root.get("levelFit").asDouble() : null)
                    .domainFit(root.path("domainFit").isNumber() ? root.get("domainFit").asDouble() : null)
                    .matchedMust(readArrayOfText(root, "matchedMust"))
                    .missingMust(readArrayOfText(root, "missingMust"))
                    .matchedNice(readArrayOfText(root, "matchedNice"))
                    .extraCvSkills(readArrayOfText(root, "extraCvSkills"))
                    .reasons(readArrayOfText(root, "reasons"))
                    .recommendations(readArrayOfText(root, "recommendations"))
                    .build();

            // Nếu AI trả thiếu score/verdict, coi là lỗi format
            if (dto.getScore() == null || dto.getVerdict() == null) {
                throw new IllegalArgumentException("AI returned incomplete JSON: " + cleaned);
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid AI JSON: " + cleaned);
        }

        return new Response<>(200, "AI match computed", dto);
    }

    private List<String> readArrayOfText(JsonNode node, String field) {
        JsonNode arr = node.path(field);
        if (!arr.isArray()) return List.of();
        List<String> list = new java.util.ArrayList<>();
        arr.forEach(n -> { if (n.isTextual()) list.add(n.asText()); });
        return list;
    }



    public JD analyzeAndSaveJD(MultipartFile file) throws Exception {
        User user = accountUtils.getCurrentAccount();
        if (user == null) {
            throw new NotLoginException("Please log in to continue");
        }

        if (user.getUserUsage().getJdAnalyzeUsed() >= user.getAPackage().getJdAnalyzeCount()) {
            throw new AuthAppException(ErrorCode.OUT_OF_JD_ANALYZE_COUNT);
        }

        String text = FileUtils.extractTextFromCV(file);

        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("A JD link or JD content is required");
        }

        // IT-only prompt: yêu cầu phân loại trước, chỉ phân tích nếu là IT
        String prompt = String.format("""
            You are an expert in analyzing Job Descriptions (JDs) for the **IT/technology domain only**.

            Your tasks:
            1) Detect whether the JD belongs to IT (e.g., Software Engineer, Backend/Frontend/Full-stack, Mobile, DevOps/SRE, Cloud, Data/ML/AI, QA/Automation, Security, System/Network, Product/BA/PO in tech, Tech Lead/Architect, etc.).
            2) If and only if the JD is IT-related, analyze it and return fields as specified below.

            Output rules:
            - Return **one valid JSON object only** (no Markdown, no explanations).
            - If NON-IT, return:
              {
                "supported": false,
                "detectedDomain": "<short domain>",
                "message": "This service only supports IT job descriptions."
              }
            - If IT, return the fields at TOP LEVEL (plus supported/detectedDomain):
              {
                "supported": true,
                "detectedDomain": "IT",
                "jobTitle": "",
                "summary": "",
                "mustHaveSkills": "",
                "niceToHaveSkills": "",
                "suitableLevel": "",
                "recommendedLearning": "",
                "questions": [
                  {
                    "question": "",
                    "suitableAnswer1": "",
                    "suitableAnswer2": "",
                    "skillNeeded": "",
                    "difficultyLevel": "",   // easy / hard / very hard
                    "questionType": ""       // technical / behavioral / logic / other
                  }
                ]
              }

            JD content:
            %s
            """, text);

        String responseText = callGemini(prompt);

        // Parse AI JSON safely
        JsonNode root;
        try {
            String cleanedJson = responseText
                    .replaceAll("(?i)```json", "")
                    .replaceAll("(?i)```", "")
                    .trim();
            root = objectMapper.readTree(cleanedJson);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("AI returned an invalid JSON response:\n" + responseText);
        }

        // Gate: chỉ cho phép IT
        boolean supported = root.path("supported").asBoolean(false);
        if (!supported) {
            String detected = root.path("detectedDomain").asText("unknown");
            String msg = root.path("message").asText("This service only supports IT job descriptions.");
            throw new IllegalArgumentException(msg + " Detected domain: " + detected + ".");
        }

        // Từ đây chắc chắn là IT và có các field top-level
        JD jd = new JD();

        // Save images to Cloudinary
        String baseName = file.getOriginalFilename()
                .replaceAll(".pdf", "")
                .replaceAll("\\s+", "_");

        List<MultipartFile> imageFiles = PdfToImageConverter.convertPdfToMultipartImages(file.getInputStream(), baseName);

        StringBuilder imageUrls = new StringBuilder();
        for (MultipartFile img : imageFiles) {
            String url = cloudinaryUtils.uploadImage(img);
            if (!imageUrls.isEmpty()) imageUrls.append(";");
            imageUrls.append(url);
        }

        jd.setUser(user);
        jd.setLinkToJd(imageUrls.toString());
        jd.setJobTitle(getJsonText(root, "jobTitle"));
        jd.setSummary(getJsonText(root, "summary"));
        jd.setMustHaveSkills(getJsonText(root, "mustHaveSkills"));
        jd.setNiceToHaveSkills(getJsonText(root, "niceToHaveSkills"));
        jd.setSuitableLevel(getJsonText(root, "suitableLevel"));
        jd.setRecommendedLearning(getJsonText(root, "recommendedLearning"));
        jd.setCreateAt(LocalDateTime.now());
        jd.setUpdateAt(LocalDateTime.now());
        jd.setDeleted(false);

        JD savedJD = jdRepository.save(jd);

        // Questions (nếu có)
        JsonNode qs = root.path("questions");
        if (qs.isArray()) {
            for (JsonNode q : qs) {
                JDEvaluate evaluate = new JDEvaluate();
                evaluate.setJd(savedJD);
                evaluate.setQuestion(getJsonText(q, "question"));
                evaluate.setSuitableAnswer1(getJsonText(q, "suitableAnswer1"));
                evaluate.setSuitableAnswer2(getJsonText(q, "suitableAnswer2"));
                evaluate.setSkillNeeded(getJsonText(q, "skillNeeded"));
                evaluate.setDifficultyLevel(getJsonText(q, "difficultyLevel"));
                evaluate.setQuestionType(getJsonText(q, "questionType"));
                evaluate.setCreateAt(LocalDateTime.now());
                evaluate.setUpdateAt(LocalDateTime.now());

                jdEvaluateRepository.save(evaluate);
            }
        }

        // Chỉ trừ lượt khi thực sự phân tích IT & lưu thành công
        user.getUserUsage().setJdAnalyzeUsed(user.getUserUsage().getJdAnalyzeUsed() + 1);
        userRepository.save(user);
        userUsageRepository.save(user.getUserUsage());

        return savedJD;
    }

    private String getJsonText(JsonNode node, String field) {
        return node.has(field) ? node.get(field).asText() : "";
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
            // Adjust the path according to Gemini response
            return json.at("/candidates/0/content/parts/0/text").asText("No response from AI.");
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred while calling Gemini API.";
        }
    }

    public JD getJDById(Long id) {
        return jdRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("JD not found with ID: " + id));
    }

    public Response<List<GetAllJdDTO>> getAllJDsByUser() {
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null)
            return new Response<>(401, "Please log in to continue", null);

        List<JD> jds = jdRepository.findByUserAndIsDeletedFalseOrderByCreateAtDesc(currentUser);

        List<GetAllJdDTO> dtos = jds.stream()
                .map(jd -> GetAllJdDTO.builder()
                        .jdId(jd.getJdId())
                        .linkToJd(jd.getLinkToJd())
                        .jobtTitle(jd.getJobTitle())
                        .summary(jd.getSummary())
                        .createAt(jd.getCreateAt())
                        .build())
                .toList();

        return new Response<>(200, "JD list retrieved successfully", dtos);
    }

}
