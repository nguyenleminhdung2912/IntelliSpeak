package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.website_feedback.WebsiteFeedbackRequestDTO;
import com.gsu25se05.itellispeak.dto.website_feedback.WebsiteFeedbackResponseDTO;
import com.gsu25se05.itellispeak.email.EmailService;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.entity.WebsiteFeedback;
import com.gsu25se05.itellispeak.repository.WebsiteFeedbackRepository;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import com.gsu25se05.itellispeak.utils.mapper.WebsiteFeedbackMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class WebsiteFeedbackService {

    private final WebsiteFeedbackRepository websiteFeedbackRepository;
    private final WebsiteFeedbackMapper websiteFeedbackMapper;
    private final AccountUtils accountUtils;
    private final EmailService emailService;

    public WebsiteFeedbackService(WebsiteFeedbackRepository websiteFeedbackRepository, WebsiteFeedbackMapper websiteFeedbackMapper, AccountUtils accountUtils, EmailService emailService) {
        this.websiteFeedbackRepository = websiteFeedbackRepository;
        this.websiteFeedbackMapper = websiteFeedbackMapper;
        this.accountUtils = accountUtils;
        this.emailService = emailService;
    }

    public Response<WebsiteFeedbackResponseDTO> createWebsiteFeedback(WebsiteFeedbackRequestDTO websiteFeedbackRequestDTO) {

        User account = accountUtils.getCurrentAccount();
        if (account == null) return new Response<>(401, "Please login first", null);

        WebsiteFeedback websiteFeedback = new WebsiteFeedback();
        websiteFeedback.setDescription(websiteFeedbackRequestDTO.getDescription());
        websiteFeedback.setIsHandled(null);
        websiteFeedback.setUser(account);
        websiteFeedback.setCreatedAt(LocalDateTime.now());

        WebsiteFeedback savedFeedback = websiteFeedbackRepository.save(websiteFeedback);

        WebsiteFeedbackResponseDTO responseDTO = new WebsiteFeedbackResponseDTO();
        responseDTO.setDescription(savedFeedback.getDescription());
        responseDTO.setIsHandled(savedFeedback.getIsHandled());
        responseDTO.setUserID(account.getUserId());
        responseDTO.setUserEmail(account.getEmail());
        responseDTO.setCreateAt(savedFeedback.getCreatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
        String fullName = Stream.of(
                        websiteFeedback.getUser().getFirstName(),
                        websiteFeedback.getUser().getLastName()
                )
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));

        responseDTO.setUserName(fullName);
        return new Response<>(200, "Website feedback created successfully", responseDTO);
    }

    public Optional<WebsiteFeedbackResponseDTO> getWebsiteFeedbackById(UUID id) {
        Optional<WebsiteFeedback> websiteFeedback = websiteFeedbackRepository.findById(id);
        if (websiteFeedback.isPresent()) {
            WebsiteFeedbackResponseDTO websiteFeedbackRequestDTO = websiteFeedbackMapper.toDTO(websiteFeedback.get());
            return Optional.of(websiteFeedbackRequestDTO);
        } else {
            return Optional.empty();
        }
    }

    public List<WebsiteFeedbackResponseDTO> getAllWebsiteFeedbacks() {
        List<WebsiteFeedback> websiteFeedbacks = websiteFeedbackRepository.findAll().stream().sorted(Comparator.comparing(WebsiteFeedback::getCreatedAt).reversed()).toList();
        return websiteFeedbacks.stream()
                .map(websiteFeedbackMapper::toDTO)
                .toList();
    }

    public void deleteWebsiteFeedback(UUID id) {
        websiteFeedbackRepository.deleteById(id);
    }

    public String handleRejectWebsiteFeedback(UUID websiteFeedbackId, String reason) {
        WebsiteFeedback websiteFeedback = websiteFeedbackRepository.findById(websiteFeedbackId).orElse(null);
        if (websiteFeedback != null) {
            websiteFeedback.setIsHandled(false);
            websiteFeedbackRepository.save(websiteFeedback);

            String userEmail = websiteFeedback.getUser().getEmail();
            String userName = websiteFeedback.getUser().getFirstName() + " " + websiteFeedback.getUser().getLastName();

            // gọi async gửi mail (không block)
            emailService.handleRejectHandleComplaint(userEmail, userName, reason);

            return "Website feedback rejected successfully";
        }
        return "Something went wrong, please try again";
    }

    public String handleApproveWebsiteFeedback(UUID websiteFeedbackId) {
        WebsiteFeedback websiteFeedback = websiteFeedbackRepository.findById(websiteFeedbackId).orElse(null);
        if (websiteFeedback != null) {
            websiteFeedback.setIsHandled(true);
            websiteFeedbackRepository.save(websiteFeedback);

            String userEmail = websiteFeedback.getUser().getEmail();
            String userName = websiteFeedback.getUser().getFirstName() + " " + websiteFeedback.getUser().getLastName();

            // gọi async gửi mail (không block)
            emailService.handleApproveHandleComplaint(userEmail, userName);

            return "Website feedback approved successfully";
        }
        return "Something went wrong, please try again";
    }
}