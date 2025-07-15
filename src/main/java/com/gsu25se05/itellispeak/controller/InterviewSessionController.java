package com.gsu25se05.itellispeak.controller;


import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.interview_session.InterviewSessionDTO;
import com.gsu25se05.itellispeak.entity.InterviewSession;
import com.gsu25se05.itellispeak.entity.Question;
import com.gsu25se05.itellispeak.service.InterviewSessionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Response<InterviewSession>> create(@RequestBody InterviewSessionDTO interviewSessionDTO) {
        InterviewSession session = interviewSessionService.save(interviewSessionDTO);
        return ResponseEntity.ok(new Response<>(200, "Interview session created", session));
    }

    @PostMapping("/{sessionId}/questions/{questionId}")
    public ResponseEntity<Response<InterviewSession>> addQuestion(@PathVariable Long sessionId, @PathVariable Long questionId) {
        InterviewSession session = interviewSessionService.addQuestionToSession(sessionId, questionId);
        return ResponseEntity.ok(new Response<>(200, "Question added to session", session));
    }

    @PostMapping("/{sessionId}/questions")
    public ResponseEntity<Response<InterviewSession>> addQuestions(@PathVariable Long sessionId, @RequestBody Set<Question> questions) {
        InterviewSession session = interviewSessionService.addQuestionsToSession(sessionId, questions);
        return ResponseEntity.ok(new Response<>(200, "Questions added to session", session));
    }

    @GetMapping("/{sessionId}/questions")
    public ResponseEntity<Response<Set<Question>>> getQuestions(@PathVariable Long sessionId) {
        Set<Question> questions = interviewSessionService.getQuestions(sessionId);
        return ResponseEntity.ok(new Response<>(200, "Questions fetched", questions));
    }

    @GetMapping("/sessions/get-all")
    public ResponseEntity<Response<Iterable<InterviewSession>>> getAllSessions() {
        Iterable<InterviewSession> sessions = interviewSessionService.getAllInterviewSession();
        return ResponseEntity.ok(new Response<>(200, "All interview sessions fetched", sessions));
    }
}
