package com.gsu25se05.itellispeak.dto.website_feedback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WebsiteFeedbackResponseDTO {
    private Long userID;
    private Long websiteFeedbackId;
    private String userName;
    private String userEmail;
    private String description;
    private Boolean isHandled;
    private String createAt;
}
