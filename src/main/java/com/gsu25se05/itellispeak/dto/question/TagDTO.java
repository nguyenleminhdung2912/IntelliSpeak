package com.gsu25se05.itellispeak.dto.question;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private Boolean isDeleted;
}
