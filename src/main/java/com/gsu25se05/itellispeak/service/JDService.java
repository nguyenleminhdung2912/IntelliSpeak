package com.gsu25se05.itellispeak.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.jd.GetAllJdDTO;
import com.gsu25se05.itellispeak.entity.JD;
import com.gsu25se05.itellispeak.entity.JDEvaluate;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.exception.ErrorCode;
import com.gsu25se05.itellispeak.exception.auth.AuthAppException;
import com.gsu25se05.itellispeak.exception.auth.NotFoundException;
import com.gsu25se05.itellispeak.exception.auth.NotLoginException;
import com.gsu25se05.itellispeak.repository.JDEvaluateRepository;
import com.gsu25se05.itellispeak.repository.JDRepository;
import com.gsu25se05.itellispeak.repository.UserRepository;
import com.gsu25se05.itellispeak.repository.UserUsageRepository;
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

    public JDService(JDRepository jdRepository, @Value("${genai.api.key}") String apiKey, AccountUtils accountUtils, UserRepository userRepository, JDEvaluateRepository jdEvaluateRepository, CloudinaryUtils cloudinaryUtils, UserUsageRepository userUsageRepository) {
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

        List<JD> jds = jdRepository.findByUserAndIsDeletedFalse(currentUser);

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
