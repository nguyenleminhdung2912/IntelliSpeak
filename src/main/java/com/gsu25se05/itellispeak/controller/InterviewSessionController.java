package com.gsu25se05.itellispeak.controller;


import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.interview_session.InterviewByTopicDTO;
import com.gsu25se05.itellispeak.dto.interview_session.InterviewSessionDTO;
import com.gsu25se05.itellispeak.dto.interview_session.QuestionSelectionRequestDTO;
import com.gsu25se05.itellispeak.dto.interview_session.SessionWithQuestionsDTO;
import com.gsu25se05.itellispeak.dto.topic.TopicWithTagsDTO;
import com.gsu25se05.itellispeak.entity.InterviewSession;
import com.gsu25se05.itellispeak.entity.Question;
import com.gsu25se05.itellispeak.service.InterviewSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@CrossOrigin("**")
@SecurityRequirement(name = "api")
@RequestMapping("/interview-sessions")
public class InterviewSessionController {
    private final InterviewSessionService interviewSessionService;

    public InterviewSessionController(InterviewSessionService interviewSessionService) {
        this.interviewSessionService = interviewSessionService;
    }

    @PostMapping("/create")
    @Operation(summary = "Tạo interview session")
    public ResponseEntity<Response<InterviewSession>> create(@RequestBody InterviewSessionDTO interviewSessionDTO) {
        InterviewSession session = interviewSessionService.save(interviewSessionDTO);
        return ResponseEntity.ok(new Response<>(200, "Interview session created", session));
    }

    @PostMapping("/{sessionId}/questions/{questionId}")
    @Operation(summary = "Thêm 1 câu hỏi cho interview session (dành cho câu hỏi đã có sẵn trong database chứ không phải thêm mới)")
    public ResponseEntity<Response<InterviewSession>> addQuestion(@PathVariable Long sessionId, @PathVariable Long questionId) {
        InterviewSession session = interviewSessionService.addQuestionToSession(sessionId, questionId);
        return ResponseEntity.ok(new Response<>(200, "Question added to session", session));
    }

    @PostMapping("/{sessionId}/questions")
    @Operation(summary = "Thêm nhiều câu hỏi cho interview session (dành cho các câu hỏi đã có sẵn trong database chứ không phải thêm mới)")
    public ResponseEntity<Response<InterviewSession>> addQuestions(@PathVariable Long sessionId, @RequestBody Set<Question> questions) {
        InterviewSession session = interviewSessionService.addQuestionsToSession(sessionId, questions);
        return ResponseEntity.ok(new Response<>(200, "Questions added to session", session));
    }

    @GetMapping("/sessions/get-all")
    @Operation(summary = "Tạm thời bỏ đi, không dùng tới, chỉ cần admin dùng thôi")
    public ResponseEntity<Response<Iterable<InterviewSession>>> getAllSessions() {
        Iterable<InterviewSession> sessions = interviewSessionService.getAllInterviewSession();
        return ResponseEntity.ok(new Response<>(200, "All interview sessions fetched", sessions));
    }

    @PostMapping("/random-questions")
    @Operation(summary = "Lấy ngẫu nhiên câu hỏi dựa trên yêu cầu của người dùng cho buổi interview đó, nếu không nhập sẽ tự chọn từ phía backend")
    public ResponseEntity<Response<SessionWithQuestionsDTO>> getRandomQuestions(@RequestBody QuestionSelectionRequestDTO request) {
        try {
            SessionWithQuestionsDTO dto = interviewSessionService.getRandomQuestions(request);
            return ResponseEntity.ok(new Response<>(200, "Random questions fetched", dto));
        } catch (RuntimeException e) {
            // For known business exceptions
            return ResponseEntity.badRequest().body(new Response<>(400, e.getMessage(), null));
        } catch (Exception e) {
            // For unexpected errors
            return ResponseEntity.status(500).body(new Response<>(500, "Internal server error: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{topic-id}")
    @Operation(summary = "Lấy danh sách các buổi phỏng vấn dựa trên ID chủ đề, chỉ trả về tiêu đề, mô tả và thời lượng")
    public ResponseEntity<Response<InterviewByTopicDTO>> getInterviewSessionByTopicId(@PathVariable("topic-id") Long topicId) {
        InterviewByTopicDTO sessions = interviewSessionService.getInterviewSessionByTopicId(topicId);
        return ResponseEntity.ok(new Response<>(200, "Interview sessions by topic id fetched", sessions));
    }

    @GetMapping("/topics-with-tags")
    @Operation(summary = "Get all topics with their tags")
    public ResponseEntity<Response<List<TopicWithTagsDTO>>> getTopicsWithTags() {
        List<TopicWithTagsDTO> data = interviewSessionService.getAllTopicsWithTags();
        return ResponseEntity.ok(new Response<>(200, "Fetched topics with tags", data));
    }

    @PutMapping("/thumbnail/{id}")
    @Operation(summary = "Đổi thumbnail nè")
    public ResponseEntity<String> updateInterviewSessionThumbnail(@PathVariable Long id, @RequestBody String thumbnailURL) {
        String result = interviewSessionService.updateInterviewSessionThumbnail(id, thumbnailURL);
        return ResponseEntity.ok(result);
    }

}
