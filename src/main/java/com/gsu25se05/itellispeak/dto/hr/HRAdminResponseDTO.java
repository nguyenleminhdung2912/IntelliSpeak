package com.gsu25se05.itellispeak.dto.hr;

import com.gsu25se05.itellispeak.entity.HRStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HRAdminResponseDTO {
    private Long hrId;
    private String fullName;
    private String email;
    private String company;
    private String phone;
    private String country;
    private Integer experienceYears;
    private String linkedinUrl;
    private String cvUrl;
    private HRStatus status;
    private LocalDateTime submittedAt;
}
