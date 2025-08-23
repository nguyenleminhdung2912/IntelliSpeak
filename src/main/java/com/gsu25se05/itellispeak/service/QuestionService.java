package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.question.QuestionDTO;
import com.gsu25se05.itellispeak.dto.question.UpdateQuestionDTO;
import com.gsu25se05.itellispeak.entity.*;
import com.gsu25se05.itellispeak.repository.InterviewSessionRepository;
import com.gsu25se05.itellispeak.repository.QuestionRepository;
import com.gsu25se05.itellispeak.repository.TagRepository;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import com.gsu25se05.itellispeak.utils.mapper.QuestionMapper;
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

    public QuestionService(QuestionRepository questionRepository, QuestionMapper questionMapper, TagRepository tagRepository, AccountUtils accountUtils, InterviewSessionRepository interviewSessionRepository) {
        this.questionRepository = questionRepository;
        this.questionMapper = questionMapper;
        this.tagRepository = tagRepository;
        this.accountUtils = accountUtils;
        this.interviewSessionRepository = interviewSessionRepository;
    }

    public QuestionDTO save(QuestionDTO dto) {
        Question entity = questionMapper.toEntity(dto);
        entity.setQuestionStatus(QuestionStatus.APPROVED);
        entity.setSource("GeeksForGeeks");

        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser != null) {
            entity.setCreatedBy(currentUser);
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
        question.setIs_deleted(true);
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

        List<QuestionDTO> questions = questionRepository.findByCreatedBy(currentUser).stream()
                .map(questionMapper::toDTO)
                .collect(Collectors.toList());

        return new Response<>(200, "Successfully retrieved question list", questions);
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
        session.getQuestions().remove(question);
        interviewSessionRepository.save(session);
    }
}
