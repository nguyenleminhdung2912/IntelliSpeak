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
            throw new NotLoginException("Vui lòng đăng nhập để tiếp tục");
        }

        if (user.getUserUsage().getJdAnalyzeUsed() >= user.getAPackage().getJdAnalyzeCount()) {
            throw new AuthAppException(ErrorCode.OUT_OF_JD_ANALYZE_COUNT);
        }

        String text = FileUtils.extractTextFromCV(file);

        String prompt;

        if (text != null && !text.isEmpty()) {
            prompt = String.format("""
                    Hãy phân tích nội dung mô tả công việc dưới đây và trả kết quả dưới dạng JSON hợp lệ (chỉ JSON, không Markdown, không giải thích). Hãy chắc chắn rằng mọi thứ là tiếng Việt:
                    
                    {
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
                          "difficultyLevel": "",  // dễ / khó / siêu khó
                          "questionType": ""       // technical / behavior / logic...
                        }
                      ]
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

        JD jd = new JD();

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

        jd.setUser(user);
        jd.setLinkToJd(imageUrls.toString());
        jd.setJobTitle(getJsonText(jsonNode, "jobTitle"));
        jd.setSummary(getJsonText(jsonNode, "summary"));
        jd.setMustHaveSkills(getJsonText(jsonNode, "mustHaveSkills"));
        jd.setNiceToHaveSkills(getJsonText(jsonNode, "niceToHaveSkills"));
        jd.setSuitableLevel(getJsonText(jsonNode, "suitableLevel"));
        jd.setRecommendedLearning(getJsonText(jsonNode, "recommendedLearning"));
        jd.setCreateAt(LocalDateTime.now());
        jd.setUpdateAt(LocalDateTime.now());
        jd.setDeleted(false);

        JD savedJD = jdRepository.save(jd);

        if (jsonNode.has("questions") && jsonNode.get("questions").isArray()) {
            ArrayNode questions = (ArrayNode) jsonNode.get("questions");
            for (JsonNode q : questions) {
                JDEvaluate evaluate = new JDEvaluate();
                evaluate.setJd(savedJD); // dùng JD đã được lưu
                evaluate.setQuestion(getJsonText(q, "question"));
                evaluate.setSuitableAnswer1(getJsonText(q, "suitableAnswer1"));
                evaluate.setSuitableAnswer2(getJsonText(q, "suitableAnswer2"));
                evaluate.setSkillNeeded(getJsonText(q, "skillNeeded"));
                evaluate.setDifficultyLevel(getJsonText(q, "difficultyLevel"));
                evaluate.setQuestionType(getJsonText(q, "questionType"));
                evaluate.setCreateAt(LocalDateTime.now());
                evaluate.setUpdateAt(LocalDateTime.now());

                jdEvaluateRepository.save(evaluate); // giờ thì không lỗi nữa
            }
        }

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
            // Đường dẫn tùy API response, ví dụ:
            return json.at("/candidates/0/content/parts/0/text").asText("Không có phản hồi từ AI.");
        } catch (Exception e) {
            e.printStackTrace();
            return "Đã xảy ra lỗi khi gọi Gemini API.";
        }
    }

    public JD getJDById(Long id) {
        return jdRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy JD với ID: " + id));
    }

    public Response<List<GetAllJdDTO>> getAllJDsByUser() {
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null)
            return new Response<>(401, "Vui lòng đăng nhập để tiếp tục", null);

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

        return new Response<>(200, "Lấy danh sách JD thành công", dtos);
    }

}
