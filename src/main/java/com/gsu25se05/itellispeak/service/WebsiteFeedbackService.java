package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.website_feedback.WebsiteFeedbackDTO;
import com.gsu25se05.itellispeak.entity.WebsiteFeedback;
import com.gsu25se05.itellispeak.repository.WebsiteFeedbackRepository;
import com.gsu25se05.itellispeak.utils.mapper.WebsiteFeedbackMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WebsiteFeedbackService {

    private final WebsiteFeedbackRepository websiteFeedbackRepository;
    private final WebsiteFeedbackMapper websiteFeedbackMapper;

    public WebsiteFeedbackService(WebsiteFeedbackRepository websiteFeedbackRepository, WebsiteFeedbackMapper websiteFeedbackMapper) {
        this.websiteFeedbackRepository = websiteFeedbackRepository;
        this.websiteFeedbackMapper = websiteFeedbackMapper;
    }

    public WebsiteFeedbackDTO createWebsiteFeedback(WebsiteFeedbackDTO websiteFeedbackDTO) {
        WebsiteFeedback websiteFeedback = new WebsiteFeedback();
        websiteFeedback.setExpression(websiteFeedbackDTO.getExpression());
        websiteFeedback.setDescription(websiteFeedbackDTO.getDescription());

        WebsiteFeedback savedFeedback = websiteFeedbackRepository.save(websiteFeedback);

        WebsiteFeedbackDTO responseDTO = new WebsiteFeedbackDTO();
        responseDTO.setExpression(savedFeedback.getExpression());
        responseDTO.setDescription(savedFeedback.getDescription());
        return responseDTO;
    }

    public Optional<WebsiteFeedbackDTO> getWebsiteFeedbackById(UUID id) {
        Optional<WebsiteFeedback> websiteFeedback = websiteFeedbackRepository.findById(id);
        if (websiteFeedback.isPresent()) {
            WebsiteFeedbackDTO websiteFeedbackDTO = websiteFeedbackMapper.toDTO(websiteFeedback.get());
            return Optional.of(websiteFeedbackDTO);
        } else {
            return Optional.empty();
        }
    }

    public List<WebsiteFeedbackDTO> getAllWebsiteFeedbacks() {
        List<WebsiteFeedback> websiteFeedbacks = websiteFeedbackRepository.findAll();
        return websiteFeedbacks.stream()
                .map(websiteFeedbackMapper::toDTO)
                .toList();
    }

    public void deleteWebsiteFeedback(UUID id) {
        websiteFeedbackRepository.deleteById(id);
    }
}