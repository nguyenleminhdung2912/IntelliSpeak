package com.gsu25se05.itellispeak.dto.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagWithQuestionsDTO {
    private Long id;
    private String title;
    private String description;
    private Boolean isDeleted;
    private List<QuestionDTO> questions;
}
