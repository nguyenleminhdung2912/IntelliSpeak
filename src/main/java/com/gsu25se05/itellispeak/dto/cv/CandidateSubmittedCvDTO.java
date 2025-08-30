package com.gsu25se05.itellispeak.dto.cv;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CandidateSubmittedCvDTO {
    private Long cvSubmissionId;
    private String memberCvTitle;
    private String memberCvLinkToCv;
    private Long companyId;
    private String companyName;
    private String companyLogoUrl;
    private Boolean isViewed;
    private String jobTitle;
    private LocalDateTime submittedAt;
}
