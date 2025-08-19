package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.ai_evaluation.EvaluationBatchResponseDto;
import com.gsu25se05.itellispeak.entity.InterviewHistory;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.repository.InterviewHistoryRepository;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import com.gsu25se05.itellispeak.utils.mapper.InterviewHistoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterviewHistoryService {

    private final InterviewHistoryRepository interviewHistoryRepository;
    private final InterviewHistoryMapper interviewHistoryMapper;
    private final AccountUtils accountUtils;

    public InterviewHistoryService(
            InterviewHistoryRepository interviewHistoryRepository,
            InterviewHistoryMapper interviewHistoryMapper,
            AccountUtils accountUtils) {
        this.interviewHistoryRepository = interviewHistoryRepository;
        this.interviewHistoryMapper = interviewHistoryMapper;
        this.accountUtils = accountUtils;
    }

    public List<EvaluationBatchResponseDto> getAllInterviewHistories() {
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            throw new IllegalStateException("Please log in to continue");
        }

        List<InterviewHistory> histories = interviewHistoryRepository.findByUser(currentUser);
        return histories.stream()
                .map(interviewHistoryMapper::toEvaluationBatchResponseForGetAllDto)
                .collect(Collectors.toList());
    }

    public EvaluationBatchResponseDto getInterviewHistoryById(Long id) {
        User currentUser = accountUtils.getCurrentAccount();
        if (currentUser == null) {
            throw new IllegalStateException("Please log in to continue");
        }

        InterviewHistory history = interviewHistoryRepository.findById(id)
                .filter(h -> h.getUser().equals(currentUser))
                .orElseThrow(() -> new IllegalArgumentException(
                        "InterviewHistory not found with ID: " + id + " or does not belong to the current user"
                ));

        return interviewHistoryMapper.toEvaluationBatchResponseDto(history);
    }
}
