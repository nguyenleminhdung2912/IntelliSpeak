package com.gsu25se05.itellispeak.dto.apackage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PackageResponseDTO {
    private Long packageId;
    private String packageName;
    private String description;
    private Double price;
    private int interviewCount;
    private int cvAnalyzeCount;
    private int jdAnalyzeCount;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
