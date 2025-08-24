package com.gsu25se05.itellispeak.utils.mapper;

import com.gsu25se05.itellispeak.dto.website_feedback.WebsiteFeedbackRequestDTO;
import com.gsu25se05.itellispeak.dto.website_feedback.WebsiteFeedbackResponseDTO;
import com.gsu25se05.itellispeak.entity.WebsiteFeedback;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class WebsiteFeedbackMapper {

    public WebsiteFeedbackResponseDTO toDTO(WebsiteFeedback websiteFeedback) {
        if (websiteFeedback == null) return null;
        WebsiteFeedbackResponseDTO dto = new WebsiteFeedbackResponseDTO();
        dto.setWebsiteFeedbackId(websiteFeedback.getWebsiteFeedbackId());
        dto.setDescription(websiteFeedback.getDescription());
        dto.setUserEmail(websiteFeedback.getUser().getEmail());
        String fullName = Stream.of(
                        websiteFeedback.getUser().getFirstName(),
                        websiteFeedback.getUser().getLastName()
                )
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));

        dto.setUserName(fullName);
        dto.setUserID(websiteFeedback.getUser().getUserId());
        dto.setIsHandled(websiteFeedback.getIsHandled());
        return dto;
    }
}
