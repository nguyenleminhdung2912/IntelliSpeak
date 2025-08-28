package com.gsu25se05.itellispeak.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CompanyJDService {

    private final CompanyJDRepository companyJDRepository;
    private final CompanyJDEvaluateRepository companyJDEvaluateRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private final AccountUtils accountUtils;
    private final UserRepository userRepository;
    private final CloudinaryUtils cloudinaryUtils;

    public CompanyJDService(CompanyJDRepository companyJDRepository, CompanyJDEvaluateRepository companyJDEvaluateRepository,
                            @Value("${genai.api.key}") String apiKey, AccountUtils accountUtils, UserRepository userRepository,
                            CloudinaryUtils cloudinaryUtils) {
        this.companyJDRepository = companyJDRepository;
        this.companyJDEvaluateRepository = companyJDEvaluateRepository;
        this.webClient = WebClient.builder()
                .baseUrl(API_URL + "?key=" + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.accountUtils = accountUtils;
        this.userRepository = userRepository;
        this.cloudinaryUtils = cloudinaryUtils;
    }

    public CompanyJD uploadAndAnalyzeCompanyJD(MultipartFile file) throws Exception {

        // Authenticate and authorize user
        User user = accountUtils.getCurrentAccount();
        if (user == null) {
            throw new NotLoginException("Please log in to continue");
        }
        if (!user.getRole().equals(User.Role.HR)) {
            throw new AuthAppException(ErrorCode.ACCOUNT_NOT_HR);
        }

        HR hr = user.getHr();
        if (hr == null || hr.getCompany() == null) {
            throw new NotFoundException("HR is not associated with a company");
        }
        Company company = hr.getCompany();

        // Extract text from PDF
        String text;
        try {
            text = FileUtils.extractTextFromCV(file);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to extract text from the PDF. The file may be corrupted or contain invalid fonts.");
        }
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("JD content could not be extracted from the file.");
        }

        // Prepare prompt for AI analysis
        String prompt = String.format("""
            You are an expert in analyzing Job Descriptions (JDs) for the **IT/technology domain only**.

            Your tasks:
            1) Detect whether the JD belongs to IT (e.g., Software Engineer, Backend/Frontend/Full-stack, Mobile, DevOps/SRE, Cloud, Data/ML/AI, QA/Automation, Security, System/Network, Product/BA/PO in tech, Tech Lead/Architect, etc.).
            2) If and only if the JD is IT-related, analyze it and return fields as specified below.
            3) Generate exactly 5 relevant interview questions tailored to the JD.

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
                    "difficultyLevel": "",   // easy / medium / hard
                    "questionType": ""       // technical / behavioral / logic / other
                  },
                  // Exactly 5 questions
                  ...
                ]
              }

            JD content:
            %s
            """, text);

        // Call Gemini API
        String responseText = callGemini(prompt);

        // Parse AI JSON response
        JsonNode root;
        try {
            String cleanedJson = responseText
                    .replaceAll("(?i)```json", "")
                    .replaceAll("(?i)```", "")
                    .trim();
            root = objectMapper.readTree(cleanedJson);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("AI returned an invalid JSON response.");
        }

        // Check if JD is IT-related
        boolean supported = root.path("supported").asBoolean(false);
        if (!supported) {
            String detected = root.path("detectedDomain").asText("unknown");
            String msg = root.path("message").asText("This service only supports IT job descriptions.");
            throw new IllegalArgumentException(msg + " Detected domain: " + detected + ".");
        }

        // Convert PDF to images and upload to Cloudinary
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

        // Create and populate CompanyJD
        CompanyJD companyJD = new CompanyJD();
        companyJD.setCompany(company);
        companyJD.setLinkToJd(imageUrls.toString());
        companyJD.setJobTitle(getJsonText(root, "jobTitle"));
        companyJD.setSummary(getJsonText(root, "summary"));
        companyJD.setMustHaveSkills(getJsonText(root, "mustHaveSkills"));
        companyJD.setNiceToHaveSkills(getJsonText(root, "niceToHaveSkills"));
        companyJD.setSuitableLevel(getJsonText(root, "suitableLevel"));
        companyJD.setRecommendedLearning(getJsonText(root, "recommendedLearning"));
        companyJD.setCreateAt(LocalDateTime.now());
        companyJD.setUpdateAt(LocalDateTime.now());
        companyJD.setDeleted(false);

        // Save CompanyJD
        CompanyJD savedCompanyJD = companyJDRepository.save(companyJD);

        // Process exactly 5 questions
        JsonNode questions = root.path("questions");
        if (!questions.isArray() || questions.size() < 5) {
            throw new IllegalArgumentException("AI did not provide exactly 5 questions as required.");
        }

        int questionCount = 0;
        for (JsonNode q : questions) {
            if (questionCount >= 5) break; // Ensure only 5 questions
            CompanyJDEvaluate evaluate = new CompanyJDEvaluate();
            evaluate.setCompanyJD(savedCompanyJD);
            evaluate.setQuestion(getJsonText(q, "question"));
            evaluate.setSuitableAnswer1(getJsonText(q, "suitableAnswer1"));
            evaluate.setSuitableAnswer2(getJsonText(q, "suitableAnswer2"));
            evaluate.setSkillNeeded(getJsonText(q, "skillNeeded"));
            evaluate.setDifficultyLevel(getJsonText(q, "difficultyLevel"));
            evaluate.setQuestionType(getJsonText(q, "questionType"));
            evaluate.setCreateAt(LocalDateTime.now());
            evaluate.setUpdateAt(LocalDateTime.now());
            companyJDEvaluateRepository.save(evaluate);
            questionCount++;
        }

        if (questionCount < 5) {
            throw new IllegalArgumentException("AI provided fewer than 5 questions.");
        }

        return savedCompanyJD;
    }

    public CompanyJD getCompanyJDWithEvaluates(Long companyJdId) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) {
            throw new NotLoginException("Please log in to continue");
        }
        if (!user.getRole().equals(User.Role.HR)) {
            throw new AuthAppException(ErrorCode.ACCOUNT_NOT_HR);
        }

        CompanyJD companyJD = companyJDRepository.findById(companyJdId)
                .orElseThrow(() -> new NotFoundException("Company JD not found with ID: " + companyJdId));

        HR hr = user.getHr();
        if (hr == null || hr.getCompany() == null || !hr.getCompany().equals(companyJD.getCompany())) {
            throw new AuthAppException(ErrorCode.HR_NOT_FOUND);
        }

        // Trigger lazy loading of evaluates
        companyJD.getCompanyJDEvaluates().size();
        return companyJD;
    }

    public List<CompanyJD> getCompanyJDsByCompanyId(Long companyId) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) {
            throw new NotLoginException("Please log in to continue");
        }
        if (!user.getRole().equals(User.Role.HR)) {
            throw new AuthAppException(ErrorCode.ACCOUNT_NOT_HR);
        }

        HR hr = user.getHr();
        if (hr == null || hr.getCompany() == null || !hr.getCompany().getCompanyId().equals(companyId)) {
            throw new AuthAppException(ErrorCode.HR_NOT_FOUND);
        }

        List<CompanyJD> jds = companyJDRepository.findByCompanyCompanyIdAndIsDeletedFalse(companyId);
        return jds;
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
            return json.at("/candidates/0/content/parts/0/text").asText("No response from AI.");
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while calling Gemini API.");
        }
    }
}
