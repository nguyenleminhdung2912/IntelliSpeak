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
    private String company;
    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^(0|\\+84)[0-9]{9}$",
            message = "Invalid phone number. Must start with 0 or +84 and be 10 digits"
    )
    private String phone;
    private String country;
    private Integer experienceYears;
    private String linkedinUrl;
    private String cvUrl;
}
