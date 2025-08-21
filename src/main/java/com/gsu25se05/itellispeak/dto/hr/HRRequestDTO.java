package com.gsu25se05.itellispeak.dto.hr;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HRRequestDTO {
    private Long companyId;
    private String companyNameIfNotExist;
    @NotBlank(message = "Please enter your phone number.")
    @Pattern(
            regexp = "^(0|\\+84)[0-9]{9}$",
            message = "Please enter a Viet Nam's phone number."
    )
    private String phone;
    private String country;
    private Integer experienceYears;
    private String linkedinUrl;
    private String cvUrl;
}
