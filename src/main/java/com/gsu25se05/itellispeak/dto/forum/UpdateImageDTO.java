package com.gsu25se05.itellispeak.dto.forum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateImageDTO {
    private Long id;       // null nếu là ảnh mới
    private String url;    // URL mới hoặc hiện tại
}
