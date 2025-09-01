package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.interview_session.ConfirmCsvRequest;
import com.gsu25se05.itellispeak.dto.interview_session.InterviewSessionDTO;
import com.gsu25se05.itellispeak.dto.question.CSVQuestionDTO;
import com.gsu25se05.itellispeak.dto.question.CompanyQuestionDTO;
import com.gsu25se05.itellispeak.dto.question.QuestionDTO;
import com.gsu25se05.itellispeak.dto.question.UpdateQuestionDTO;
import com.gsu25se05.itellispeak.entity.*;
import com.gsu25se05.itellispeak.exception.ErrorCode;
import com.gsu25se05.itellispeak.exception.auth.AuthAppException;
import com.gsu25se05.itellispeak.repository.CompanyRepository;
import com.gsu25se05.itellispeak.repository.InterviewSessionRepository;
import com.gsu25se05.itellispeak.repository.QuestionRepository;
import com.gsu25se05.itellispeak.repository.TagRepository;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import com.gsu25se05.itellispeak.utils.mapper.QuestionMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.stereotype.Service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.StringReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final TagRepository tagRepository;
    private final AccountUtils accountUtils;
    private final InterviewSessionRepository interviewSessionRepository;
    private final CompanyRepository companyRepository;

    public QuestionService(QuestionRepository questionRepository, QuestionMapper questionMapper, TagRepository tagRepository, AccountUtils accountUtils, InterviewSessionRepository interviewSessionRepository, CompanyRepository companyRepository) {
        this.questionRepository = questionRepository;
        this.questionMapper = questionMapper;
        this.tagRepository = tagRepository;
        this.accountUtils = accountUtils;
        this.interviewSessionRepository = interviewSessionRepository;
        this.companyRepository = companyRepository;
    }

    public QuestionDTO save(QuestionDTO dto) {
        Question entity = questionMapper.toEntity(dto);
        entity.setQuestionStatus(QuestionStatus.APPROVED);
        entity.setSource("GeeksForGeeks");

        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser != null) {
            entity.setCreatedBy(currentUser);
        }

        if (currentUser.getHr().getStatus() == HRStatus.APPROVED)
            entity.setCompany(currentUser.getHr().getCompany());

        if (currentUser.getRole() == User.Role.HR && currentUser.getHr().getCompany() != null) {
            entity.setCompany(currentUser.getHr().getCompany());
        }

        if (dto.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(dto.getTagIds()));
            entity.setTags(tags);
        }
        return questionMapper.toDTO(questionRepository.save(entity));
    }

    public void deleteQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        question.setIsDeleted(true);
        questionRepository.save(question);
    }

    public QuestionDTO updateQuestion(Long questionId, UpdateQuestionDTO dto) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        if (dto.getTitle() != null) question.setTitle(dto.getTitle());
        if (dto.getContent() != null) question.setContent(dto.getContent());
        if (dto.getSuitableAnswer1() != null) question.setSuitableAnswer1(dto.getSuitableAnswer1());
        if (dto.getSuitableAnswer2() != null) question.setSuitableAnswer2(dto.getSuitableAnswer2());
        if (dto.getDifficulty() != null) question.setDifficulty(dto.getDifficulty());
        if (dto.getSource() != null) question.setSource(dto.getSource());

        return questionMapper.toDTO(questionRepository.save(question));
    }

    public Optional<QuestionDTO> findById(Long id) {
        return questionRepository.findById(id).map(questionMapper::toDTO);
    }

    public List<QuestionDTO> findAll() {
        return questionRepository.findAll().stream()
                .filter(question -> question.getIsDeleted() == false)
                .map(questionMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Response<List<QuestionDTO>> getByCurrentUser() {
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            return new Response<>(401, "Please log in to continue", null);
        }

        String roleName = currentUser.getRole().name();
        if (!"HR".equalsIgnoreCase(roleName) && !"ADMIN".equalsIgnoreCase(roleName)) {
            return new Response<>(403, "Only HR or ADMIN users can view the question list", null);
        }

        List<QuestionDTO> questions = questionRepository.findByCreatedByOrderByQuestionIdDesc(currentUser).stream()
                .filter(question -> question.getIsDeleted() == false)
                .map(questionMapper::toDTO)
                .collect(Collectors.toList());

        return new Response<>(200, "Successfully retrieved question list", questions);
    }

    private CompanyQuestionDTO toCompanyQuestionDTO(Question q) {
        return CompanyQuestionDTO.builder()
                .questionId(q.getQuestionId())
                .title(q.getTitle())
                .content(q.getContent())
                .difficulty(q.getDifficulty() != null ? q.getDifficulty().name() : null)
                .suitableAnswer1(q.getSuitableAnswer1())
                .suitableAnswer2(q.getSuitableAnswer2())
                .isDeleted(Boolean.TRUE.equals(q.getIsDeleted()))
                .tagIds(q.getTags() != null
                        ? q.getTags().stream().map(Tag::getTagId).collect(java.util.stream.Collectors.toSet())
                        : java.util.Collections.emptySet())
                .tags(q.getTags())
                .interviewSessionId(null)
                .interviewSessionName(null)
                .build();
    }

    @Transactional(readOnly = true)
    public Response<List<CompanyQuestionDTO>> getMyCompanyQuestions() {
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) return new Response<>(401, "Please log in to continue", null);
        if (currentUser.getRole() != User.Role.HR) return new Response<>(403, "Only HR can view their company questions", null);
        if (currentUser.getHr() == null || currentUser.getHr().getCompany() == null)
            return new Response<>(403, "HR is not linked to any company", null);

        final Company company = currentUser.getHr().getCompany();

        //Lấy tất cả câu hỏi của công ty
        final List<Question> questions =
                questionRepository.findByCompanyAndIsDeletedFalseOrderByQuestionIdDesc(company);

        if (questions.isEmpty()) {
            return new Response<>(200, "Successfully retrieved company questions", Collections.emptyList());
        }

        //Lấy tất cả session có chứa các câu hỏi này
        final List<InterviewSession> sessions =
                interviewSessionRepository.findAllByCompanyAndQuestionsIn(company, questions);

        //Chọn session cho mỗi question
        final Map<Long, InterviewSession> latestSessionByQid = new HashMap<>();
        for (InterviewSession s : sessions) {
            if (s.getQuestions() == null) continue;
            for (Question q : s.getQuestions()) {
                final Long qid = q.getQuestionId();
                if (qid == null) continue;
                final InterviewSession cur = latestSessionByQid.get(qid);
                if (cur == null || s.getInterviewSessionId() > cur.getInterviewSessionId()) {
                    latestSessionByQid.put(qid, s);
                }
            }
        }

        //Map sang CompanyQuestionDTO và gắn interviewSessionId và Name
        final List<CompanyQuestionDTO> dtos = questions.stream()
                .map(q -> {
                    final InterviewSession ls = latestSessionByQid.get(q.getQuestionId());
                    return CompanyQuestionDTO.builder()
                            .questionId(q.getQuestionId())
                            .title(q.getTitle())
                            .content(q.getContent())
                            .difficulty(q.getDifficulty() != null ? q.getDifficulty().name() : null)
                            .suitableAnswer1(q.getSuitableAnswer1())
                            .suitableAnswer2(q.getSuitableAnswer2())
                            .isDeleted(Boolean.TRUE.equals(q.getIsDeleted()))
                            .tagIds(q.getTags() != null
                                    ? q.getTags().stream().map(Tag::getTagId).collect(Collectors.toSet())
                                    : Collections.emptySet())
                            .tags(q.getTags())
                            .interviewSessionId(ls != null ? ls.getInterviewSessionId() : null)
                            .interviewSessionName(ls != null ? ls.getTitle() : null)
                            .build();
                })
                .collect(Collectors.toList());

        return new Response<>(200, "Successfully retrieved company questions", dtos);
    }

    public Response<List<QuestionDTO>> importFromCsv(MultipartFile file, Long tagId) {
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            return new Response<>(401, "Please log in to continue", null);
        }
        String roleName = currentUser.getRole().name();
        if (!"HR".equalsIgnoreCase(roleName) && !"ADMIN".equalsIgnoreCase(roleName)) {
            return new Response<>(403, "Only HR or ADMIN users can import questions", null);
        }

        if (tagId == null) {
            return new Response<>(400, "Missing required parameter: tagId", null);
        }

        // Đảm bảo tag tồn tại
        Tag tag = tagRepository.findById(tagId).orElse(null);
        if (tag == null) {
            return new Response<>(400, "Tag not found: " + tagId, null);
        }

        final List<String> REQUIRED_HEADERS = List.of(
                "title", "content", "difficulty", "suitableAnswer1", "suitableAnswer2"
        );

        List<QuestionDTO> imported = new ArrayList<>();
        List<String> rowErrors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            List<String> lines = reader.lines().collect(Collectors.toList());
            if (lines.isEmpty()) {
                return new Response<>(400, "CSV file is empty", null);
            }

            // remove UTF-8 BOM
            lines.set(0, lines.get(0).replace("\uFEFF", ""));
            String csvContent = String.join("\n", lines);

            try (CSVParser csv = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreHeaderCase()
                    .withTrim()
                    .parse(new StringReader(csvContent))) {

                // Kiểm tra header bắt buộc (case-insensitive)
                Set<String> headersLc = csv.getHeaderNames().stream()
                        .map(h -> h == null ? "" : h.trim().toLowerCase())
                        .collect(Collectors.toSet());

                for (String required : REQUIRED_HEADERS) {
                    if (!headersLc.contains(required.toLowerCase())) {
                        return new Response<>(400, "CSV file is missing required column: " + required, null);
                    }
                }

                // Bỏ qua hoàn toàn cột tagIds nếu file có (để đảm bảo dùng duy nhất 1 tag từ request)
                long rowIndex = 1;
                for (CSVRecord r : csv) {
                    rowIndex++;
                    try {
                        String title = safe(r, "title");
                        String content = safe(r, "content");
                        String difficultyRaw = safe(r, "difficulty");
                        String s1 = safe(r, "suitableAnswer1");
                        String s2 = safe(r, "suitableAnswer2");

                        if (title.isBlank() || content.isBlank() || difficultyRaw.isBlank()) {
                            throw new IllegalArgumentException("title/content/difficulty must not be blank");
                        }

                        String difficulty = normalizeDifficulty(difficultyRaw); // EASY|MEDIUM|HARD

                        QuestionDTO dto = new QuestionDTO();
                        dto.setTitle(title);
                        dto.setContent(content);
                        dto.setDifficulty(difficulty);
                        dto.setSuitableAnswer1(s1);
                        dto.setSuitableAnswer2(s2);

                        dto.setTagIds(Set.of(tagId));

                        imported.add(save(dto));
                    } catch (Exception rowEx) {
                        rowErrors.add("Row " + rowIndex + ": " + rowEx.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            return new Response<>(500, "Unable to read CSV file: " + e.getMessage(), null);
        } catch (IllegalArgumentException e) {
            return new Response<>(400, "Invalid CSV format: " + e.getMessage(), null);
        }

        if (!rowErrors.isEmpty()) {
            String msg = "CSV import completed with " + rowErrors.size() + " row error(s). "
                    + "First error: " + rowErrors.get(0);
            return new Response<>(200, msg, imported);
        }
        return new Response<>(200, "CSV import successful", imported);
    }

    public Response<List<CSVQuestionDTO>> importQuestionsToInterviewSession(
            MultipartFile file,
            Long tagId,
            Long interviewSessionId
    ) {
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            return new Response<>(401, "Please log in to continue", null);
        }
        String roleName = currentUser.getRole().name();
        if (!"HR".equalsIgnoreCase(roleName) && !"ADMIN".equalsIgnoreCase(roleName)) {
            return new Response<>(403, "Only HR or ADMIN users can import questions", null);
        }

        // Validate params
        if (tagId == null) {
            return new Response<>(400, "Missing required parameter: tagId", null);
        }
        if (interviewSessionId == null) {
            return new Response<>(400, "Missing required parameter: interviewSessionId", null);
        }

        Tag tag = tagRepository.findById(tagId).orElse(null);
        if (tag == null) {
            return new Response<>(400, "Tag not found: " + tagId, null);
        }

        InterviewSession session = interviewSessionRepository.findById(interviewSessionId).orElse(null);
        if (session == null) {
            return new Response<>(400, "Interview session not found: " + interviewSessionId, null);
        }

        final List<String> REQUIRED_HEADERS = List.of(
                "title", "content", "difficulty", "suitableAnswer1", "suitableAnswer2"
        );

        List<CSVQuestionDTO> createdDtos = new ArrayList<>();
        List<String> rowErrors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            List<String> lines = reader.lines().collect(Collectors.toList());
            if (lines.isEmpty()) {
                return new Response<>(400, "CSV file is empty", null);
            }
            // remove UTF-8 BOM
            lines.set(0, lines.get(0).replace("\uFEFF", ""));
            String csvContent = String.join("\n", lines);

            try (CSVParser csv = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreHeaderCase()
                    .withTrim()
                    .parse(new StringReader(csvContent))) {

                Set<String> headersLc = csv.getHeaderNames().stream()
                        .map(h -> h == null ? "" : h.trim().toLowerCase())
                        .collect(Collectors.toSet());
                for (String h : REQUIRED_HEADERS) {
                    if (!headersLc.contains(h.toLowerCase())) {
                        return new Response<>(400, "CSV file is missing required column: " + h, null);
                    }
                }

                Company uploaderCompany = (currentUser.getRole() == User.Role.HR)
                        ? (currentUser.getHr() != null ? currentUser.getHr().getCompany() : null)
                        : null;

                if (session.getQuestions() == null) {
                    session.setQuestions(new HashSet<>());
                }

                long rowIndex = 1; // header = row 1
                for (CSVRecord r : csv) {
                    rowIndex++;
                    try {
                        String title = safe(r, "title");
                        String content = safe(r, "content");
                        String difficultyRaw = safe(r, "difficulty");
                        String s1 = safe(r, "suitableAnswer1");
                        String s2 = safe(r, "suitableAnswer2");

                        if (title.isBlank() || content.isBlank() || difficultyRaw.isBlank()) {
                            throw new IllegalArgumentException("title/content/difficulty must not be blank");
                        }

                        Difficulty diffEnum = Difficulty.valueOf(normalizeDifficulty(difficultyRaw)); // EASY|MEDIUM|HARD

                        Question q = new Question();
                        q.setTitle(title);
                        q.setContent(content);
                        q.setSuitableAnswer1(s1);
                        q.setSuitableAnswer2(s2);
                        q.setDifficulty(diffEnum);
                        q.setQuestionStatus(QuestionStatus.APPROVED);
                        q.setSource("GeeksForGeeks");
                        q.setIsDeleted(Boolean.FALSE);

                        q.setCreatedBy(currentUser);
                        if (uploaderCompany != null) {
                            q.setCompany(uploaderCompany);
                        }

                        q.setTags(new HashSet<>(Collections.singletonList(tag)));

                        Question saved = questionRepository.save(q);
                        session.getQuestions().add(saved);

                        // Build CSVQuestionDTO
                        CSVQuestionDTO dto = toCsvQuestionDTO(saved, session, tag);
                        createdDtos.add(dto);

                    } catch (Exception rowEx) {
                        rowErrors.add("Row " + rowIndex + ": " + rowEx.getMessage());
                    }
                }


                Integer total = (session.getQuestions() == null) ? 0 : session.getQuestions().size();
                session.setTotalQuestion(total);
                interviewSessionRepository.save(session);

                for (CSVQuestionDTO dto : createdDtos) {
                    if (dto.getInterviewSessionDTO() != null) {
                        dto.getInterviewSessionDTO().setTotalQuestion(total);
                    }
                }

                String msg;
                if (!rowErrors.isEmpty()) {
                    msg = "CSV import completed with " + rowErrors.size()
                            + " row error(s). First error: " + rowErrors.get(0);
                } else {
                    msg = "CSV import successful";
                }

                return new Response<>(200, msg, createdDtos);
            }

        } catch (IOException e) {
            return new Response<>(500, "Unable to read CSV file: " + e.getMessage(), null);
        } catch (IllegalArgumentException e) {
            return new Response<>(400, "Invalid CSV format: " + e.getMessage(), null);
        }
    }

    @Transactional(readOnly = true)
    public Response<List<CSVQuestionDTO>> previewQuestionsFromCsv(MultipartFile file) {
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            return new Response<>(401, "Please log in to continue", null);
        }
        String roleName = currentUser.getRole().name();
        if (!"HR".equalsIgnoreCase(roleName) && !"ADMIN".equalsIgnoreCase(roleName)) {
            return new Response<>(403, "Only HR or ADMIN users can preview CSV", null);
        }

        final List<String> REQUIRED_HEADERS = List.of(
                "title", "content", "difficulty", "suitableAnswer1", "suitableAnswer2"
        );

        List<CSVQuestionDTO> parsedDtos = new ArrayList<>();
        List<String> rowErrors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            List<String> lines = reader.lines().collect(Collectors.toList());
            if (lines.isEmpty()) {
                return new Response<>(400, "CSV file is empty", null);
            }

            // remove UTF-8 BOM
            lines.set(0, lines.get(0).replace("\uFEFF", ""));
            String csvContent = String.join("\n", lines);

            try (CSVParser csv = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreHeaderCase()
                    .withTrim()
                    .parse(new StringReader(csvContent))) {

                Set<String> headersLc = csv.getHeaderNames().stream()
                        .map(h -> h == null ? "" : h.trim().toLowerCase())
                        .collect(Collectors.toSet());

                for (String h : REQUIRED_HEADERS) {
                    if (!headersLc.contains(h.toLowerCase())) {
                        return new Response<>(400, "CSV file is missing required column: " + h, null);
                    }
                }

                long rowIndex = 1; // header = row 1
                for (CSVRecord r : csv) {
                    rowIndex++;
                    try {
                        String title = safe(r, "title");
                        String content = safe(r, "content");
                        String difficultyRaw = safe(r, "difficulty");
                        String s1 = safe(r, "suitableAnswer1");
                        String s2 = safe(r, "suitableAnswer2");

                        if (title.isBlank() || content.isBlank() || difficultyRaw.isBlank()) {
                            throw new IllegalArgumentException("title/content/difficulty must not be blank");
                        }

                        String normalizedDiff = normalizeDifficulty(difficultyRaw); // EASY|MEDIUM|HARD

                        CSVQuestionDTO dto = CSVQuestionDTO.builder()
                                .title(title)
                                .content(content)
                                .difficulty(normalizedDiff)
                                .suitableAnswer1(s1)
                                .suitableAnswer2(s2)
                                .isDeleted(false)
                                .interviewSessionDTO(null) // không nhận session nữa
                                .build();

                        parsedDtos.add(dto);

                    } catch (Exception rowEx) {
                        rowErrors.add("Row " + rowIndex + ": " + rowEx.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            return new Response<>(500, "Unable to read CSV file: " + e.getMessage(), null);
        } catch (IllegalArgumentException e) {
            return new Response<>(400, "Invalid CSV format: " + e.getMessage(), null);
        }

        String msg = rowErrors.isEmpty()
                ? ("CSV preview successful. Parsed: " + parsedDtos.size())
                : ("CSV preview completed with " + rowErrors.size() + " row error(s). First error: " + rowErrors.get(0));

        return new Response<>(200, msg, parsedDtos);
    }

    @Transactional
    public Response<List<CSVQuestionDTO>> saveQuestionsFromPreview(ConfirmCsvRequest req) {
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            return new Response<>(401, "Please log in to continue", null);
        }
        String roleName = currentUser.getRole().name();
        if (!"HR".equalsIgnoreCase(roleName) && !"ADMIN".equalsIgnoreCase(roleName)) {
            return new Response<>(403, "Only HR or ADMIN users can save CSV questions", null);
        }

        if (req == null || req.getQuestions() == null || req.getQuestions().isEmpty()) {
            return new Response<>(400, "Request must include non-empty questions list", null);
        }

        InterviewSession session = null;
        if (req.getInterviewSessionId() != null) {
            session = interviewSessionRepository.findById(req.getInterviewSessionId()).orElse(null);
            if (session == null) {
                return new Response<>(400, "Interview session not found: " + req.getInterviewSessionId(), null);
            }
            if (session.getQuestions() == null) session.setQuestions(new HashSet<>());
        }

        Company uploaderCompany = (currentUser.getRole() == User.Role.HR && currentUser.getHr() != null)
                ? currentUser.getHr().getCompany() : null;

        List<CSVQuestionDTO> savedDtos = new ArrayList<>();
        List<String> rowErrors = new ArrayList<>();

        int rowIndex = 0;
        for (CSVQuestionDTO item : req.getQuestions()) {
            rowIndex++;
            try {
                // validate tối thiểu
                String title = n(item.getTitle());
                String content = n(item.getContent());
                String diffRaw = n(item.getDifficulty());
                if (title.isBlank() || content.isBlank() || diffRaw.isBlank()) {
                    throw new IllegalArgumentException("title/content/difficulty must not be blank");
                }

                Difficulty diffEnum = Difficulty.valueOf(normalizeDifficulty(diffRaw)); // EASY|MEDIUM|HARD

                // resolve tagIds per question
                Set<Tag> tags = resolveTags(item.getTagIds()); // cho phép null/empty


                Question saved = saveSingleQuestionRequiresNew(
                        title, content, item.getSuitableAnswer1(), item.getSuitableAnswer2(),
                        diffEnum, currentUser, uploaderCompany, tags
                );

                if (session != null) {
                    session.getQuestions().add(saved);
                }

                // build DTO trả về
                CSVQuestionDTO dto = toCsvQuestionsDTO(saved, session, tags);
                savedDtos.add(dto);

            } catch (Exception ex) {
                rowErrors.add("Row " + rowIndex + ": " + ex.getMessage());
            }
        }

        if (session != null) {
            Integer total = (session.getQuestions() == null) ? 0 : session.getQuestions().size();
            session.setTotalQuestion(total);
            interviewSessionRepository.save(session);

            for (CSVQuestionDTO dto : savedDtos) {
                if (dto.getInterviewSessionDTO() != null) {
                    dto.getInterviewSessionDTO().setTotalQuestion(total);
                }
            }
        }

        String msg = rowErrors.isEmpty()
                ? "CSV save successful"
                : ("CSV save completed with " + rowErrors.size() + " row error(s). First error: " + rowErrors.get(0));

        return new Response<>(200, msg, savedDtos);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected Question saveSingleQuestionRequiresNew(
            String title,
            String content,
            String s1,
            String s2,
            Difficulty difficulty,
            User creator,
            Company company,
            Set<Tag> tags
    ) {
        Question q = new Question();
        q.setTitle(title);
        q.setContent(content);
        q.setSuitableAnswer1(s1);
        q.setSuitableAnswer2(s2);
        q.setDifficulty(difficulty);
        q.setQuestionStatus(QuestionStatus.APPROVED);
        q.setSource("CSV Upload");
        q.setIsDeleted(Boolean.FALSE);

        q.setCreatedBy(creator);
        if (company != null) q.setCompany(company);

        if (tags != null && !tags.isEmpty()) {
            q.setTags(new HashSet<>(tags));
        } else {
            q.setTags(new HashSet<>());
        }

        return questionRepository.save(q);
    }

    private Set<Tag> resolveTags(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return Collections.emptySet();
        List<Tag> found = tagRepository.findAllById(tagIds);
        Set<Long> foundIds = found.stream().map(Tag::getTagId).collect(Collectors.toSet());
        Set<Long> missing = tagIds.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toSet());
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Tag not found: " + missing);
        }
        return new HashSet<>(found);
    }

    private static String n(String s) { return s == null ? "" : s.trim(); }

    private CSVQuestionDTO toCsvQuestionsDTO(Question q, InterviewSession session, Set<Tag> tagsForRow) {
        InterviewSessionDTO sessionDTO = null;
        if (session != null) {
            sessionDTO = new InterviewSessionDTO();
            sessionDTO.setInterviewSessionId(session.getInterviewSessionId());
            sessionDTO.setTitle(session.getTitle());
            sessionDTO.setTotalQuestion(session.getTotalQuestion());
        }

        return CSVQuestionDTO.builder()
                .questionId(q.getQuestionId())
                .title(q.getTitle())
                .content(q.getContent())
                .difficulty(q.getDifficulty().name())
                .suitableAnswer1(q.getSuitableAnswer1())
                .suitableAnswer2(q.getSuitableAnswer2())
                .isDeleted(Boolean.TRUE.equals(q.getIsDeleted()))
                .tags(tagsForRow != null && !tagsForRow.isEmpty() ? tagsForRow : q.getTags())
                .interviewSessionDTO(sessionDTO)
                .build();
    }


    private CSVQuestionDTO toCsvQuestionDTO(Question saved, InterviewSession session, Tag tag) {

        InterviewSessionDTO isDto = new InterviewSessionDTO();
        isDto.setInterviewSessionId(session.getInterviewSessionId());
        isDto.setTitle(session.getTitle());
        isDto.setTotalQuestion(session.getTotalQuestion());

        return CSVQuestionDTO.builder()
                .questionId(saved.getQuestionId())
                .title(saved.getTitle())
                .content(saved.getContent())
                .difficulty(saved.getDifficulty() != null ? saved.getDifficulty().name() : null)
                .suitableAnswer1(saved.getSuitableAnswer1())
                .suitableAnswer2(saved.getSuitableAnswer2())
                .isDeleted(Boolean.TRUE.equals(saved.getIsDeleted()))
                .tagIds(Set.of(tag.getTagId()))
                .tags(Set.of(tag))
                .interviewSessionDTO(isDto)
                .build();
    }

    private static String safe(CSVRecord r, String col) {
        String v = r.get(col);
        return v == null ? "" : v.trim();
    }

    private static String normalizeDifficulty(String raw) {
        String s = raw.trim().toUpperCase();
        switch (s) {
            case "EASY":
            case "MEDIUM":
            case "HARD":
                return s;
            default:
                // nếu muốn nghiêm ngặt, throw; hoặc default MEDIUM
                return "MEDIUM";
        }
    }

    private static Set<Long> parseTagIds(String raw) {
        if (raw == null || raw.isBlank()) return Collections.emptySet();
        String s = raw.trim();
        // Hỗ trợ dạng [1,2,3]
        if (s.startsWith("[") && s.endsWith("]")) {
            s = s.substring(1, s.length() - 1);
        }
        if (s.isBlank()) return Collections.emptySet();

        return Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .map(x -> x.replaceAll("[^0-9]", "")) // lọc ký tự không phải số
                .filter(x -> !x.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }

    public void removeQuestionFromSession(Long sessionId, Long questionId) {
        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        boolean removed = session.getQuestions().remove(question);

        // Chỉ cập nhật lại khi việc xóa thực sự diễn ra
        if (removed) {
            session.setTotalQuestion(session.getQuestions().size());
            interviewSessionRepository.save(session);
        }
    }

    public Response<List<QuestionDTO>> getCompanyQuestionsNotInSession(Long sessionId) {
        // 1. Security: Get current user, check role, and get company
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            throw new AuthAppException(ErrorCode.NOT_LOGIN);
        }
        if (currentUser.getRole() != User.Role.HR) {
            throw new AuthAppException(ErrorCode.ACCOUNT_NOT_HR);
        }
        Company hrCompany = (currentUser.getHr() != null) ? currentUser.getHr().getCompany() : null;
        if (hrCompany == null) {
            throw new AuthAppException(ErrorCode.HR_NOT_FOUND);
        }

        // 2. Find session and verify ownership
        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AuthAppException(ErrorCode.INTERVIEW_SESSION_NOT_FOUND));

        // An HR can only add questions to their own company's sessions
        if (session.getCompany() == null || !session.getCompany().getCompanyId().equals(hrCompany.getCompanyId())) {
            throw new AuthAppException(ErrorCode.ACTION_FORBIDDEN);
        }

        // 3. Get tags from the session. If none, no questions can be suggested.
        Set<Tag> sessionTags = session.getTags();
        if (sessionTags == null || sessionTags.isEmpty()) {
            return new Response<>(200, "Interview session has no tags, no relevant questions to suggest.", Collections.emptyList());
        }

        // 4. Get IDs of questions already in the session
        Set<Long> existingQuestionIds = session.getQuestions().stream()
                .map(Question::getQuestionId)
                .collect(Collectors.toSet());

        // 5. Fetch questions from repository that match session tags
        List<Question> availableQuestions;
        if (existingQuestionIds.isEmpty()) {
            availableQuestions = questionRepository.findDistinctByCompanyAndIsDeletedFalseAndTagsInOrderByQuestionIdDesc(hrCompany, sessionTags);
        } else {
            availableQuestions = questionRepository.findDistinctByCompanyAndIsDeletedFalseAndTagsInAndQuestionIdNotInOrderByQuestionIdDesc(hrCompany, sessionTags, existingQuestionIds);
        }

        // 6. Map to DTOs and return
        List<QuestionDTO> dtos = availableQuestions.stream()
                .map(questionMapper::toDTO)
                .collect(Collectors.toList());

        return new Response<>(200, "Successfully retrieved available questions for the session.", dtos);
    }
}
