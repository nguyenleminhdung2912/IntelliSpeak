package com.gsu25se05.itellispeak.dto.cv;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HRViewSubmittedCvDTO {
    private Long cvSubmissionId;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String memberCvTitle;
    private String memberCvLinkToCv;
    private String jobTitle;
    private Boolean isViewed;
    private LocalDateTime submittedAt;
}