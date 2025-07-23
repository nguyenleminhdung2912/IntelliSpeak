package com.gsu25se05.itellispeak.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.cv.CVAnalysisResponseDTO;
import com.gsu25se05.itellispeak.entity.*;
import com.gsu25se05.itellispeak.repository.*;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import com.gsu25se05.itellispeak.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    public CVService(
            @Value("${genai.api.key}") String apiKey,
            CVEvaluateRepository cvEvaluateRepository,
            CVFeedbackCategoryRepository categoryRepository,
            CVFeedbackTipRepository tipRepository,
            CVExtractedInfoRepository cvExtractedInfoRepository,
            MemberCVRepository memberCVRepository,
            AccountUtils accountUtils
    ) {
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
    }

    private String sanitizeText(String text) {
        // Loại bỏ ký tự control ASCII không in được (mã < 32 trừ newline/tab)
        return text.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "")
                .replaceAll("�", "") // Loại ký tự lỗi font
                .replaceAll("\\p{C}", ""); // Ký tự "invisible" (Unicode control)
    }

    public Response<CVAnalysisResponseDTO> analyzeAndSaveFromFile(MultipartFile file) throws Exception {
        String rawText = FileUtils.extractTextFromCV(file);
        String cleanText = sanitizeText(rawText);
        return analyzeAndSaveEvaluation(cleanText);
    }

    public Response<CVAnalysisResponseDTO> analyzeAndSaveEvaluation(String cvText) throws Exception {
        String prompt = preparePrompt(cvText);
        String response = callGemini(prompt);
        String cleaned = cleanJson(response);
        JsonNode root = objectMapper.readTree(cleaned);

        JsonNode feedbackNode = root.get("feedback");
        JsonNode infoNode = root.get("extractedInfo");

        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Vui lòng đăng nhập để tiếp tục", null);

        // Save MemberCV
        MemberCV memberCV = new MemberCV();
        memberCV.setUser(user);
        memberCV.setDeleted(false);
        memberCV.setCreateAt(LocalDateTime.now());
        memberCVRepository.save(memberCV);

        // Save CVEvaluate
        CVEvaluate cvEvaluate = new CVEvaluate();
        cvEvaluate.setMemberCV(memberCV);
        cvEvaluate.setOverallScore(feedbackNode.get("overallScore").asInt());
        cvEvaluate.setCreateAt(LocalDateTime.now());
        cvEvaluate.setUpdateAt(LocalDateTime.now());
        cvEvaluate.setDeleted(false);
        cvEvaluateRepository.save(cvEvaluate);

        // Save each feedback category and tips
        List<String> categories = List.of("ATS", "toneAndStyle", "content", "structure", "skills");
        for (String cat : categories) {
            JsonNode catNode = feedbackNode.get(cat);
            CVFeedbackCategory category = new CVFeedbackCategory();
            category.setCvEvaluate(cvEvaluate);
            category.setCategoryName(cat);
            category.setScore(catNode.get("score").asInt());
            categoryRepository.save(category);

            for (JsonNode tipNode : catNode.get("tips")) {
                CVFeedbackTip tip = new CVFeedbackTip();
                tip.setFeedbackCategory(category);
                String rawType = tipNode.get("type").asText();
                TipType tipType = TipType.fromString(rawType);

                if (tipType == null) {
                    // Log cảnh báo để debug
                    System.err.println("❌ Invalid tip type from AI: " + rawType);
                    continue; // skip tip
                }

                tip.setType(tipType);
                tip.setTip(tipNode.get("tip").asText());
                tip.setExplanation(tipNode.has("explanation") ? tipNode.get("explanation").asText() : null);
                tipRepository.save(tip);
            }
        }

        // Save extracted info
        CVExtractedInfo extracted = new CVExtractedInfo();
        extracted.setMemberCV(memberCV);
        extracted.setFullName(infoNode.get("fullName").asText());
        extracted.setEmail(infoNode.get("email").asText());
        extracted.setPhone(infoNode.get("phone").asText());
        extracted.setTotalYearsExperience(infoNode.get("totalYearsExperience").asInt());
        extracted.setEducationLevel(infoNode.get("educationLevel").asText());
        extracted.setUniversity(infoNode.get("university").asText());
        extracted.setSkills(objectMapper.writeValueAsString(infoNode.get("skills")));
        extracted.setCertifications(infoNode.get("certifications").asText());
        extracted.setCareerGoals(infoNode.get("careerGoals").asText());
        extracted.setWorkExperience(infoNode.get("workExperience").asText());
        extracted.setCreateAt(LocalDateTime.now());
        extracted.setUpdateAt(LocalDateTime.now());

        List<CVFeedbackCategory> returnCvEvaluate1 = getCV(cvEvaluate.getId()).getData().getCategories();
        System.out.println("Thông tin categories 2");
        for (CVFeedbackCategory category : returnCvEvaluate1)
        {
            System.out.println(category);
        }

        CVAnalysisResponseDTO dto = new CVAnalysisResponseDTO(getCV(cvEvaluate.getId()).getData(), cvExtractedInfoRepository.save(extracted));
        return new Response<>(200, "Phân tích thành công", dto);
    }

    private String preparePrompt(String cvText) {
        return String.format("""
                Bạn là một chuyên gia về hệ thống ATS (Applicant Tracking System) và phân tích CV.
                Hãy đánh giá và cho điểm CV sau, đồng thời đưa ra gợi ý để cải thiện.
                Hãy đánh giá chi tiết, trung thực. Nếu CV chưa tốt, hãy chấm điểm thấp và giải thích rõ lý do.
                
                Trả về dữ liệu ở định dạng JSON, **chỉ JSON**, không thêm giải thích nào khác.
                Trong đó:
                - `overallScore`: tổng điểm (tối đa 100).
                - `type` trong `tips` chỉ được dùng 1 trong các giá trị: `"good"`, `"improve"`, `"warning"`, `"dangerous"`, `"neutral"`, `"note"`.
                
                Dưới đây là cấu trúc kết quả mẫu:
                
                {
                  "feedback": {
                    "overallScore": 85,
                    "ATS": {
                      "score": 90,
                      "tips": [
                        {
                          "type": "good",
                          "tip": "Sử dụng định dạng thân thiện với ATS.",
                          "explanation": "CV có cấu trúc rõ ràng, giúp hệ thống ATS dễ đọc và phân tích."
                        }
                      ]
                    },
                    "toneAndStyle": {
                      "score": 85,
                      "tips": [...]
                    },
                    "content": {
                      "score": 80,
                      "tips": [...]
                    },
                    "structure": {
                      "score": 75,
                      "tips": [...]
                    },
                    "skills": {
                      "score": 70,
                      "tips": [...]
                    }
                  },
                  "extractedInfo": {
                    "fullName": "Nguyễn Văn A",
                    "email": "example@gmail.com",
                    "phone": "0123456789",
                    "totalYearsExperience": 3,
                    "educationLevel": "Đại học",
                    "university": "Đại học FPT",
                    "skills": ["Java", "Spring Boot"],
                    "certifications": "Chứng chỉ AWS",
                    "careerGoals": "Trở thành lập trình viên backend chuyên nghiệp.",
                    "workExperience": "..."
                  }
                }
                
                Dưới đây là nội dung CV:
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
            return json.at("/candidates/0/content/parts/0/text").asText("Không có phản hồi từ AI.");
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi khi gọi Gemini API.";
        }
    }

    private String cleanJson(String text) {
        return text.replaceAll("(?i)```json", "").replaceAll("(?i)```", "").trim();
    }

    public Response<CVEvaluate> getCV(Long id) {
        CVEvaluate cvEvaluate = cvEvaluateRepository.findById(id).orElse(null);
        return new Response<>(200, "Thành công", cvEvaluate);
    }
}
