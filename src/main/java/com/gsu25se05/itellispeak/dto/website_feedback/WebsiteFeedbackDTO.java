package com.gsu25se05.itellispeak.dto.website_feedback;

import com.gsu25se05.itellispeak.entity.WebsiteFeedback;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WebsiteFeedbackDTO {
    private WebsiteFeedback.expressions expression;
    private String description;
}
