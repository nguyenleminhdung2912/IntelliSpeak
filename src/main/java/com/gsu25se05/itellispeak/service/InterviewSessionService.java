package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.interview_session.*;
import com.gsu25se05.itellispeak.dto.topic.TagSimpleDTO;
import com.gsu25se05.itellispeak.dto.topic.TopicWithTagsDTO;
import com.gsu25se05.itellispeak.entity.*;
import com.gsu25se05.itellispeak.repository.InterviewSessionRepository;
import com.gsu25se05.itellispeak.repository.QuestionRepository;
import com.gsu25se05.itellispeak.repository.TagRepository;
import com.gsu25se05.itellispeak.repository.TopicRepository;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import com.gsu25se05.itellispeak.utils.mapper.InterviewSessionMapper;
import com.gsu25se05.itellispeak.utils.mapper.QuestionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final AccountUtils accountUtils;

    public InterviewSessionService(
            InterviewSessionRepository interviewSessionRepository,
            QuestionRepository questionRepository,
            InterviewSessionMapper interviewSessionMapper,
            TagRepository tagRepository,
            TopicRepository topicRepository,
            QuestionMapper questionMapper,
            AccountUtils accountUtils) {
        this.interviewSessionRepository = interviewSessionRepository;
        this.questionRepository = questionRepository;
        this.interviewSessionMapper = interviewSessionMapper;
        this.tagRepository = tagRepository;
        this.topicRepository = topicRepository;
        this.questionMapper = questionMapper;
        this.accountUtils = accountUtils;
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
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser != null) {
            entity.setCreatedBy(currentUser);
        }
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

        Set<Question> existingQuestions = session.getQuestions();
        questions.removeAll(existingQuestions);
        session.getQuestions().addAll(questions);
        return interviewSessionRepository.save(session);
    }

    @Transactional
    public Iterable<InterviewSession> getAllInterviewSession() {
        return interviewSessionRepository.findAllBySourceNotOrSourceIsNull("RANDOM");
    }


    public List<InterviewSession> getAllSessionsCreatedByHR() {
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            throw new IllegalStateException("Vui lòng đăng nhập để tiếp tục");
        }

        if (currentUser.getRole() != User.Role.HR && currentUser.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Chỉ HR hoặc ADMIN mới có quyền xem các phiên phỏng vấn do mình tạo");
        }

        return interviewSessionRepository.findByCreatedBy(currentUser);
    }

    public InterviewSession getSessionCreatedByHR(Long id) {
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            throw new IllegalStateException("Vui lòng đăng nhập để tiếp tục");
        }

        if (currentUser.getRole() != User.Role.HR && currentUser.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Chỉ HR hoặc ADMIN mới có quyền xem phiên phỏng vấn do mình tạo");
        }

        return interviewSessionRepository.findById(id)
                .filter(s -> s.getCreatedBy() != null && s.getCreatedBy().equals(currentUser))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy Session với ID: " + id + " hoặc không thuộc quyền sở hữu"));
    }


    @Transactional
    public SessionWithQuestionsDTO getRandomQuestions(QuestionSelectionRequestDTO request) {
        int easyCount, mediumCount, hardCount;
        int total = request.getNumberOfQuestion();

        // Set ratio based on total
        if (total == 5) {
            easyCount = 2; mediumCount = 2; hardCount = 1;
        } else if (total == 10) {
            easyCount = 4; mediumCount = 4; hardCount = 2;
        } else if (total == 15) {
            easyCount = 7; mediumCount = 5; hardCount = 3;
        } else {
            easyCount = 2; mediumCount = 2; hardCount = 1;
        }

        List<QuestionInfoDTO> result = new ArrayList<>();
        result.addAll(randomQuestions(request, Difficulty.EASY, easyCount));
        result.addAll(randomQuestions(request, Difficulty.MEDIUM, mediumCount));
        result.addAll(randomQuestions(request, Difficulty.HARD, hardCount));
        Collections.shuffle(result);

        // Tạo InterviewSession tạm thời
        InterviewSession tempSession = new InterviewSession();
        tempSession.setTitle("Buổi phỏng vấn ngẫu nhiên - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
        tempSession.setDescription("Tạo tự động từ getRandomQuestions");
        tempSession.setTotalQuestion(total);
        tempSession.setDifficulty(Difficulty.MEDIUM); // Có thể điều chỉnh logic
        tempSession.setDurationEstimate(Duration.ofMinutes(total * 5L)); // Ước tính 5 phút/câu
        tempSession.setCreateAt(LocalDateTime.now());
        tempSession.setIsDeleted(false);
        tempSession.setSource("RANDOM"); // Đặt nguồn là RANDOM

        // Gán Topic nếu có topicId
        if (request.getTopicId() != null) {
            Topic topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new RuntimeException("Topic not found"));
            tempSession.setTopic(topic);
        }

        // Gán Tags nếu có
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            tempSession.setTags(tags);
        }

        // Gán Questions
        Set<Question> questions = result.stream()
                .map(dto -> questionRepository.findById(dto.getQuestionId())
                        .orElseThrow(() -> new RuntimeException("Question not found")))
                .collect(Collectors.toSet());
        tempSession.setQuestions(questions);

        // Lưu InterviewSession
        tempSession = interviewSessionRepository.save(tempSession);

        // Tạo DTO trả về
        SessionWithQuestionsDTO dto = new SessionWithQuestionsDTO();
        dto.setInterviewSessionId(tempSession.getInterviewSessionId());
        dto.setTitle(tempSession.getTitle());
        dto.setDescription(tempSession.getDescription());
        dto.setTotalQuestion(total);
        dto.setDurationEstimate(tempSession.getDurationEstimate());
        dto.setQuestions(result);
        return dto;
    }

    @Transactional
    public List<QuestionInfoDTO> randomQuestions(QuestionSelectionRequestDTO request, Difficulty difficulty, int count) {
        if (count <= 0) return Collections.emptyList();

        List<Question> questions = questionRepository.findByTagsAndDifficultyAndIsDeletedFalse(
                request.getTagIds() == null || request.getTagIds().isEmpty() ? null : request.getTagIds(),
                difficulty
        );
        Collections.shuffle(questions);
        return questions.stream()
                .limit(count)
                .map(questionMapper::toInfoDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public InterviewByTopicDTO getInterviewSessionByTopicId(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        List<InterviewSession> interviewSessionList = interviewSessionRepository.findByTopic_TopicIdAndIsDeletedFalse(topicId);

        List<InterviewSessionDTO> sessionDTOs = interviewSessionList.stream()
                .map(interviewSessionMapper::toDTO)
                .collect(Collectors.toList());

        return InterviewByTopicDTO.builder()
                .title(topic.getTitle())
                .description(topic.getDescription())
                .longDescription(topic.getLongDescription())
                .interviewSessionDTOs(sessionDTOs)
                .build();
    }

    @Transactional
    public List<TopicWithTagsDTO> getAllTopicsWithTags() {
        List<Topic> topics = topicRepository.findAllByIsDeletedFalse();
        return topics.stream().map(topic -> {
            List<TagSimpleDTO> tagDTOs = topic.getTags() == null ? List.of() :
                    topic.getTags().stream()
                            .filter(tag -> !tag.getIsDeleted())
                            .map(tag -> new TagSimpleDTO(tag.getTagId(), tag.getTitle()))
                            .collect(Collectors.toList());
            return new TopicWithTagsDTO(topic.getTopicId(), topic.getTitle(), tagDTOs);
        }).collect(Collectors.toList());
    }

    @Transactional
    public String updateInterviewSessionThumbnail(Long id, String thumbnailURL) {
        InterviewSession existingInterviewSession = interviewSessionRepository.findById(id).orElse(null);
        if (existingInterviewSession != null) {
            existingInterviewSession.setInterviewSessionThumbnail(thumbnailURL);
            existingInterviewSession.setUpdateAt(LocalDateTime.now());
            try {
                interviewSessionRepository.save(existingInterviewSession);
            } catch (Exception e) {
                return "Có lỗi rồi bé ơi";
            }
            return "Lưu thumbnail thành công rồi nha";
        }
        return "Không tìm thấy InterviewSession";
    }

    @Transactional(readOnly = true)
    public SessionWithQuestionsDTO getRandomQuestionsBySession(Long sessionId) {
        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("InterviewSession not found"));
        int total = session.getTotalQuestion();
        int easyCount = Math.round(total * 5f / 10f);
        int mediumCount = Math.round(total * 3f / 10f);
        int hardCount = total - easyCount - mediumCount;

        List<QuestionInfoDTO> result = new ArrayList<>();
        result.addAll(randomQuestionsBySession(session, Difficulty.EASY, easyCount));
        result.addAll(randomQuestionsBySession(session, Difficulty.MEDIUM, mediumCount));
        result.addAll(randomQuestionsBySession(session, Difficulty.HARD, hardCount));
        Collections.shuffle(result);

        SessionWithQuestionsDTO dto = new SessionWithQuestionsDTO();
        dto.setInterviewSessionId(sessionId);
        dto.setTitle(session.getTitle());
        dto.setDescription(session.getDescription());
        dto.setTotalQuestion(total);
        dto.setDurationEstimate(session.getDurationEstimate());
        dto.setQuestions(result);
        return dto;
    }

    @Transactional
    public List<QuestionInfoDTO> randomQuestionsBySession(InterviewSession session, Difficulty difficulty, int count) {
        if (count <= 0) return Collections.emptyList();
        List<Question> questions = session.getQuestions().stream()
                .filter(q -> q.getDifficulty() == difficulty && !q.getIs_deleted())
                .collect(Collectors.toList());
        Collections.shuffle(questions);
        return questions.stream()
                .limit(count)
                .map(questionMapper::toInfoDTO)
                .collect(Collectors.toList());
    }
}
