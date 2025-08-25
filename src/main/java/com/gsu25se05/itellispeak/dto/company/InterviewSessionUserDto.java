package com.gsu25se05.itellispeak.dto.company;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSessionUserDto {
    private Long id;
    private String name;
    private String avatar;
}
