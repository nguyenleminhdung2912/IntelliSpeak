package com.gsu25se05.itellispeak.dto.topic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopicWithTagsDTO {
    private Long topicId;
    private String title;
    private List<TagSimpleDTO> tags;
}
