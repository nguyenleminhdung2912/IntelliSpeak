package com.gsu25se05.itellispeak.dto.reply;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateReplyPostRequestDTO {
    private Long postId;

    private String title;

    private String content;

}
