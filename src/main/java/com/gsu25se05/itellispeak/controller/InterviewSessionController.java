package com.gsu25se05.itellispeak.controller;


import com.gsu25se05.itellispeak.dto.interview_session.InterviewSessionDTO;
import com.gsu25se05.itellispeak.entity.InterviewSession;
import com.gsu25se05.itellispeak.entity.Question;
import com.gsu25se05.itellispeak.service.InterviewSessionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    @PostMapping
    public InterviewSession create(@RequestBody InterviewSessionDTO interviewSessionDTO) {
        return interviewSessionService.save(interviewSessionDTO);
    }

    @PostMapping("/{sessionId}/questions/{questionId}")
    public InterviewSession addQuestion(@PathVariable Long sessionId, @PathVariable Long questionId) {
        return interviewSessionService.addQuestionToSession(sessionId, questionId);
    }

    @GetMapping("/{sessionId}/questions")
    public Set<Question> getQuestions(@PathVariable Long sessionId) {
        return interviewSessionService.getQuestions(sessionId);
    }
}
