package com.gsu25se05.itellispeak.dto.hr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HRResponseDTO {
    private Long hrId;
    private String company;
    private String phone;
    private String country;
    private Integer experienceYears;
    private String linkedinUrl;
    private String cvUrl;
    private LocalDateTime submittedAt;
}
