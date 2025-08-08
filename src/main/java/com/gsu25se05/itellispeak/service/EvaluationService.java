package com.gsu25se05.itellispeak.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsu25se05.itellispeak.dto.ai_evaluation.*;
import com.gsu25se05.itellispeak.entity.*;
import com.gsu25se05.itellispeak.exception.auth.NotLoginException;
import com.gsu25se05.itellispeak.repository.InterviewHistoryDetailRepository;
import com.gsu25se05.itellispeak.repository.InterviewHistoryRepository;
import com.gsu25se05.itellispeak.repository.InterviewSessionRepository;
import com.gsu25se05.itellispeak.repository.QuestionRepository;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(EvaluationService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final InterviewHistoryRepository interviewHistoryRepository;
    private final InterviewHistoryDetailRepository interviewHistoryDetailRepository;
    private final QuestionRepository questionRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final AccountUtils accountUtils;

    public EvaluationService(
            @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent}") String geminiApiUrl,
            @Value("${gemini.api.key}") String apiKey,
            InterviewHistoryRepository interviewHistoryRepository,
            InterviewHistoryDetailRepository interviewHistoryDetailRepository,
            QuestionRepository questionRepository,
            InterviewSessionRepository interviewSessionRepository,
            AccountUtils accountUtils) {
        this.webClient = WebClient.builder()
                .baseUrl(geminiApiUrl + "?key=" + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.interviewHistoryRepository = interviewHistoryRepository;
        this.interviewHistoryDetailRepository = interviewHistoryDetailRepository;
        this.questionRepository = questionRepository;
        this.interviewSessionRepository = interviewSessionRepository;
        this.accountUtils = accountUtils;
    }

    @Transactional
    public EvaluationBatchResponseDto evaluateBatch(EvaluationRequestDto request) {

        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) throw new NotLoginException("Vui lòng đăng nhập để tiếp tục");

        EvaluationBatchResponseDto responseDto = new EvaluationBatchResponseDto();
        List<EvaluationResponseDto> results = new ArrayList<>();

        try {
            // Lấy thông tin buổi phỏng vấn
            InterviewSessionDto session = request.getInterviewSession();

            // Tạo prompt với chatHistory
            String prompt = buildPrompt(session, request.getChatHistory());
            logger.info("Prompt gửi tới Gemini: {}", prompt);

            // Tạo request body cho Gemini
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of(
                                    "text", prompt
                            ))
                    ))
            );
            logger.info("Request body: {}", objectMapper.writeValueAsString(requestBody));

            // Gọi Gemini API
            String response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            logger.info("Phản hồi từ Gemini: {}", response);

            // Parse phản hồi JSON từ Gemini
            JsonNode json = objectMapper.readTree(response);
            JsonNode resultText = json.at("/candidates/0/content/parts/0/text");
            if (resultText.isMissingNode()) {
                throw new RuntimeException("Không nhận được text từ Gemini");
            }

            // Làm sạch và parse mảng JSON từ text
            String cleanedJson = resultText.asText()
                    .replaceAll("(?i)```json", "")
                    .replaceAll("(?i)```", "")
                    .trim();
            JsonNode resultsJson = objectMapper.readTree(cleanedJson);

            // Tìm InterviewSession từ database
            InterviewSession interviewSessionEntity = interviewSessionRepository.findById((long) session.getInterviewSessionId())
                    .orElse(null);
            if (interviewSessionEntity == null) {
                throw new RuntimeException("Không tìm thấy InterviewSession với ID: " + session.getInterviewSessionId());
            }

            // Tạo và lưu InterviewHistory trước
            InterviewHistory interviewHistory = new InterviewHistory();
            interviewHistory.setInterviewSession(interviewSessionEntity);
            interviewHistory.setTotalQuestion(session.getTotalQuestion());
            interviewHistory.setStartedAt(LocalDateTime.now());
            interviewHistory.setUser(currentUser);
            interviewHistory = interviewHistoryRepository.save(interviewHistory); // Lưu trước để có ID

            // Tạo danh sách InterviewHistoryDetail
            List<InterviewHistoryDetail> details = new ArrayList<>();
            double totalScore = 0.0;
            int evaluatedQuestions = 0;

            // Chuyển đổi phản hồi thành DTO và lưu vào database
            if (resultsJson.isArray()) {
                for (JsonNode jsonObj : resultsJson) {
                    EvaluationResponseDto dto = new EvaluationResponseDto();
                    Long questionId = jsonObj.get("questionId").asLong();
                    dto.setQuestionId(questionId);
                    dto.setQuestion(jsonObj.get("question").asText());
                    dto.setUserAnswer(jsonObj.get("userAnswer").asText());
                    String levelStr = jsonObj.get("level").asText();
                    Double score = normalizeLevelToScore(levelStr);
                    dto.setLevel(score.toString()); // Lưu score dưới dạng chuỗi cho DTO

                    // Parse feedback
                    JsonNode feedbackJson = jsonObj.get("feedback");
                    FeedbackDto feedback = new FeedbackDto();
                    FeedbackDto.KnowledgeFeedback knowledge = new FeedbackDto.KnowledgeFeedback();
                    knowledge.setCorrectness(feedbackJson.get("knowledge").get("correctness").asText());
                    knowledge.setImprovement(feedbackJson.get("knowledge").get("improvement").asText());
                    knowledge.setStrengths(feedbackJson.get("knowledge").get("strengths").asText());
                    FeedbackDto.CommunicationFeedback communication = new FeedbackDto.CommunicationFeedback();
                    communication.setClarity(feedbackJson.get("communication").get("clarity").asText());
                    communication.setConciseness(feedbackJson.get("communication").get("conciseness").asText());
                    communication.setTerminology(feedbackJson.get("communication").get("terminology").asText());
                    feedback.setKnowledge(knowledge);
                    feedback.setCommunication(communication);
                    feedback.setConclusion(feedbackJson.get("conclusion").asText());
                    dto.setFeedback(feedback);

                    // Thêm suitableAnswer1 và suitableAnswer2 từ danh sách câu hỏi
                    session.getQuestions().stream()
                            .filter(q -> q.getQuestionId() == questionId)
                            .findFirst()
                            .ifPresent(q -> {
                                dto.setSuitableAnswer1(q.getSuitableAnswer1());
                                dto.setSuitableAnswer2(q.getSuitableAnswer2());
                            });

                    // Tìm Question từ database
                    Question question = questionRepository.findById((long) questionId).orElse(null);
                    if (question == null) {
                        logger.warn("Không tìm thấy Question với ID: {}", questionId);
                        continue; // Bỏ qua nếu không tìm thấy câu hỏi
                    }

                    // Tạo InterviewHistoryDetail
                    InterviewHistoryDetail detail = new InterviewHistoryDetail();
                    detail.setQuestion(question);
                    detail.setInterviewHistory(interviewHistory); // Gán InterviewHistory đã lưu
                    detail.setAnsweredContent(dto.getUserAnswer());
                    detail.setScore(score);
                    detail.setAiEvaluatedContent(objectMapper.writeValueAsString(feedback));
                    detail.setSuitableAnswer1(dto.getSuitableAnswer1());
                    detail.setSuitableAnswer2(dto.getSuitableAnswer2());
                    detail.setDifficulty(Difficulty.valueOf(
                            session.getQuestions().stream()
                                    .filter(q -> q.getQuestionId() == questionId)
                                    .findFirst()
                                    .map(QuestionDto::getDifficulty)
                                    .orElse("EASY")
                    ));

                    totalScore += score;
                    evaluatedQuestions++;

                    details.add(detail);
                    results.add(dto);
                }

                // Cập nhật InterviewHistory với details và averageScore
                interviewHistory.setDetails(details);
                interviewHistory.setAverageScore(evaluatedQuestions > 0 ? totalScore / evaluatedQuestions : 0.0);
                interviewHistory.setEndedAt(LocalDateTime.now());
                interviewHistory.setAiOverallEvaluate(generateOverallEvaluation(results));

                interviewHistory = interviewHistoryRepository.save(interviewHistory); // Lưu lại để cascade lưu details

                // Gán thông tin InterviewHistory vào responseDto
                responseDto.setInterviewHistoryId(interviewHistory.getInterviewHistoryId());
                responseDto.setInterviewSessionId((long) session.getInterviewSessionId());
                responseDto.setTotalQuestion(interviewHistory.getTotalQuestion());
                responseDto.setAverageScore(interviewHistory.getAverageScore());
                responseDto.setAiOverallEvaluate(interviewHistory.getAiOverallEvaluate());
                responseDto.setStartedAt(interviewHistory.getStartedAt());
                responseDto.setEndedAt(interviewHistory.getEndedAt());
                responseDto.setResults(results);
            } else {
                throw new RuntimeException("Phản hồi JSON từ Gemini không phải mảng");
            }
        } catch (WebClientResponseException e) {
            EvaluationResponseDto errorDto = new EvaluationResponseDto();
            errorDto.setError("Lỗi khi gọi Gemini: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            results.add(errorDto);
            responseDto.setResults(results);
            logger.error("Lỗi từ Gemini: {}", e.getResponseBodyAsString());
        } catch (Exception e) {
            EvaluationResponseDto errorDto = new EvaluationResponseDto();
            errorDto.setError("Lỗi khi xử lý: " + e.getMessage());
            results.add(errorDto);
            responseDto.setResults(results);
            logger.error("Lỗi xử lý: ", e);
        }

        return responseDto;
    }

    private Double normalizeLevelToScore(String level) {
        if (level == null || level.trim().isEmpty()) {
            return 2.0; // Sai
        }
        String normalized = Normalizer.normalize(level, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^\\p{ASCII}]", "")
                .toUpperCase()
                .replace(" ", "_");
        switch (normalized) {
            case "DUNG":
            case "ĐUNG":
                return 10.0;
            case "DUNG_MOT_PHAN":
            case "ĐUNG_MOT_PHAN":
                return 7.0;
            case "GAN_DUNG":
            case "GẦN_DUNG":
                return 4.0;
            case "SAI":
            default:
                return 2.0;
        }
    }

    private String generateOverallEvaluation(List<EvaluationResponseDto> results) {
        long correctCount = results.stream().filter(r -> Double.parseDouble(r.getLevel()) == 10.0).count();
        long partialCount = results.stream().filter(r -> Double.parseDouble(r.getLevel()) == 7.0).count();
        long nearCorrectCount = results.stream().filter(r -> Double.parseDouble(r.getLevel()) == 4.0).count();
        long incorrectCount = results.stream().filter(r -> Double.parseDouble(r.getLevel()) == 2.0).count();

        return String.format(
                "Tổng quan: Ứng viên đạt điểm 10/10 cho %d/%d câu hỏi, 7/10 cho %d câu, 4/10 cho %d câu, và 2/10 cho %d câu. " +
                        "Đề xuất: Cần cải thiện kiến thức chuyên sâu về HTML, CSS, JavaScript, đặc biệt ở các câu hỏi độ khó trung bình và cao.",
                correctCount, results.size(), partialCount, nearCorrectCount, incorrectCount
        );
    }

    private String buildPrompt(InterviewSessionDto session, List<ChatMessageDto> chatHistory) {
        StringBuilder prompt = new StringBuilder();

        // Thêm thông tin buổi phỏng vấn
        prompt.append("Thông tin buổi phỏng vấn:\n")
                .append(String.format("ID: %d\n", session.getInterviewSessionId()))
                .append(String.format("Tiêu đề: %s\n", session.getTitle()))
                .append(String.format("Mô tả: %s\n", session.getDescription()))
                .append(String.format("Tổng số câu hỏi: %d\n", session.getTotalQuestion()))
                .append(String.format("Thời gian dự kiến: %s\n", session.getDurationEstimate()));

        // Thêm danh sách câu hỏi
        prompt.append("\nDanh sách câu hỏi:\n");
        List<QuestionDto> questions = session.getQuestions();
        for (int i = 0; i < questions.size(); i++) {
            QuestionDto q = questions.get(i);
            prompt.append(String.format("Câu hỏi %d (ID: %d, Độ khó: %s):\n", i + 1, q.getQuestionId(), q.getDifficulty()))
                    .append(String.format("  - Tiêu đề: %s\n", q.getTitle()))
                    .append(String.format("  - Nội dung: %s\n", q.getContent()))
                    .append(String.format("  - Trả lời mẫu 1: %s\n", q.getSuitableAnswer1()))
                    .append(String.format("  - Trả lời mẫu 2: %s\n", q.getSuitableAnswer2()))
                    .append(String.format("  - Tags: %s\n", String.join(", ", q.getTags())));
        }

        // Thêm lịch sử hội thoại
        prompt.append("\nLịch sử hội thoại:\n");
        for (ChatMessageDto message : chatHistory) {
            prompt.append(String.format("- Vai trò: %s\n", message.getRole()))
                    .append(String.format("  - Nội dung: %s\n", message.getContent()));
        }

        // Yêu cầu đánh giá
        prompt.append("\nYêu cầu:\n")
                .append("1. Phân tích 'Lịch sử hội thoại' để xác định câu trả lời của người dùng (vai trò 'user') cho từng câu hỏi trong 'Danh sách câu hỏi'. Gộp tất cả các câu trả lời liên quan (nếu có) cho mỗi câu hỏi, bỏ qua các tin nhắn không phải câu trả lời như yêu cầu gợi ý, 'bỏ qua', 'không biết', 'lặp lại câu hỏi', hoặc tương tự.\n")
                .append("2. Đánh giá từng câu hỏi dựa trên hai câu trả lời mẫu, sử dụng câu trả lời của người dùng đã gộp. Nếu không có câu trả lời hợp lệ cho câu hỏi, ghi là 'Không có câu trả lời' và đánh giá là 'Sai'.\n")
                .append("   - Mức độ chính xác: Sai, Gần đúng, Đúng một phần, Đúng.\n")
                .append("   - Kiến thức:\n")
                .append("     - Trả lời đúng hay chưa (đúng/sai/gần đúng/đúng một phần).\n")
                .append("     - Cải thiện kiến thức chỗ nào (nêu chi tiết các ý còn thiếu hoặc cần học thêm).\n")
                .append("     - Đã làm tốt chỗ nào rồi (nêu các ý trả lời đúng hoặc nổi bật, hoặc 'Không có' nếu không có câu trả lời).\n")
                .append("   - Khả năng giao tiếp:\n")
                .append("     - Trả lời có rõ ràng không (câu trả lời dễ hiểu, mạch lạc không, hoặc 'Không đánh giá được' nếu không có câu trả lời).\n")
                .append("     - Trả lời có ngắn gọn và súc tích không (có dài dòng hoặc lan man không, hoặc 'Không đánh giá được' nếu không có câu trả lời).\n")
                .append("     - Trả lời có sử dụng thuật ngữ chuyên môn phù hợp không (có dùng đúng từ ngữ kỹ thuật không, hoặc 'Không đánh giá được' nếu không có câu trả lời).\n")
                .append("   - Kết luận: Tổng kết ngắn gọn về chất lượng câu trả lời và gợi ý tổng quát để cải thiện.\n")
                .append("3. Trả về kết quả theo định dạng JSON hợp lệ (chỉ JSON, không Markdown, không giải thích):\n")
                .append("   [\n")
                .append("     {\n")
                .append("       \"questionId\": <ID>,\n")
                .append("       \"question\": \"<Nội dung câu hỏi>\",\n")
                .append("       \"userAnswer\": \"<Câu trả lời người dùng hoặc 'Không có câu trả lời' nếu không có>\",\n")
                .append("       \"level\": \"<Mức độ chính xác: Sai, Gần đúng, Đúng một phần, Đúng>\",\n")
                .append("       \"feedback\": {\n")
                .append("         \"knowledge\": {\n")
                .append("           \"correctness\": \"<Trả lời đúng hay chưa>\",\n")
                .append("           \"improvement\": \"<Cải thiện kiến thức chỗ nào>\",\n")
                .append("           \"strengths\": \"<Đã làm tốt chỗ nào rồi>\"\n")
                .append("         },\n")
                .append("         \"communication\": {\n")
                .append("           \"clarity\": \"<Trả lời có rõ ràng không>\",\n")
                .append("           \"conciseness\": \"<Trả lời có ngắn gọn và súc tích không>\",\n")
                .append("           \"terminology\": \"<Trả lời có sử dụng thuật ngữ chuyên môn phù hợp không>\"\n")
                .append("         },\n")
                .append("         \"conclusion\": \"<Kết luận ngắn gọn và gợi ý tổng quát>\"\n")
                .append("       }\n")
                .append("     }\n")
                .append("   ]\n")
                .append("4. Văn phong chuyên nghiệp, ngắn gọn, giống HR.\n");

        return prompt.toString();
    }
}