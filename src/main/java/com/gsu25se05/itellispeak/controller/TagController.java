package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.question.QuestionDTO;
import com.gsu25se05.itellispeak.dto.question.TagDTO;
import com.gsu25se05.itellispeak.dto.question.TagWithQuestionsDTO;
import com.gsu25se05.itellispeak.entity.Question;
import com.gsu25se05.itellispeak.entity.Tag;
import com.gsu25se05.itellispeak.entity.Topic;
import com.gsu25se05.itellispeak.service.TagService;
import com.gsu25se05.itellispeak.utils.mapper.QuestionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/tag")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class TagController {
    private final TagService tagService;
    private final QuestionMapper questionMapper;

    public TagController(TagService tagService, QuestionMapper questionMapper) {
        this.tagService = tagService;
        this.questionMapper = questionMapper;
    }

    @PostMapping
    public ResponseEntity<Response<TagDTO>> create(@RequestBody TagDTO dto) {
        TagDTO saved = tagService.save(dto);
        return ResponseEntity.ok(new Response<>(200, "Tag created successfully", saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<TagDTO>> getById(@PathVariable Long id) {
        return tagService.findById(id)
                .map(tag -> ResponseEntity.ok(new Response<>(200, "Tag found", tag)))
                .orElse(ResponseEntity.status(404).body(new Response<>(404, "Tag not found", null)));
    }

    @GetMapping
    public ResponseEntity<Response<List<TagDTO>>> getAll() {
        List<TagDTO> tags = tagService.findAll();
        return ResponseEntity.ok(new Response<>(200, "Tags fetched successfully", tags));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin Tag theo ID (Tức là nhập ID lên Uri á Lĩu")
    public ResponseEntity<Response<TagDTO>> update(@PathVariable Long id, @RequestBody TagDTO dto) {
        return tagService.update(id, dto)
                .map(tag -> ResponseEntity.ok(new Response<>(200, "Tag updated successfully", tag)))
                .orElse(ResponseEntity.status(404).body(new Response<>(404, "Tag not found", null)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa Tag theo ID")
    public ResponseEntity<Response<Void>> delete(@PathVariable Long id) {
        if (tagService.delete(id)) {
            return ResponseEntity.ok(new Response<>(200, "Tag deleted successfully", null));
        } else {
            return ResponseEntity.status(404).body(new Response<>(404, "Tag not found", null));
        }
    }

    @PutMapping("/{questionId}/tags/{tagId}")
    @Operation(summary = "Thêm Tag vào Question theo ID (Nhập question id và tag id là để thêm Tag có TagID đó vào Question có QuestionID đó)")
    public ResponseEntity<Response<QuestionDTO>> addTagToQuestion(
            @PathVariable Long questionId,
            @PathVariable Long tagId) {
        Question updated = tagService.addTagToQuestion(questionId, tagId);
        QuestionDTO dto = questionMapper.toDTO(updated);
        return ResponseEntity.ok(new Response<>(200, "Tag added to question", dto));
    }

    @PutMapping("/{tagId}/questions")
    @Operation(summary = "Thêm Tag vào nhiều Question theo ID",
            description = "Nhập TagID lên trên đường dẫn, và danh sách QuestionIds vào body, là Thêm 1 tag vào nhiều Question")
    public ResponseEntity<Response<List<QuestionDTO>>> addTagToQuestions(
            @PathVariable Long tagId,
            @RequestBody List<Long> questionIds) {
        List<Question> updatedQuestions = tagService.addTagToQuestions(tagId, questionIds);
        List<QuestionDTO> questionsDto = updatedQuestions.stream()
                .map(questionMapper::toDTO)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(new Response<>(200, "Tag added to multiple questions", questionsDto));
    }

    @DeleteMapping("/{questionId}/tags/{tagId}")
    @Operation(summary = "Xóa Tag khỏi Question theo ID")
    public ResponseEntity<Response<QuestionDTO>> removeTagFromQuestion(
            @PathVariable Long questionId,
            @PathVariable Long tagId) {
        Question updated = tagService.removeTagFromQuestion(questionId, tagId);
        QuestionDTO dto = questionMapper.toDTO(updated);
        return ResponseEntity.ok(new Response<>(200, "Tag removed from question", dto));
    }

    @GetMapping("/with-questions")
    @Operation(summary = "Hiện danh sách câu hỏi thuộc Tag đó")
    public ResponseEntity<Response<List<TagWithQuestionsDTO>>> getAllTagsWithQuestions() {
        List<TagWithQuestionsDTO> result = tagService.getAllTagsWithQuestions();
        return ResponseEntity.ok(new Response<>(200, "Fetched tags with questions", result));
    }

    @PutMapping("/{tagId}/topics/{topicId}")
    public ResponseEntity<TagDTO> addTopicToTag(@PathVariable Long tagId, @PathVariable Long topicId) {
        Tag updated = tagService.addTagToTopic(tagId, topicId);
        TagDTO dto = tagService.findById(tagId).orElse(null);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{tagId}/topics/{topicId}")
    public ResponseEntity<TagDTO> removeTopicFromTag(@PathVariable Long tagId, @PathVariable Long topicId) {
        Tag updated = tagService.removeTagFromTopic(tagId, topicId);
        TagDTO dto = tagService.findById(tagId).orElse(null);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{tagId}/topics")
    public ResponseEntity<Set<Topic>> getTopicsOfTag(@PathVariable Long tagId) {
        Set<Topic> topics = tagService.getTopicsByTag(tagId);
        return ResponseEntity.ok(topics);
    }

}