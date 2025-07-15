package com.gsu25se05.itellispeak.utils.mapper;

import com.gsu25se05.itellispeak.dto.website_feedback.WebsiteFeedbackDTO;
import com.gsu25se05.itellispeak.entity.WebsiteFeedback;
import org.springframework.stereotype.Component;

@Component
public class WebsiteFeedbackMapper {

    public WebsiteFeedbackDTO toDTO(WebsiteFeedback websiteFeedback) {
        if (websiteFeedback == null) return null;
        WebsiteFeedbackDTO dto = new WebsiteFeedbackDTO();
        dto.setExpression(websiteFeedback.getExpression());
        dto.setDescription(websiteFeedback.getDescription());
        return dto;
    }

    public WebsiteFeedback toEntity(WebsiteFeedbackDTO dto) {
        if (dto == null) return null;
        WebsiteFeedback websiteFeedback = new WebsiteFeedback();
        websiteFeedback.setExpression(dto.getExpression());
        websiteFeedback.setDescription(dto.getDescription());
        return websiteFeedback;
    }

}
