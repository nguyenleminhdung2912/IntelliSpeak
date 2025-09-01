package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.ai_evaluation.EvaluationBatchResponseDto;
import com.gsu25se05.itellispeak.dto.interview_session.*;
import com.gsu25se05.itellispeak.dto.topic.TagSimpleDTO;
import com.gsu25se05.itellispeak.dto.topic.TopicWithTagsDTO;
import com.gsu25se05.itellispeak.entity.*;
import com.gsu25se05.itellispeak.exception.ErrorCode;
import com.gsu25se05.itellispeak.exception.ForbiddenException;
import com.gsu25se05.itellispeak.exception.auth.AuthAppException;
import com.gsu25se05.itellispeak.exception.auth.NotFoundException;
import com.gsu25se05.itellispeak.exception.auth.NotLoginException;
import com.gsu25se05.itellispeak.repository.*;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import com.gsu25se05.itellispeak.utils.TranslationUtil;
import com.gsu25se05.itellispeak.utils.mapper.InterviewSessionMapper;
import com.gsu25se05.itellispeak.utils.mapper.QuestionMapper;
import jakarta.persistence.EntityNotFoundException;
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
    private final UserUsageRepository userUsageRepository;
    private final UserRepository userRepository;
    private final TranslationUtil translationUtil;

    public InterviewSessionService(
            InterviewSessionRepository interviewSessionRepository,
            QuestionRepository questionRepository,
            InterviewSessionMapper interviewSessionMapper,
            TagRepository tagRepository,
            TopicRepository topicRepository,
            QuestionMapper questionMapper,
            AccountUtils accountUtils, UserUsageRepository userUsageRepository, UserRepository userRepository, TranslationUtil translationUtil) {
        this.interviewSessionRepository = interviewSessionRepository;
        this.questionRepository = questionRepository;
        this.interviewSessionMapper = interviewSessionMapper;
        this.tagRepository = tagRepository;
        this.topicRepository = topicRepository;
        this.questionMapper = questionMapper;
        this.accountUtils = accountUtils;
        this.userUsageRepository = userUsageRepository;
        this.userRepository = userRepository;
        this.translationUtil = translationUtil;
    }

    @Transactional
    public InterviewSession save(InterviewSessionDTO dto) {

        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            // 401
            throw new NotLoginException("Please log in to continue");
        }

        String roleName = (currentUser.getRole() != null) ? currentUser.getRole().name() : null;
        boolean isAdmin = "ADMIN".equalsIgnoreCase(roleName);
        boolean isHrApproved = currentUser.getHr() != null
                && currentUser.getHr().getStatus() == HRStatus.APPROVED;

        if (!(isAdmin || isHrApproved)) {
            // 403
            throw new ForbiddenException("Only ADMIN or APPROVED HR can create interview sessions");
        }

        Set<Question> questions = (dto.getQuestionIds() == null || dto.getQuestionIds().isEmpty())
                ? java.util.Collections.emptySet()
                : new java.util.HashSet<>(questionRepository.findAllById(dto.getQuestionIds()));

        Set<Tag> tags = (dto.getTagIds() == null || dto.getTagIds().isEmpty())
                ? java.util.Collections.emptySet()
                : new java.util.HashSet<>(tagRepository.findAllById(dto.getTagIds()));

        Topic topic = (dto.getTopicId() == null) ? null
                : topicRepository.findById(dto.getTopicId())
                .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + dto.getTopicId()));

        InterviewSession entity;
        if (isHrApproved) {
            entity = interviewSessionMapper.toEntityWithCompany(
                    dto, questions, tags, topic, currentUser.getHr().getCompany()
            );
            entity.setSource("HR");
        } else {
            entity = interviewSessionMapper.toEntity(dto, questions, tags, topic);
            entity.setSource("ADMIN");
        }

        entity.setCreatedBy(currentUser);
        return interviewSessionRepository.save(entity);
    }

    @Transactional
    public void delete(Long interviewSessionId) {
        if (interviewSessionId == null) {
            throw new IllegalArgumentException("Interview session ID must not be null");
        }

        InterviewSession session = interviewSessionRepository.findById(interviewSessionId)
                .orElseThrow(() -> new AuthAppException(ErrorCode.INTERVIEW_SESSION_NOT_FOUND));

        if (Boolean.TRUE.equals(session.getIsDeleted())) {
            throw new IllegalStateException("Interview session with id " + interviewSessionId + " is already deleted");
        }

        session.setIsDeleted(true);
        session.setUpdateAt(LocalDateTime.now());

        interviewSessionRepository.save(session);
    }

    @Transactional
    public InterviewSession updateInterviewSession(Long id, UpdateInterviewSessionRequestDTO request) {
        InterviewSession session = interviewSessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Interview session not found"));

        // update topic
        if (request.getTopicId() != null) {
            Topic topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new EntityNotFoundException("Topic not found"));
            session.setTopic(topic);
        }

        // update title
        if (request.getTitle() != null) {
            session.setTitle(request.getTitle());
        }

        // update description
        if (request.getDescription() != null) {
            session.setDescription(request.getDescription());
        }

        if (request.getDifficulty() != null) {
            session.setDifficulty(request.getDifficulty());
        }

        // update tags (remove duplicates)
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            // loại bỏ trùng bằng Set
            Set<Long> uniqueTagIds = new HashSet<>(request.getTagIds());

            List<Tag> tagsFromDb = tagRepository.findAllById(uniqueTagIds);

            if (tagsFromDb.size() != uniqueTagIds.size()) {
                throw new EntityNotFoundException("One or more tags not found");
            }

            session.setTags(new HashSet<>(tagsFromDb));
        }

        session.setUpdateAt(LocalDateTime.now());
        return interviewSessionRepository.save(session);
    }

    @Transactional
    public InterviewSession addQuestionToSession(Long sessionId, Long questionId) {
        // 1. Get current user for authorization
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            throw new AuthAppException(ErrorCode.NOT_LOGIN);
        }

        // 2. Find entities
        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AuthAppException(ErrorCode.INTERVIEW_SESSION_NOT_FOUND));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new AuthAppException(ErrorCode.QUESTION_NOT_FOUND));

        // 3. Authorization Check: Only owner or ADMIN can modify
        boolean isOwner = session.getCreatedBy() != null && session.getCreatedBy().getUserId().equals(currentUser.getUserId());
        boolean isAdmin = currentUser.getRole() == User.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AuthAppException(ErrorCode.ACTION_FORBIDDEN);
        }

        // 4. Business Rule: If session is for a company, question must be public or from the same company
        Company sessionCompany = session.getCompany();
        if (sessionCompany != null && question.getCompany() != null && !question.getCompany().getCompanyId().equals(sessionCompany.getCompanyId())) {
            throw new AuthAppException(ErrorCode.ACTION_FORBIDDEN);
        }

        // 5. Add question and update total only if it's a new addition
        boolean isAdded = session.getQuestions().add(question);
        if (isAdded) {
            session.setTotalQuestion(session.getQuestions().size());
            return interviewSessionRepository.save(session);
        }

        return session; // Return session without saving if the question was already present
    }

    @Transactional
    public InterviewSession addQuestionsToSession(Long sessionId, AddQuestionsRequestDTO request) {
        // 1. Get current user for authorization
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            throw new AuthAppException(ErrorCode.NOT_LOGIN);
        }

        // 2. Find session and perform authorization check
        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AuthAppException(ErrorCode.INTERVIEW_SESSION_NOT_FOUND));

        boolean isOwner = session.getCreatedBy() != null && session.getCreatedBy().getUserId().equals(currentUser.getUserId());
        boolean isAdmin = currentUser.getRole() == User.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AuthAppException(ErrorCode.ACTION_FORBIDDEN);
        }

        // 3. Handle empty request
        if (request.getQuestionIds() == null || request.getQuestionIds().isEmpty()) {
            // Nothing to add, just return the session
            return session;
        }

        // 4. Find questions and validate their existence
        List<Question> questionsToAdd = questionRepository.findAllById(request.getQuestionIds());
        if (questionsToAdd.size() != request.getQuestionIds().size()) {
            throw new AuthAppException(ErrorCode.INVALID_INPUT);
        }

        // 5. Business Rule: Check if questions can be added to the session's company
        Company sessionCompany = session.getCompany();
        if (sessionCompany != null) {
            for (Question q : questionsToAdd) {
                if (q.getCompany() != null && !q.getCompany().getCompanyId().equals(sessionCompany.getCompanyId())) {
                    throw new AuthAppException(ErrorCode.ACTION_FORBIDDEN);
                }
            }
        }

        // 6. Add questions (Set handles duplicates) and update total
        session.getQuestions().addAll(questionsToAdd);
        session.setTotalQuestion(session.getQuestions().size());

        // 7. Save and return
        return interviewSessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public List<InterviewSession> getAllInterviewSession() {
        return interviewSessionRepository.findAllVisibleFetchAll("RANDOM");
    }

    @Transactional(readOnly = true)
    public InterviewSession getInterviewSessionById(Long id) {
        return interviewSessionRepository.findVisibleById(id, "RANDOM")
                .orElseThrow(() -> new NotFoundException("Interview session not found or unavailable"));
    }

    @Transactional
    public List<InterviewSession> getAllSessionsCreatedByHR() {
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            throw new IllegalStateException("Please log in to continue");
        }

        if (currentUser.getRole() != User.Role.HR && currentUser.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Only HR or ADMIN are allowed to view the interview sessions they created");
        }

        return interviewSessionRepository.findByIsDeletedFalseAndCreatedBy(currentUser);
    }

    @Transactional
    public InterviewSession getSessionCreatedByHR(Long id) {
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            throw new IllegalStateException("Please log in to continue");
        }

        if (currentUser.getRole() != User.Role.HR && currentUser.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Only HR or ADMIN are allowed to view the interview sessions they created");
        }

        return interviewSessionRepository.findById(id)
                .filter(s -> s.getCreatedBy() != null && s.getCreatedBy().equals(currentUser))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Session with ID: " + id + " not found or not owned by the current user"));
    }

    @Transactional
    public SessionWithQuestionsDTO getRandomQuestions(QuestionSelectionRequestDTO request) {

        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            throw new NotLoginException("Please log in to continue");
        }

        if (currentUser.getUserUsage().getInterviewUsed() >= currentUser.getAPackage().getInterviewCount()) {
            throw new AuthAppException(ErrorCode.OUT_OF_INTERVIEW_COUNT);
        }

        int easyCount, mediumCount, hardCount;
        int total = request.getNumberOfQuestion();

        // Set ratio based on total
        if (total == 3) {
            easyCount = 1;
            mediumCount = 2;
            hardCount = 0;
        } else if (total == 5) {
            easyCount = 2;
            mediumCount = 2;
            hardCount = 1;
        } else if (total == 10) {
            easyCount = 4;
            mediumCount = 4;
            hardCount = 2;
        } else if (total == 15) {
            easyCount = 7;
            mediumCount = 5;
            hardCount = 3;
        } else {
            easyCount = 2;
            mediumCount = 2;
            hardCount = 1;
        }

        List<QuestionInfoDTO> result = new ArrayList<>();
        result.addAll(randomQuestions(request, Difficulty.EASY, easyCount));
        result.addAll(randomQuestions(request, Difficulty.MEDIUM, mediumCount));
        result.addAll(randomQuestions(request, Difficulty.HARD, hardCount));
        Collections.shuffle(result);

        // Tạo InterviewSession tạm thời
        InterviewSession tempSession = new InterviewSession();
        tempSession.setTitle("Random Interview Session - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
        tempSession.setDescription("Automatically generated from user");
        tempSession.setTotalQuestion(total);
        tempSession.setDifficulty(Difficulty.MEDIUM); // Có thể điều chỉnh logic
        tempSession.setDurationEstimate(Duration.ofMinutes(total * 5L)); // Ước tính 5 phút/câu
        tempSession.setCreateAt(LocalDateTime.now());
        tempSession.setIsDeleted(false);
        tempSession.setSource("RANDOM"); // Đặt nguồn là RANDOM
        tempSession.setCreatedBy(currentUser);

        // Gán Topic nếu có topicId
        if (request.getTopicId() != null) {
            Topic topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new RuntimeException("Topic not found"));
            tempSession.setTopic(topic);
            tempSession.setTitle(topic.getTitle() + " " + tempSession.getTitle());
        }

        Set<Tag> tags = new HashSet<>();
        // Gán Tags nếu có
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
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

        List<TagSimpleDTO> tagSimpleDTOS = new ArrayList<>();
        for (Tag tag : tags) {
            TagSimpleDTO tagSimpleDTO = new TagSimpleDTO();
            tagSimpleDTO.setTitle(tag.getTitle());
            tagSimpleDTO.setTagId(tag.getTagId());
            tagSimpleDTOS.add(tagSimpleDTO);
        }

        // Tạo DTO trả về
        SessionWithQuestionsDTO dto = new SessionWithQuestionsDTO();
        dto.setInterviewSessionId(tempSession.getInterviewSessionId());
        dto.setTitle(tempSession.getTitle());
        dto.setCompanyId(
                tempSession.getCompany() != null ? tempSession.getCompany().getCompanyId() : null
        );
        dto.setTags(tagSimpleDTOS);
        dto.setDescription(tempSession.getDescription());
        dto.setTotalQuestion(total);
        dto.setDurationEstimate(tempSession.getDurationEstimate());
        dto.setQuestions(result);

        userRepository.save(currentUser);
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

        List<InterviewSession> interviewSessionList = interviewSessionRepository.findByTopic_TopicIdAndIsDeletedFalse(topicId)
                .stream()
                .filter(session -> session
                        .getSource() == null || "ADMIN".equals(session.getSource()))
                .collect(Collectors.toList());

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
    public String updateInterviewSessionThumbnail(Long id, ThumbnailRequestDTO thumbnailURL) {
        InterviewSession existingInterviewSession = interviewSessionRepository.findById(id).orElse(null);
        if (existingInterviewSession != null) {
            existingInterviewSession.setInterviewSessionThumbnail(thumbnailURL.getThumbnailURL());
            existingInterviewSession.setUpdateAt(LocalDateTime.now());
            try {
                interviewSessionRepository.save(existingInterviewSession);
            } catch (Exception e) {
                return "An error has occurred";
            }
            return "Thumbnail saved successfully";
        }
        return "InterviewSession not found";
    }

    @Transactional(readOnly = true)
    public SessionWithQuestionsDTO getRandomQuestionsBySession(Long sessionId) {

        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            throw new NotLoginException("Please log in to continue");
        }

        if (currentUser.getUserUsage().getInterviewUsed() >= currentUser.getAPackage().getInterviewCount()) {
            throw new AuthAppException(ErrorCode.OUT_OF_INTERVIEW_COUNT);
        }

        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("InterviewSession not found"));
        if (session.getIsDeleted() == true) {
            throw new AuthAppException(ErrorCode.INTERVIEW_SESSION_WERE_DELETED);
        }
        int total = session.getTotalQuestion();
        int easyCount = Math.round(total * 5f / 10f);
        int mediumCount = Math.round(total * 3f / 10f);
        int hardCount = total - easyCount - mediumCount;

        List<QuestionInfoDTO> result = new ArrayList<>();

        if (session.getCompany() != null) {
            Set<Question> questionList = session.getQuestions();
            result.addAll(questionList.stream().map(questionMapper::toInfoDTO).collect(Collectors.toSet()));

            SessionWithQuestionsDTO dto = new SessionWithQuestionsDTO();
            dto.setInterviewSessionId(sessionId);
            dto.setTitle(session.getTitle());
            dto.setCompanyId(
                    session.getCompany() != null ? session.getCompany().getCompanyId() : null
            );
            dto.setDescription(session.getDescription());
            dto.setTotalQuestion(total);
            dto.setDurationEstimate(session.getDurationEstimate());
            dto.setQuestions(result);

            currentUser.getUserUsage().setInterviewUsed(currentUser.getUserUsage().getInterviewUsed() + 1);
            userRepository.save(currentUser);
            userUsageRepository.save(currentUser.getUserUsage());

            return dto;
        }

        result.addAll(randomQuestionsBySession(session, Difficulty.EASY, easyCount));
        result.addAll(randomQuestionsBySession(session, Difficulty.MEDIUM, mediumCount));
        result.addAll(randomQuestionsBySession(session, Difficulty.HARD, hardCount));
        Collections.shuffle(result);

        SessionWithQuestionsDTO dto = new SessionWithQuestionsDTO();
        dto.setInterviewSessionId(sessionId);
        dto.setTitle(session.getTitle());
        dto.setCompanyId(
                session.getCompany() != null ? session.getCompany().getCompanyId() : null
        );
        dto.setDescription(session.getDescription());
        dto.setTotalQuestion(total);
        dto.setDurationEstimate(session.getDurationEstimate());
        dto.setQuestions(result);

        currentUser.getUserUsage().setInterviewUsed(currentUser.getUserUsage().getInterviewUsed() + 1);
        userRepository.save(currentUser);
        userUsageRepository.save(currentUser.getUserUsage());

        return dto;
    }

    @Transactional
    public List<QuestionInfoDTO> randomQuestionsBySession(InterviewSession session, Difficulty difficulty, int count) {
        if (count <= 0) return Collections.emptyList();
        List<Question> questions = session.getQuestions().stream()
                .filter(q -> q.getDifficulty() == difficulty && !q.getIsDeleted())
                .collect(Collectors.toList());
        Collections.shuffle(questions);
        return questions.stream()
                .limit(count)
                .map(questionMapper::toInfoDTO)
                .collect(Collectors.toList());
    }

    public List<EvaluationBatchResponseDto> getAllRandomGeneratedQuestionsSession() {
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            throw new NotLoginException("Please log in to continue");
        }

        List<InterviewSession> interviewSessionList = interviewSessionRepository.findByIsDeletedFalseAndCreatedBy(currentUser);
        return interviewSessionList.stream()
                .map(interviewSessionMapper::toEvaluationBatchResponseForGetAllDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public InterviewSession saveFromHR(InterviewSessionDTO dto) {

        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            throw new NotLoginException("Please log in to continue");
        }
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

        entity.setCreatedBy(currentUser);
        entity.setSource("HR");

        return interviewSessionRepository.save(entity);
    }

    @Transactional
    public SessionWithQuestionsDTO getRandomQuestionsBySessionVietnamese(Long interviewSessionId) {
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            throw new NotLoginException("Please log in to continue");
        }

        if (currentUser.getUserUsage().getInterviewUsed() >= currentUser.getAPackage().getInterviewCount()) {
            throw new AuthAppException(ErrorCode.OUT_OF_INTERVIEW_COUNT);
        }

        InterviewSession session = interviewSessionRepository.findById(interviewSessionId)
                .orElseThrow(() -> new RuntimeException("InterviewSession not found"));
        int total = session.getTotalQuestion();
        int easyCount = Math.round(total * 5f / 10f);
        int mediumCount = Math.round(total * 3f / 10f);
        int hardCount = total - easyCount - mediumCount;

        List<QuestionInfoDTO> result = new ArrayList<>();

        if (session.getCompany() != null) {
            Set<Question> questionList = session.getQuestions();
            result.addAll(questionList.stream().map(questionMapper::toInfoDTO).collect(Collectors.toSet()));

            // Dịch các trường trong QuestionInfoDTO bằng batch translation
            List<String> titles = result.stream().map(QuestionInfoDTO::getTitle).collect(Collectors.toList());
            List<String> contents = result.stream().map(QuestionInfoDTO::getContent).collect(Collectors.toList());
            List<String> answers1 = result.stream().map(QuestionInfoDTO::getSuitableAnswer1).collect(Collectors.toList());
            List<String> answers2 = result.stream().map(QuestionInfoDTO::getSuitableAnswer2).collect(Collectors.toList());
            List<String> difficulties = result.stream().map(QuestionInfoDTO::getDifficulty).collect(Collectors.toList());

            List<String> translatedTitles = translationUtil.translateBatchToVietnamese(titles);
            List<String> translatedContents = translationUtil.translateBatchToVietnamese(contents);
            List<String> translatedAnswers1 = translationUtil.translateBatchToVietnamese(answers1);
            List<String> translatedAnswers2 = translationUtil.translateBatchToVietnamese(answers2);
            List<String> translatedDifficulties = translationUtil.translateBatchToVietnamese(difficulties);

            for (int i = 0; i < result.size(); i++) {
                result.get(i).setTitle(translatedTitles.get(i));
                result.get(i).setContent(translatedContents.get(i));
                result.get(i).setSuitableAnswer1(translatedAnswers1.get(i));
                result.get(i).setSuitableAnswer2(translatedAnswers2.get(i));
                result.get(i).setDifficulty(translatedDifficulties.get(i));
            }

            SessionWithQuestionsDTO dto = new SessionWithQuestionsDTO();
            dto.setInterviewSessionId(interviewSessionId);
            // Dịch title và description
            List<String> sessionTexts = Arrays.asList(session.getTitle(), session.getDescription());
            List<String> translatedSessionTexts = translationUtil.translateBatchToVietnamese(sessionTexts);
            dto.setTitle(translatedSessionTexts.get(0));
            dto.setDescription(translatedSessionTexts.get(1));
            dto.setCompanyId(
                    session.getCompany() != null ? session.getCompany().getCompanyId() : null
            );
            dto.setTotalQuestion(total);
            dto.setDurationEstimate(session.getDurationEstimate());
            dto.setQuestions(result);
            // Giữ nguyên tags
            dto.setTags(session.getTags().stream()
                    .map(tag -> new TagSimpleDTO(tag.getTagId(), tag.getTitle()))
                    .collect(Collectors.toList()));

            currentUser.getUserUsage().setInterviewUsed(currentUser.getUserUsage().getInterviewUsed() + 1);
            userRepository.save(currentUser);
            userUsageRepository.save(currentUser.getUserUsage());
        }

        result.addAll(randomQuestionsBySession(session, Difficulty.EASY, easyCount));
        result.addAll(randomQuestionsBySession(session, Difficulty.MEDIUM, mediumCount));
        result.addAll(randomQuestionsBySession(session, Difficulty.HARD, hardCount));
        Collections.shuffle(result);

        // Dịch các trường trong QuestionInfoDTO bằng batch translation
        List<String> titles = result.stream().map(QuestionInfoDTO::getTitle).collect(Collectors.toList());
        List<String> contents = result.stream().map(QuestionInfoDTO::getContent).collect(Collectors.toList());
        List<String> answers1 = result.stream().map(QuestionInfoDTO::getSuitableAnswer1).collect(Collectors.toList());
        List<String> answers2 = result.stream().map(QuestionInfoDTO::getSuitableAnswer2).collect(Collectors.toList());
        List<String> difficulties = result.stream().map(QuestionInfoDTO::getDifficulty).collect(Collectors.toList());

        List<String> translatedTitles = translationUtil.translateBatchToVietnamese(titles);
        List<String> translatedContents = translationUtil.translateBatchToVietnamese(contents);
        List<String> translatedAnswers1 = translationUtil.translateBatchToVietnamese(answers1);
        List<String> translatedAnswers2 = translationUtil.translateBatchToVietnamese(answers2);
        List<String> translatedDifficulties = translationUtil.translateBatchToVietnamese(difficulties);

        for (int i = 0; i < result.size(); i++) {
            result.get(i).setTitle(translatedTitles.get(i));
            result.get(i).setContent(translatedContents.get(i));
            result.get(i).setSuitableAnswer1(translatedAnswers1.get(i));
            result.get(i).setSuitableAnswer2(translatedAnswers2.get(i));
            result.get(i).setDifficulty(translatedDifficulties.get(i));
        }

        SessionWithQuestionsDTO dto = new SessionWithQuestionsDTO();
        dto.setInterviewSessionId(interviewSessionId);
        // Dịch title và description
        List<String> sessionTexts = Arrays.asList(session.getTitle(), session.getDescription());
        List<String> translatedSessionTexts = translationUtil.translateBatchToVietnamese(sessionTexts);
        dto.setTitle(translatedSessionTexts.get(0));
        dto.setDescription(translatedSessionTexts.get(1));
        dto.setCompanyId(
                session.getCompany() != null ? session.getCompany().getCompanyId() : null
        );
        dto.setTotalQuestion(total);
        dto.setDurationEstimate(session.getDurationEstimate());
        dto.setQuestions(result);
        // Giữ nguyên tags
        dto.setTags(session.getTags().stream()
                .map(tag -> new TagSimpleDTO(tag.getTagId(), tag.getTitle()))
                .collect(Collectors.toList()));

        currentUser.getUserUsage().setInterviewUsed(currentUser.getUserUsage().getInterviewUsed() + 1);
        userRepository.save(currentUser);
        userUsageRepository.save(currentUser.getUserUsage());

        return dto;
    }
}
