package com.gsu25se05.itellispeak.utils.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsu25se05.itellispeak.dto.ai_evaluation.*;
import com.gsu25se05.itellispeak.entity.InterviewHistory;
import com.gsu25se05.itellispeak.entity.InterviewHistoryDetail;
import com.gsu25se05.itellispeak.entity.InterviewSession;
import com.gsu25se05.itellispeak.entity.Question;
import com.gsu25se05.itellispeak.entity.Tag;
import com.gsu25se05.itellispeak.repository.InterviewHistoryDetailRepository;
import com.gsu25se05.itellispeak.repository.InterviewSessionRepository;
import com.gsu25se05.itellispeak.repository.QuestionRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InterviewHistoryMapper {

    private final InterviewHistoryDetailRepository interviewHistoryDetailRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;

    public InterviewHistoryMapper(
            InterviewHistoryDetailRepository interviewHistoryDetailRepository,
            InterviewSessionRepository interviewSessionRepository,
            QuestionRepository questionRepository,
            ObjectMapper objectMapper) {
        this.interviewHistoryDetailRepository = interviewHistoryDetailRepository;
        this.interviewSessionRepository = interviewSessionRepository;
        this.questionRepository = questionRepository;
        this.objectMapper = objectMapper;
    }

    public EvaluationBatchResponseDto toEvaluationBatchResponseDto(InterviewHistory history) {
        EvaluationBatchResponseDto responseDto = new EvaluationBatchResponseDto();
        responseDto.setInterviewHistoryId(history.getInterviewHistoryId());
        responseDto.setInterviewSessionId(history.getInterviewSession().getInterviewSessionId());
        responseDto.setTotalQuestion(history.getTotalQuestion());
        responseDto.setAverageScore(history.getAverageScore());
        responseDto.setAiOverallEvaluate(history.getAiOverallEvaluate());
        responseDto.setStartedAt(history.getStartedAt());
        responseDto.setEndedAt(history.getEndedAt());

        // Lấy danh sách InterviewHistoryDetail
        List<InterviewHistoryDetail> details = interviewHistoryDetailRepository.findByInterviewHistory(history);
        List<EvaluationResponseDto> results = new ArrayList<>();

        // Lấy InterviewSession để tạo InterviewSessionDto
        InterviewSession session = interviewSessionRepository.findById(history.getInterviewSession().getInterviewSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy InterviewSession với ID: " + history.getInterviewSession().getInterviewSessionId()));

        InterviewSessionDto sessionDto = new InterviewSessionDto();
        sessionDto.setInterviewSessionId(session.getInterviewSessionId());
        sessionDto.setTitle(session.getTitle());
        sessionDto.setDescription(session.getDescription());
        sessionDto.setTotalQuestion(session.getTotalQuestion());
        sessionDto.setDurationEstimate(String.valueOf(session.getDurationEstimate()));

        // Lấy danh sách Question từ database
        List<Long> questionIds = details.stream()
                .map(detail -> detail.getQuestion().getQuestionId())
                .collect(Collectors.toList());
        List<Question> questions = questionRepository.findAllById(questionIds);
        List<QuestionDto> questionDtos = questions.stream()
                .map(q -> {
                    QuestionDto dto = new QuestionDto();
                    dto.setQuestionId(q.getQuestionId());
                    dto.setTitle(q.getTitle());
                    dto.setContent(q.getContent());
                    dto.setSuitableAnswer1(q.getSuitableAnswer1());
                    dto.setSuitableAnswer2(q.getSuitableAnswer2());
                    dto.setDifficulty(q.getDifficulty().name());
                    // Chuyển Set<Tag> thành List<String> (chỉ lấy tên tag)
                    List<String> tagNames = q.getTags().stream()
                            .map(Tag::getTitle) // giả sử trong Tag có getName()
                            .collect(Collectors.toList());
                    dto.setTags(tagNames);
                    return dto;
                })
                .collect(Collectors.toList());
        sessionDto.setQuestions(questionDtos);

        // Chuyển đổi InterviewHistoryDetail thành EvaluationResponseDto
        for (InterviewHistoryDetail detail : details) {
            EvaluationResponseDto result = new EvaluationResponseDto();
            result.setQuestionId(detail.getQuestion().getQuestionId());
            result.setQuestion(detail.getQuestion().getContent());
            result.setUserAnswer(detail.getAnsweredContent());
            result.setLevel(String.valueOf(detail.getScore()));
            result.setSuitableAnswer1(detail.getSuitableAnswer1());
            result.setSuitableAnswer2(detail.getSuitableAnswer2());

            try {
                JsonNode feedbackJson = objectMapper.readTree(detail.getAiEvaluatedContent());
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
                result.setFeedback(feedback);
            } catch (Exception e) {
                result.setError("Lỗi khi parse feedback: " + e.getMessage());
            }

            results.add(result);
        }

        responseDto.setResults(results);
        return responseDto;
    }

    public EvaluationBatchResponseDto toEvaluationBatchResponseForGetAllDto(InterviewHistory history) {
        EvaluationBatchResponseDto responseDto = new EvaluationBatchResponseDto();
        responseDto.setInterviewHistoryId(history.getInterviewHistoryId());
        responseDto.setInterviewSessionId(history.getInterviewSession().getInterviewSessionId());
        responseDto.setInterviewTitle(history.getInterviewSession().getTitle());
        responseDto.setTotalQuestion(history.getTotalQuestion());
        responseDto.setAverageScore(history.getAverageScore());
        responseDto.setAiOverallEvaluate(history.getAiOverallEvaluate());
        responseDto.setStartedAt(history.getStartedAt());
        responseDto.setEndedAt(history.getEndedAt());

        return responseDto;
    }
}
