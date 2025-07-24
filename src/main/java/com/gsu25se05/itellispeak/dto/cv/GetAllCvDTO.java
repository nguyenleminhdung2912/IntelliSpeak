package com.gsu25se05.itellispeak.dto.cv;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAllCvDTO {

    private String overallScore;
    private String imageUrls;
    private String cvTitle;
    private boolean isActive;

}
