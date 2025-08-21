package com.gsu25se05.itellispeak.dto.company;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gsu25se05.itellispeak.dto.ai_evaluation.InterviewSessionDto;
import com.gsu25se05.itellispeak.dto.hr.HRResponseDTO;
import com.gsu25se05.itellispeak.dto.interview_session.InterviewSessionDTO;
import com.gsu25se05.itellispeak.entity.HR;
import com.gsu25se05.itellispeak.entity.InterviewSession;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class GetCompanyDetailResponseDTO {
    private Long companyId;
    private String name;
    private String shortName;
    private String description;
    private String logoUrl;
    private String website;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private Boolean isDeleted = false;
    private List<HRResponseDTO> hrList = new ArrayList<>();
    private List<InterviewSessionDto> interviewTemplateList = new ArrayList<>();
}
