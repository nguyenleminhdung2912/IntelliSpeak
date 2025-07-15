package com.gsu25se05.itellispeak.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsu25se05.itellispeak.dto.jd.JDInputDTO;
import com.gsu25se05.itellispeak.entity.JD;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.exception.auth.NotFoundException;
import com.gsu25se05.itellispeak.repository.JDRepository;
import com.gsu25se05.itellispeak.repository.UserRepository;
import com.gsu25se05.itellispeak.utils.AccountUtils;
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
public class JDService {

    private final JDRepository jdRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private final AccountUtils accountUtils;
    private final UserRepository userRepository;

    public JDService(JDRepository jdRepository, @Value("${genai.api.key}") String apiKey, AccountUtils accountUtils, UserRepository userRepository) {
        this.jdRepository = jdRepository;
        this.webClient = WebClient.builder()
                .baseUrl(API_URL + "?key=" + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.accountUtils = accountUtils;
        this.userRepository = userRepository;
    }

    public JD analyzeAndSaveJD(MultipartFile file) throws Exception {
        User user = accountUtils.getCurrentAccount();
        String text = FileUtils.extractTextFromCV(file);

        String prompt;

        if (text != null && !text.isEmpty()) {
            prompt = String.format("""
                    Hãy phân tích nội dung mô tả công việc dưới đây và trả kết quả dưới dạng **JSON hợp lệ** (chỉ JSON, không Markdown, không giải thích):
                    
                    {
                      "jobTitle": "",
                      "summary": "",
                      "mustHaveSkills": "",
                      "niceToHaveSkills": "",
                      "suitableLevel": "",
                      "recommendedLearning": ""
                    }
                    Nội dung JD: %s
                    """, text);
        } else {
            throw new IllegalArgumentException("Phải nhập link hoặc nội dung JD");
        }

        String responseText = callGemini(prompt);

        // Giả định AI trả về JSON dạng string
        JsonNode jsonNode;
        try {
            // ✅ Làm sạch kết quả có thể có ```json hoặc ``` bao quanh
            String cleanedJson = responseText
                    .replaceAll("(?i)```json", "")
                    .replaceAll("(?i)```", "")
                    .trim();

            jsonNode = objectMapper.readTree(cleanedJson);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("AI trả về kết quả không hợp lệ JSON:\n" + responseText);
        }

        if (user == null) {
            throw new NotFoundException("Không tìm thấy người dùng đăng nhập");
        }

        JD jd = new JD();
        jd.setUser(user);
        jd.setLinkToJd("");
        jd.setJobTitle(getJsonText(jsonNode, "jobTitle"));
        jd.setSummary(getJsonText(jsonNode, "summary"));
        jd.setMustHaveSkills(getJsonText(jsonNode, "mustHaveSkills"));
        jd.setNiceToHaveSkills(getJsonText(jsonNode, "niceToHaveSkills"));
        jd.setSuitableLevel(getJsonText(jsonNode, "suitableLevel"));
        jd.setRecommendedLearning(getJsonText(jsonNode, "recommendedLearning"));
        jd.setCreateAt(LocalDateTime.now());
        jd.setUpdateAt(LocalDateTime.now());
        jd.setDeleted(false);

        return jdRepository.save(jd);
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
            // Đường dẫn tùy API response, ví dụ:
            return json.at("/candidates/0/content/parts/0/text").asText("Không có phản hồi từ AI.");
        } catch (Exception e) {
            e.printStackTrace();
            return "Đã xảy ra lỗi khi gọi Gemini API.";
        }
    }

}

