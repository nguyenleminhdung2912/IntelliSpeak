package com.gsu25se05.itellispeak.dto.jd;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JDInputDTO {

    @Nullable
    private String linkToJd;       // Có thể null
    private String jdRawContent;   // Có thể null
}
