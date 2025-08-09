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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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


}
