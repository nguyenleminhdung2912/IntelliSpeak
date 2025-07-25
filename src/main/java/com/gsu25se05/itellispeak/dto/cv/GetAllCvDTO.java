package com.gsu25se05.itellispeak.dto.cv;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAllCvDTO {

    private Long id;
    private String overallScore;
    private String imageUrls;
    private String cvTitle;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDateTime createAt;
    private boolean isActive;

}
