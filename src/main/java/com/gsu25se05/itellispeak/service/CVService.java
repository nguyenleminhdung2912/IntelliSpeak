package com.gsu25se05.itellispeak.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsu25se05.itellispeak.entity.CVEvaluate;
import com.gsu25se05.itellispeak.entity.CVExtractedInfo;
import com.gsu25se05.itellispeak.entity.MemberCV;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.repository.CVEvaluateReposiotory;
import com.gsu25se05.itellispeak.repository.CVExtractedInfoRepository;
import com.gsu25se05.itellispeak.repository.MemberCVRepository;
import com.gsu25se05.itellispeak.repository.UserRepository;
import com.gsu25se05.itellispeak.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CVService {
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private final CVEvaluateReposiotory cvEvaluateRepository;
    private final CVExtractedInfoRepository cvExtractedInfoRepository;
    private final MemberCVRepository memberCVRepository;
    private final UserRepository userRepository;

    public CVService(@Value("${genai.api.key}") String apiKey, CVEvaluateReposiotory cvEvaluateRepository, CVExtractedInfoRepository cvExtractedInfoRepository, MemberCVRepository memberCVRepository, UserRepository userRepository) {
        this.webClient = WebClient.builder()
                .baseUrl(API_URL + "?key=" + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.cvEvaluateRepository = cvEvaluateRepository;
        this.cvExtractedInfoRepository = cvExtractedInfoRepository;
        this.memberCVRepository = memberCVRepository;
        this.userRepository = userRepository;
    }

    public CVEvaluate analyzeAndSaveFromFile(MultipartFile file) throws Exception {
        String text = FileUtils.extractTextFromCV(file);
        return analyzeAndSaveEvaluation(text);
    }

    public CVEvaluate analyzeAndSaveEvaluation(String cvText) throws Exception {
        String prompt = String.format("""
        Hãy đánh giá và trích xuất thông tin từ CV dưới đây. 
        Trả kết quả dưới dạng JSON hợp lệ gồm 2 phần:

        {
          "evaluation": {
            "score": 85,
            "isGood": true,
            "whatToImprove": "...",
            "presentationFeedback": "...",
            "grammarFeedback": "...",
            "relevanceScore": 80,
            "missingSkills": "...",
            "highlightedSkills": "...",
            "recommendations": "..."
          },
          "extractedInfo": {
            "fullName": "...",
            "email": "...",
            "phone": "...",
            "totalYearsExperience": 3,
            "educationLevel": "...",
            "university": "...",
            "skills": ["Java", "Spring Boot", "..."],
            "certifications": "...",
            "careerGoals": "...",
            "workExperience": "..."
          }
        }

        CV nội dung: %s
    """, cvText);

        String response = callGemini(prompt);
        String cleaned = cleanJson(response);
        JsonNode rootNode = objectMapper.readTree(cleaned);

        JsonNode evalNode = rootNode.get("evaluation");
        JsonNode infoNode = rootNode.get("extractedInfo");

        User user = userRepository.findByUserId(UUID.fromString("029ffe92-8d76-41bb-be00-aafd67f52fd9")).orElse(null);

        // Create and save MemberCV
        MemberCV memberCV = new MemberCV();
        memberCV.setCreateAt(LocalDateTime.now());
        memberCV.setDeleted(false);
        memberCV.setUser(user);
        memberCVRepository.save(memberCV);

        // Create and save CVEvaluate
        CVEvaluate evaluation = new CVEvaluate();
        evaluation.setScore(evalNode.get("score").asInt());
        evaluation.setIsGood(evalNode.get("isGood").asBoolean());
        evaluation.setWhatToImprove(evalNode.get("whatToImprove").asText());
        evaluation.setPresentationFeedback(evalNode.get("presentationFeedback").asText());
        evaluation.setGrammarFeedback(evalNode.get("grammarFeedback").asText());
        evaluation.setRelevanceScore(evalNode.get("relevanceScore").asInt());
        evaluation.setMissingSkills(evalNode.get("missingSkills").asText());
        evaluation.setHighlightedSkills(evalNode.get("highlightedSkills").asText());
        evaluation.setRecommendations(evalNode.get("recommendations").asText());
        evaluation.setCreateAt(LocalDateTime.now());
        evaluation.setUpdateAt(LocalDateTime.now());
        evaluation.setDeleted(false);
        evaluation.setMemberCV(memberCV);
        cvEvaluateRepository.save(evaluation);

        // Create and save CVExtractedInfo
        CVExtractedInfo extractedInfo = new CVExtractedInfo();
        extractedInfo.setMemberCV(memberCV);
        extractedInfo.setFullName(infoNode.get("fullName").asText());
        extractedInfo.setEmail(infoNode.get("email").asText());
        extractedInfo.setPhone(infoNode.get("phone").asText());
        extractedInfo.setTotalYearsExperience(infoNode.get("totalYearsExperience").asInt());
        extractedInfo.setEducationLevel(infoNode.get("educationLevel").asText());
        extractedInfo.setUniversity(infoNode.get("university").asText());
        extractedInfo.setSkills(objectMapper.writeValueAsString(infoNode.get("skills")));
        extractedInfo.setCertifications(infoNode.get("certifications").asText());
        extractedInfo.setCareerGoals(infoNode.get("careerGoals").asText());
        extractedInfo.setWorkExperience(infoNode.get("workExperience").asText());
        extractedInfo.setCreateAt(LocalDateTime.now());
        extractedInfo.setUpdateAt(LocalDateTime.now());
        cvExtractedInfoRepository.save(extractedInfo);

        return evaluation;
    }



    private String cleanJson(String text) {
        return text.replaceAll("(?i)```json", "").replaceAll("(?i)```", "").trim();
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
            return json.at("/candidates/0/content/parts/0/text").asText("Không có phản hồi từ AI.");
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi khi gọi Gemini API.";
        }
    }
}
