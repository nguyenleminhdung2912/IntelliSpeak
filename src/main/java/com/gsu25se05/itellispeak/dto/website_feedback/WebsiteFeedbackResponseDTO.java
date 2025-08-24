package com.gsu25se05.itellispeak.dto.website_feedback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WebsiteFeedbackResponseDTO {
    private UUID userID;
    private UUID websiteFeedbackId;
    private String userName;
    private String description;
    private Boolean isHandled;
}
