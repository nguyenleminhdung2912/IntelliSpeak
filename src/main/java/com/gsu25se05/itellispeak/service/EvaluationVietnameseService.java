package com.gsu25se05.itellispeak.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsu25se05.itellispeak.dto.ai_evaluation.*;
import com.gsu25se05.itellispeak.entity.*;
import com.gsu25se05.itellispeak.exception.auth.NotLoginException;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EvaluationVietnameseService {

    private static final Logger logger = LoggerFactory.getLogger(EvaluationVietnameseService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final InterviewHistoryRepository interviewHistoryRepository;
    private final QuestionRepository questionRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final AccountUtils accountUtils;

    public EvaluationVietnameseService(
            @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent}") String geminiApiUrl,
            @Value("${gemini.api.key}") String apiKey,
            InterviewHistoryRepository interviewHistoryRepository,
            QuestionRepository questionRepository,
            InterviewSessionRepository interviewSessionRepository,
            AccountUtils accountUtils) {
        this.webClient = WebClient.builder()
                .baseUrl(geminiApiUrl + "?key=" + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.interviewHistoryRepository = interviewHistoryRepository;
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
            logger.info("Prompt gửi đến Gemini: {}", prompt);

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
                throw new RuntimeException("Không nhận được văn bản từ Gemini");
            }

            // Làm sạch và parse JSON từ text
            String cleanedJson = resultText.asText()
                    .replaceAll("(?i)```json", "")
                    .replaceAll("(?i)```", "")
                    .trim();
            JsonNode responseJson = objectMapper.readTree(cleanedJson);

            // Lấy danh sách kết quả và đánh giá tổng quan
            JsonNode resultsJson = responseJson.get("results");
            String overallEvaluation = responseJson.get("overallEvaluation").asText();

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
                    Double score = jsonObj.get("score").asDouble();
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
                        logger.warn("Không tìm thấy câu hỏi với ID: {}", questionId);
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

                    // Enforce Difficulty as HARD, MEDIUM, or EASY
                    String difficultyStr = session.getQuestions().stream()
                            .filter(q -> q.getQuestionId() == questionId)
                            .findFirst()
                            .map(QuestionDto::getDifficulty)
                            .orElse("EASY");
                    try {
                        // Handle Vietnamese difficulty values and convert to English enum
                        String normalizedDifficulty = switch (difficultyStr.toUpperCase()) {
                            case "DỄ" -> "EASY";
                            case "TRUNG BÌNH" -> "MEDIUM";
                            case "KHÓ" -> "HARD";
                            default -> difficultyStr.toUpperCase();
                        };
                        detail.setDifficulty(Difficulty.valueOf(normalizedDifficulty));
                    } catch (IllegalArgumentException e) {
                        logger.warn("Giá trị độ khó không hợp lệ '{}' cho câu hỏi ID {}. Mặc định là EASY.", difficultyStr, questionId);
                        detail.setDifficulty(Difficulty.EASY); // Default to EASY for invalid values
                    }

                    totalScore += score;
                    evaluatedQuestions++;

                    details.add(detail);
                    results.add(dto);
                }

                // Cập nhật InterviewHistory với details và averageScore
                interviewHistory.setDetails(details);
                interviewHistory.setAverageScore(evaluatedQuestions > 0 ? totalScore / evaluatedQuestions : 0.0);
                interviewHistory.setEndedAt(LocalDateTime.now());
                interviewHistory.setAiOverallEvaluate(overallEvaluation);

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
                throw new RuntimeException("Phản hồi JSON từ Gemini không phải là mảng kết quả");
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

    private String buildPrompt(InterviewSessionDto session, List<ChatMessageDto> chatHistory) {
        StringBuilder prompt = new StringBuilder();

        // Thêm thông tin buổi phỏng vấn
        prompt.append("Thông tin buổi phỏng vấn:\n")
                .append(String.format("ID: %d\n", session.getInterviewSessionId()))
                .append(String.format("Tiêu đề: %s\n", session.getTitle()))
                .append(String.format("Mô tả: %s\n", session.getDescription()))
                .append(String.format("Tổng số câu hỏi: %d\n", session.getTotalQuestion()))
                .append(String.format("Thời gian ước tính: %s\n", session.getDurationEstimate()));

        // Thêm danh sách câu hỏi
        prompt.append("\nDanh sách câu hỏi:\n");
        List<QuestionDto> questions = session.getQuestions();
        for (int i = 0; i < questions.size(); i++) {
            QuestionDto q = questions.get(i);
            prompt.append(String.format("Câu hỏi %d (ID: %d, Độ khó: %s):\n", i + 1, q.getQuestionId(), q.getDifficulty()))
                    .append(String.format("  - Tiêu đề: %s\n", q.getTitle()))
                    .append(String.format("  - Nội dung: %s\n", q.getContent()))
                    .append(String.format("  - Câu trả lời mẫu 1: %s\n", q.getSuitableAnswer1()))
                    .append(String.format("  - Câu trả lời mẫu 2: %s\n", q.getSuitableAnswer2()))
                    .append(String.format("  - Thẻ: %s\n", String.join(", ", q.getTags())))
                    .append("  - Lưu ý: Độ khó phải là một trong DỄ, TRUNG BÌNH, hoặc KHÓ.\n");
        }

        // Thêm lịch sử hội thoại
        prompt.append("\nLịch sử hội thoại:\n");
        for (ChatMessageDto message : chatHistory) {
            prompt.append(String.format("- Vai trò: %s\n", message.getRole()))
                    .append(String.format("  - Nội dung: %s\n", message.getContent()));
        }

        // Yêu cầu đánh giá
        prompt.append("\nHướng dẫn:\n")
                .append("1. Phản hồi **hoàn toàn bằng tiếng Việt**, bất kể nội dung lịch sử hội thoại hoặc câu hỏi có chứa tiếng Anh hoặc ngôn ngữ khác.\n")
                .append("2. Phân tích 'Lịch sử hội thoại' để xác định câu trả lời của người dùng (vai trò 'user') cho mỗi câu hỏi trong 'Danh sách câu hỏi'.\n")
                .append("   - Gộp tất cả câu trả lời liên quan của người dùng cho mỗi câu hỏi.\n")
                .append("   - Bỏ qua các tin nhắn không liên quan như yêu cầu gợi ý, 'bỏ qua', 'không biết', 'lặp lại câu hỏi', hoặc tương tự.\n")
                .append("3. Đánh giá mỗi câu hỏi dựa trên hai câu trả lời mẫu, sử dụng câu trả lời đã gộp của người dùng.\n")
                .append("   - Nếu không có câu trả lời hợp lệ cho một câu hỏi, ghi là 'Không có câu trả lời' và chấm 0 điểm.\n")
                .append("   - Chấm điểm từ 0 đến 10 dựa trên độ chính xác và chất lượng của câu trả lời (0 cho hoàn toàn sai hoặc không có câu trả lời, 10 cho hoàn hảo).\n")
                .append("   - Kiến thức:\n")
                .append("     - Câu trả lời có đúng không (mô tả mức độ đúng)?\n")
                .append("     - Kiến thức nào cần cải thiện (chi tiết các điểm còn thiếu hoặc lĩnh vực cần học)?\n")
                .append("     - Điểm mạnh là gì (các điểm đúng hoặc mạnh trong câu trả lời, hoặc 'Không có' nếu không có câu trả lời)?\n")
                .append("   - Giao tiếp:\n")
                .append("     - Câu trả lời có rõ ràng không (dễ hiểu, mạch lạc, hoặc 'Không đánh giá được' nếu không có câu trả lời)?\n")
                .append("     - Câu trả lời có ngắn gọn không (không quá dài dòng, hoặc 'Không đánh giá được' nếu không có câu trả lời)?\n")
                .append("     - Câu trả lời có sử dụng thuật ngữ kỹ thuật phù hợp không (hoặc 'Không đánh giá được' nếu không có câu trả lời)?\n")
                .append("   - Kết luận: Cung cấp tóm tắt ngắn gọn về chất lượng câu trả lời và gợi ý cải thiện chung.\n")
                .append("4. Cung cấp một đánh giá tổng quan ngắn gọn về hiệu suất của ứng viên bằng giọng văn chuyên nghiệp, ngắn gọn (1-2 câu).\n")
                .append("5. Trả về kết quả bằng định dạng JSON hợp lệ (chỉ JSON, không Markdown, không giải thích):\n")
                .append("   {\n")
                .append("     \"results\": [\n")
                .append("       {\n")
                .append("         \"questionId\": <ID>,\n")
                .append("         \"question\": \"<Nội dung câu hỏi>\",\n")
                .append("         \"userAnswer\": \"<Câu trả lời của người dùng hoặc 'Không có câu trả lời' nếu không có>\",\n")
                .append("         \"score\": <Điểm từ 0 đến 10>,\n")
                .append("         \"feedback\": {\n")
                .append("           \"knowledge\": {\n")
                .append("             \"correctness\": \"<Câu trả lời có đúng không?>\",\n")
                .append("             \"improvement\": \"<Kiến thức nào cần cải thiện>\",\n")
                .append("             \"strengths\": \"<Điểm mạnh là gì>\"\n")
                .append("           },\n")
                .append("           \"communication\": {\n")
                .append("             \"clarity\": \"<Câu trả lời có rõ ràng không?>\",\n")
                .append("             \"conciseness\": \"<Câu trả lời có ngắn gọn không?>\",\n")
                .append("             \"terminology\": \"<Câu trả lời có sử dụng thuật ngữ kỹ thuật phù hợp không?>\"\n")
                .append("           },\n")
                .append("           \"conclusion\": \"<Tóm tắt ngắn gọn và gợi ý cải thiện>\"\n")
                .append("         }\n")
                .append("       }\n")
                .append("     ],\n")
                .append("     \"overallEvaluation\": \"<Đánh giá tổng quan ngắn gọn về hiệu suất của ứng viên>\"\n")
                .append("   }\n")
                .append("6. Sử dụng giọng văn chuyên nghiệp, ngắn gọn, bằng tiếng Việt.\n");

        return prompt.toString();
    }
}