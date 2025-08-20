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
import java.util.LinkedHashSet;
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
        if (currentUser == null) throw new NotLoginException("Please log in to continue");

        EvaluationBatchResponseDto responseDto = new EvaluationBatchResponseDto();
        List<EvaluationResponseDto> results = new ArrayList<>();

        try {
            // Lấy thông tin buổi phỏng vấn
            InterviewSessionDto session = request.getInterviewSession();

            // Tạo prompt với chatHistory
            String prompt = buildPrompt(session, request.getChatHistory());
            logger.info("Prompt sent to Gemini: {}", prompt);

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

            logger.info("Response from Gemini: {}", response);

            // Parse phản hồi JSON từ Gemini
            JsonNode json = objectMapper.readTree(response);
            JsonNode resultText = json.at("/candidates/0/content/parts/0/text");
            if (resultText.isMissingNode()) {
                throw new RuntimeException("No text received from Gemini");
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
                throw new RuntimeException("InterviewSession not found with ID: " + session.getInterviewSessionId());
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
                        logger.warn("Question not found with ID: {}", questionId);
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
                throw new RuntimeException("Gemini JSON response is not an array");
            }
        } catch (WebClientResponseException e) {
            EvaluationResponseDto errorDto = new EvaluationResponseDto();
            errorDto.setError("Error while calling Gemini: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            results.add(errorDto);
            responseDto.setResults(results);
            logger.error("Error from Gemini: {}", e.getResponseBodyAsString());
        } catch (Exception e) {
            EvaluationResponseDto errorDto = new EvaluationResponseDto();
            errorDto.setError("Error while processing: " + e.getMessage());
            results.add(errorDto);
            responseDto.setResults(results);
            logger.error("Processing error: ", e);
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
        if (results.isEmpty()) {
            return "The candidate has not answered any questions and must complete the interview to be evaluated.";
        }

        double averageScore = results.stream()
                .mapToDouble(r -> Double.parseDouble(r.getLevel()))
                .average()
                .orElse(0.0);

        // Thu thập các điểm cần cải thiện
        List<String> improvements = new ArrayList<>();
        for (EvaluationResponseDto dto : results) {
            FeedbackDto feedback = dto.getFeedback();
            if (feedback != null && !feedback.getKnowledge().getImprovement().equals("None") && !feedback.getKnowledge().getImprovement().isEmpty()) {
                improvements.add(feedback.getKnowledge().getImprovement());
            }
        }

        String improvementSummary = improvements.isEmpty() ? "professional knowledge" : String.join(", ", new LinkedHashSet<>(improvements));

        // Xây dựng nhận xét ngắn gọn
        String performance = averageScore >= 7.0 ? "tốt" : averageScore >= 4.0 ? "good" : "fair";
        return String.format(
                "In this interview, the candidate performed %s, but needs to improve on %s to enhance the effectiveness of their answers.",
                performance, improvementSummary
        );
    }

    private String buildPrompt(InterviewSessionDto session, List<ChatMessageDto> chatHistory) {
        StringBuilder prompt = new StringBuilder();

        // Thêm thông tin buổi phỏng vấn
        prompt.append("Interview session information:\n")
                .append(String.format("ID: %d\n", session.getInterviewSessionId()))
                .append(String.format("Title: %s\n", session.getTitle()))
                .append(String.format("Description: %s\n", session.getDescription()))
                .append(String.format("Total questions: %d\n", session.getTotalQuestion()))
                .append(String.format("Estimated duration: %s\n", session.getDurationEstimate()));

        // Thêm danh sách câu hỏi
        prompt.append("\nQuestion list:\n");
        List<QuestionDto> questions = session.getQuestions();
        for (int i = 0; i < questions.size(); i++) {
            QuestionDto q = questions.get(i);
            prompt.append(String.format("Question %d (ID: %d, Difficulty: %s):\n", i + 1, q.getQuestionId(), q.getDifficulty()))
                    .append(String.format("  - Title: %s\n", q.getTitle()))
                    .append(String.format("  - Content: %s\n", q.getContent()))
                    .append(String.format("  - Sample Answer 1: %s\n", q.getSuitableAnswer1()))
                    .append(String.format("  - Sample Answer 2: %s\n", q.getSuitableAnswer2()))
                    .append(String.format("  - Tags: %s\n", String.join(", ", q.getTags())));
        }


        // Thêm lịch sử hội thoại
        prompt.append("\nConversation history:\n");
        for (ChatMessageDto message : chatHistory) {
            prompt.append(String.format("- Role: %s\n", message.getRole()))
                    .append(String.format("  - Content: %s\n", message.getContent()));
        }

        // Yêu cầu đánh giá
        prompt.append("\nInstructions:\n")
                .append("1. Analyze the 'Conversation history' to identify the user's answers (role 'user') for each question in the 'Question list'.\n")
                .append("   - Merge all related user answers for each question.\n")
                .append("   - Ignore irrelevant messages such as requests for hints, 'skip', 'don't know', 'repeat question', or similar.\n")
                .append("2. Evaluate each question based on the two sample answers, using the merged user answers.\n")
                .append("   - If there is no valid answer for a question, mark it as 'No answer' and the evaluation as 'Incorrect'.\n")
                .append("   - Accuracy levels: Incorrect, Partially correct, Almost correct, Correct.\n")
                .append("   - Knowledge:\n")
                .append("     - Was the answer correct (Incorrect/Partially correct/Almost correct/Correct)?\n")
                .append("     - What knowledge needs improvement (detail missing points or areas to study)?\n")
                .append("     - What was done well (correct or strong points in the answer, or 'None' if no answer)?\n")
                .append("   - Communication:\n")
                .append("     - Was the answer clear (easy to understand, coherent, or 'Not assessable' if no answer)?\n")
                .append("     - Was the answer concise (not too wordy, or 'Not assessable' if no answer)?\n")
                .append("     - Did the answer use appropriate technical terminology (or 'Not assessable' if no answer)?\n")
                .append("   - Conclusion: Provide a short summary of the answer quality and general improvement suggestions.\n")
                .append("3. Return the result in valid JSON format (JSON only, no Markdown, no explanation):\n")
                .append("   [\n")
                .append("     {\n")
                .append("       \"questionId\": <ID>,\n")
                .append("       \"question\": \"<Question content>\",\n")
                .append("       \"userAnswer\": \"<User's answer or 'No answer' if none>\",\n")
                .append("       \"level\": \"<Accuracy level: Incorrect, Almost correct, Partially correct, Correct>\",\n")
                .append("       \"feedback\": {\n")
                .append("         \"knowledge\": {\n")
                .append("           \"correctness\": \"<Was the answer correct?>\",\n")
                .append("           \"improvement\": \"<What knowledge needs improvement>\",\n")
                .append("           \"strengths\": \"<What was done well>\"\n")
                .append("         },\n")
                .append("         \"communication\": {\n")
                .append("           \"clarity\": \"<Was the answer clear?>\",\n")
                .append("           \"conciseness\": \"<Was the answer concise?>\",\n")
                .append("           \"terminology\": \"<Did the answer use proper technical terms?>\"\n")
                .append("         },\n")
                .append("         \"conclusion\": \"<Short conclusion and general suggestions>\"\n")
                .append("       }\n")
                .append("     }\n")
                .append("   ]\n")
                .append("4. Use a professional, concise HR-style tone.\n");


        return prompt.toString();
    }
}