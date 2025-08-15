package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.question.QuestionDTO;
import com.gsu25se05.itellispeak.entity.Question;
import com.gsu25se05.itellispeak.entity.QuestionStatus;
import com.gsu25se05.itellispeak.entity.Tag;
import com.gsu25se05.itellispeak.entity.User;
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

    public QuestionService(QuestionRepository questionRepository, QuestionMapper questionMapper, TagRepository tagRepository, AccountUtils accountUtils) {
        this.questionRepository = questionRepository;
        this.questionMapper = questionMapper;
        this.tagRepository = tagRepository;
        this.accountUtils = accountUtils;
    }

    public QuestionDTO save(QuestionDTO dto) {
        Question entity = questionMapper.toEntity(dto);
        entity.setQuestionStatus(QuestionStatus.APPROVED);

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
            return new Response<>(401, "Vui lòng đăng nhập để tiếp tục", null);
        }

        String roleName = currentUser.getRole().name();
        if (!"HR".equalsIgnoreCase(roleName) && !"ADMIN".equalsIgnoreCase(roleName)) {
            return new Response<>(403, "Chỉ HR hoặc ADMIN mới có quyền xem danh sách câu hỏi", null);
        }

        List<QuestionDTO> questions = questionRepository.findByCreatedBy(currentUser).stream()
                .map(questionMapper::toDTO)
                .collect(Collectors.toList());

        return new Response<>(200, "Lấy danh sách câu hỏi thành công", questions);
    }

    public Response<List<QuestionDTO>> importFromCsv(MultipartFile file) {
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            return new Response<>(401, "Vui lòng đăng nhập để tiếp tục", null);
        }

        String roleName = currentUser.getRole().name();
        if (!"HR".equalsIgnoreCase(roleName) && !"ADMIN".equalsIgnoreCase(roleName)) {
            return new Response<>(403, "Chỉ HR hoặc ADMIN mới có quyền import câu hỏi", null);
        }

        List<QuestionDTO> importedQuestions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            List<String> lines = reader.lines().collect(Collectors.toList());
            if (lines.isEmpty()) {
                return new Response<>(400, "File CSV trống", null);
            }

            lines.set(0, lines.get(0).replace("\uFEFF", ""));

            String csvContent = String.join("\n", lines);
            try (CSVParser csvParser = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreHeaderCase()
                    .withTrim()
                    .parse(new StringReader(csvContent))) {

                List<String> headers = csvParser.getHeaderNames().stream()
                        .map(String::trim)
                        .collect(Collectors.toList());

                List<String> requiredHeaders = List.of("title", "content", "difficulty", "suitableAnswer1", "suitableAnswer2", "tagIds");
                for (String required : requiredHeaders) {
                    if (!headers.contains(required)) {
                        return new Response<>(400, "File CSV thiếu cột bắt buộc: " + required, null);
                    }
                }

                for (CSVRecord record : csvParser) {
                    try {
                        QuestionDTO dto = new QuestionDTO();
                        dto.setTitle(record.get("title"));
                        dto.setContent(record.get("content"));
                        dto.setDifficulty(record.get("difficulty"));
                        dto.setSuitableAnswer1(record.get("suitableAnswer1"));
                        dto.setSuitableAnswer2(record.get("suitableAnswer2"));

                        String tagIdsStr = record.get("tagIds");
                        if (tagIdsStr != null && !tagIdsStr.isBlank()) {
                            Set<Long> tagIds = Arrays.stream(tagIdsStr.split(","))
                                    .map(String::trim)
                                    .filter(s -> !s.isEmpty())
                                    .map(Long::parseLong)
                                    .collect(Collectors.toSet());
                            dto.setTagIds(tagIds);
                        }

                        importedQuestions.add(save(dto));
                    } catch (Exception e) {
                        return new Response<>(400, "Lỗi khi đọc dòng CSV: " + e.getMessage(), null);
                    }
                }
            }

        } catch (IOException e) {
            return new Response<>(500, "Không thể đọc file CSV: " + e.getMessage(), null);
        } catch (IllegalArgumentException e) {
            return new Response<>(400, "File CSV không đúng định dạng: " + e.getMessage(), null);
        }

        return new Response<>(200, "Import CSV thành công", importedQuestions);
    }





}
