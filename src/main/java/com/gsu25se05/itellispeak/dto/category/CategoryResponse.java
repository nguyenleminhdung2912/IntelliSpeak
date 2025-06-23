package com.gsu25se05.itellispeak.dto.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private long categoryId;
    private String categoryTitle;
    private LocalDateTime createdAt;
}
