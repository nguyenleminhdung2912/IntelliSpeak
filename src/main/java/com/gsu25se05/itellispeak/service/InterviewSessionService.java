package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.interview_session.InterviewSessionDTO;
import com.gsu25se05.itellispeak.entity.InterviewSession;
import com.gsu25se05.itellispeak.entity.Question;
import com.gsu25se05.itellispeak.repository.InterviewSessionRepository;
import com.gsu25se05.itellispeak.repository.QuestionRepository;
import com.gsu25se05.itellispeak.utils.mapper.InterviewSessionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class InterviewSessionService {
    private final InterviewSessionRepository interviewSessionRepository;
    private final QuestionRepository questionRepository;
    private final InterviewSessionMapper interviewSessionMapper;

    public InterviewSessionService(
            InterviewSessionRepository interviewSessionRepository,
            QuestionRepository questionRepository,
            InterviewSessionMapper interviewSessionMapper) {
        this.interviewSessionRepository = interviewSessionRepository;
        this.questionRepository = questionRepository;
        this.interviewSessionMapper = interviewSessionMapper;
    }

    @Transactional
    public InterviewSession save(InterviewSessionDTO dto) {
        Set<Question> questions = new HashSet<>();
        if (dto.getQuestionIds() != null && !dto.getQuestionIds().isEmpty()) {
            questions.addAll(questionRepository.findAllById(dto.getQuestionIds()));
        }

        InterviewSession entity = interviewSessionMapper.toEntity(dto, questions);
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

    public Set<Question> getQuestions(Long sessionId) {
        Optional<InterviewSession> sessionOpt = interviewSessionRepository.findById(sessionId);
        return sessionOpt.map(InterviewSession::getQuestions).orElse(new HashSet<>());
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
}
