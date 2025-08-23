package com.gsu25se05.itellispeak.dto.savedPost;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ToggleSaveDTO {
    private Long postId;
    private boolean isSaved;
}
