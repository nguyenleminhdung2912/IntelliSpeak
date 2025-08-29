package com.gsu25se05.itellispeak.dto.cv;

import lombok.Data;

@Data
public class CandidateSubmittedCvDTO {
    private Long cvSubmissionId;
    private String memberCvTitle;
    private String memberCvLinkToCv;
    private Long companyId;
    private String companyName;
    private String companyLogoUrl;
}
