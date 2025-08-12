package com.gsu25se05.itellispeak.dto.jd;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAllJdDTO {
    private Long jdId;
    private String linkToJd;
    private String summary;
    private String jobtTitle;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDateTime createAt;
}
