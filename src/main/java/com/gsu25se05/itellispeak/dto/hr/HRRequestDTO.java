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
    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(
            regexp = "^(0|\\+84)[0-9]{9}$",
            message = "Số điện thoại không đúng định dạng nhà mạng Việt Nam"
    )
    private String phone;
    private String country;
    private Integer experienceYears;
    private String linkedinUrl;
    private String cvUrl;
}
