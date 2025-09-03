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
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Comparator;
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
    - Overall score 0..100. This overall score should also be considered by levelFit and domainFit. For example if level FIT is 1, domain FIT is 1, and the user have every must have skill and nice to have skill, then the score must around 90 or upper.
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
        if (user == null) throw new NotLoginException("Please log in to continue");

        if (user.getUserUsage().getJdAnalyzeUsed() >= user.getAPackage().getJdAnalyzeCount()) {
            throw new AuthAppException(ErrorCode.OUT_OF_JD_ANALYZE_COUNT);
        }

        String text = FileUtils.extractTextFromCV(file);
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("A JD file with readable text is required");
        }

        //chặn CV trước khi gọi AI
        if (looksLikeCV(text) && !looksLikeJD(text)) {
            throw new IllegalArgumentException("The uploaded file looks like a CV/resume, not a Job Description.");
        }

        // (2) Prompt buộc AI phân loại tài liệu
        String prompt = String.format("""
        You are an expert in analyzing **Job Descriptions** (JDs) for the **IT/technology domain only**.

        First, strictly classify the input document by type:
        - "JD": recruitment/job description posting (contains role title, responsibilities, requirements, benefits, company info, etc.)
        - "CV": resume/curriculum vitae (candidate profile, education, projects, skills, experience)
        - "Other": anything else.

        Output JSON ONLY (no markdown). If documentType != "JD", return:
        {
          "documentType": "<JD|CV|Other>",
          "supported": false,
          "message": "This endpoint only accepts Job Descriptions."
        }

        If documentType == "JD", continue and check IT-only. If NON-IT JD, return:
        {
          "documentType": "JD",
          "supported": false,
          "message": "This service only supports IT job descriptions."
        }

        If IT JD, return exactly:
        {
          "documentType": "JD",
          "supported": true,
          "detectedDomain": "<short IT domain>",
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
              "difficultyLevel": "",
              "questionType": ""
            }
          ]
        }

        Document content:
        %s
        """, text);

        String responseText = callGemini(prompt);

        // Parse AI JSON safely
        JsonNode root;
        try {
            String cleaned = responseText.replaceAll("(?i)```json", "")
                    .replaceAll("(?i)```", "")
                    .trim();
            root = objectMapper.readTree(cleaned);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("AI returned an invalid JSON response:\n" + responseText);
        }

        String docType = root.path("documentType").asText("");
        boolean supported = root.path("supported").asBoolean(false);
        if (!"JD".equalsIgnoreCase(docType) || !supported) {
            String msg = root.path("message").asText("This endpoint only accepts Job Descriptions.");
            throw new IllegalArgumentException(msg);
        }

        String baseName = file.getOriginalFilename()
                .replaceAll(".pdf", "")
                .replaceAll("\\s+", "_");

        List<MultipartFile> images = PdfToImageConverter.convertPdfToMultipartImages(file.getInputStream(), baseName);
        StringBuilder imageUrls = new StringBuilder();
        for (MultipartFile img : images) {
            String url = cloudinaryUtils.uploadImage(img);
            if (!imageUrls.isEmpty()) imageUrls.append(";");
            imageUrls.append(url);
        }

        JD jd = new JD();
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

        // Questions
        JsonNode qs = root.path("questions");
        if (qs.isArray()) {
            for (JsonNode q : qs) {
                JDEvaluate e = new JDEvaluate();
                e.setJd(savedJD);
                e.setQuestion(getJsonText(q, "question"));
                e.setSuitableAnswer1(getJsonText(q, "suitableAnswer1"));
                e.setSuitableAnswer2(getJsonText(q, "suitableAnswer2"));
                e.setSkillNeeded(getJsonText(q, "skillNeeded"));
                e.setDifficultyLevel(getJsonText(q, "difficultyLevel"));
                e.setQuestionType(getJsonText(q, "questionType"));
                e.setCreateAt(LocalDateTime.now());
                e.setUpdateAt(LocalDateTime.now());
                jdEvaluateRepository.save(e);
            }
        }

        user.getUserUsage().setJdAnalyzeUsed(user.getUserUsage().getJdAnalyzeUsed() + 1);
        userRepository.save(user);
        userUsageRepository.save(user.getUserUsage());

        return savedJD;
    }

    /** Heuristic: nhận diện nhanh CV/JD trước khi gọi AI */
    private boolean looksLikeCV(String text) {
        String s = text.toLowerCase();
        int hits =
                (s.contains("curriculum vitae") ? 1 : 0) +
                        (s.contains("resume") ? 1 : 0) +
                        (s.contains("education") ? 1 : 0) +
                        (s.contains("experience") ? 1 : 0) +
                        (s.contains("projects") ? 1 : 0) +
                        (s.contains("skills") ? 1 : 0) +
                        (s.contains("certificate") || s.contains("certifications") ? 1 : 0) +
                        (s.contains("summary") ? 1 : 0) +
                        (s.matches("(?s).*\\b(github|linkedin)\\.com/.*") ? 1 : 0);
        return hits >= 3;
    }

    private boolean looksLikeJD(String text) {
        String s = text.toLowerCase();
        int hits =
                (s.contains("we are hiring") || s.contains("we're hiring") ? 1 : 0) +
                        (s.contains("job description") ? 1 : 0) +
                        (s.contains("responsibilities") || s.contains("responsibility") ? 1 : 0) +
                        (s.contains("requirements") || s.contains("requirement") ? 1 : 0) +
                        (s.contains("benefits") ? 1 : 0) +
                        (s.contains("qualifications") ? 1 : 0) +
                        (s.contains("salary") || s.contains("compensation") ? 1 : 0) +
                        (s.contains("apply now") || s.contains("how to apply") ? 1 : 0);
        return hits >= 2;
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
        JD jd = jdRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("JD not found with ID: " + id));

        if (Boolean.TRUE.equals(jd.isDeleted())) {
            throw new NotFoundException("JD not found with ID: " + id);
        }

        return jd;
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
                // sắp xếp theo ngày tạo giảm dần (mới nhất trước)
                .sorted(Comparator.comparing(GetAllJdDTO::getCreateAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        return new Response<>(200, "JD list retrieved successfully", dtos);
    }

    @Transactional
    public void deleteJd(Long jdId) {
        // 1. Get current user
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            throw new NotLoginException("Please log in to continue");
        }

        // 2. Find the JD and verify ownership
        JD jd = jdRepository.findById(jdId)
                .orElseThrow(() -> new NotFoundException("JD not found with ID: " + jdId));

        if (!jd.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new AuthAppException(ErrorCode.ACTION_FORBIDDEN);
        }

        // 3. Soft delete
        jd.setDeleted(true);
        jd.setUpdateAt(LocalDateTime.now());
        jdRepository.save(jd);
    }

}
