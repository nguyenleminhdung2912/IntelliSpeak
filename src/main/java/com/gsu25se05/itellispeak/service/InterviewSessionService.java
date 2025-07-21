package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.interview_session.InterviewSessionDTO;
import com.gsu25se05.itellispeak.dto.interview_session.QuestionInfoDTO;
import com.gsu25se05.itellispeak.dto.interview_session.QuestionSelectionRequestDTO;
import com.gsu25se05.itellispeak.dto.interview_session.SessionWithQuestionsDTO;
import com.gsu25se05.itellispeak.entity.*;
import com.gsu25se05.itellispeak.repository.InterviewSessionRepository;
import com.gsu25se05.itellispeak.repository.QuestionRepository;
import com.gsu25se05.itellispeak.repository.TagRepository;
import com.gsu25se05.itellispeak.repository.TopicRepository;
import com.gsu25se05.itellispeak.utils.mapper.InterviewSessionMapper;
import com.gsu25se05.itellispeak.utils.mapper.QuestionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InterviewSessionService {
    private final InterviewSessionRepository interviewSessionRepository;
    private final QuestionRepository questionRepository;
    private final InterviewSessionMapper interviewSessionMapper;
    private final TagRepository tagRepository;
    private final TopicRepository topicRepository;
    private final QuestionMapper questionMapper;


    public InterviewSessionService(
            InterviewSessionRepository interviewSessionRepository,
            QuestionRepository questionRepository,
            InterviewSessionMapper interviewSessionMapper,
            TagRepository tagRepository,
            TopicRepository topicRepository,
            QuestionMapper questionMapper) {
        this.interviewSessionRepository = interviewSessionRepository;
        this.questionRepository = questionRepository;
        this.interviewSessionMapper = interviewSessionMapper;
        this.tagRepository = tagRepository;
        this.topicRepository = topicRepository;
        this.questionMapper = questionMapper;
    }

    @Transactional
    public InterviewSession save(InterviewSessionDTO dto) {
        Set<Question> questions = new HashSet<>();
        if (dto.getQuestionIds() != null && !dto.getQuestionIds().isEmpty()) {
            questions.addAll(questionRepository.findAllById(dto.getQuestionIds()));
        }
        Set<Tag> tags = new HashSet<>();
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            tags.addAll(tagRepository.findAllById(dto.getTagIds()));
        }
        Topic topic = null;
        if (dto.getTopicId() != null) {
            topic = topicRepository.findById(dto.getTopicId()).orElse(null);
        }
        InterviewSession entity = interviewSessionMapper.toEntity(dto, questions, tags, topic);
        return interviewSessionRepository.save(entity);
    }

    @Transactional
    public InterviewSession addQuestionToSession(Long sessionId, Long questionId) {
        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        session.getQuestions().add(question);
        return interviewSessionRepository.save(session);
    }

    @Transactional
    public InterviewSession addQuestionsToSession(Long sessionId, Set<Question> questions) {
        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Interview Session with ID " + sessionId + " not found"));

        // Filter out questions that already exist in the session to avoid duplicates
        Set<Question> existingQuestions = session.getQuestions();
        questions.removeAll(existingQuestions); // Remove questions already present

        session.getQuestions().addAll(questions); // Add only new questions
        return interviewSessionRepository.save(session);
    }

    @Transactional
    public Iterable<InterviewSession> getAllInterviewSession() {
        return interviewSessionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public SessionWithQuestionsDTO getRandomQuestions(QuestionSelectionRequestDTO request) {
        InterviewSession session = interviewSessionRepository.findById(request.getInterviewSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        List<QuestionInfoDTO> result = new ArrayList<>();
        result.addAll(randomQuestions(request, Difficulty.EASY, request.getEasyCount()));
        result.addAll(randomQuestions(request, Difficulty.MEDIUM, request.getMediumCount()));
        result.addAll(randomQuestions(request, Difficulty.HARD, request.getHardCount()));

        SessionWithQuestionsDTO dto = new SessionWithQuestionsDTO();
        dto.setInterviewSessionId(session.getInterviewSessionId());
        dto.setTitle(session.getTitle());
        dto.setDescription(session.getDescription());
        dto.setTotalQuestion(session.getTotalQuestion());
        dto.setDurationEstimate(session.getDurationEstimate());
        dto.setQuestions(result);
        return dto;
    }

    private List<QuestionInfoDTO> randomQuestions(QuestionSelectionRequestDTO request, Difficulty difficulty, int count) {
        if (count <= 0) return Collections.emptyList();
        List<Question> questions = questionRepository.findBySessionAndDifficultyAndTags(
                request.getInterviewSessionId(),
                difficulty,
                request.getTagIds() == null || request.getTagIds().isEmpty() ? null : request.getTagIds()
        );
        Collections.shuffle(questions);
        return questions.stream()
                .limit(count)
                .map(questionMapper::toInfoDTO)
                .collect(Collectors.toList());
    }
}
