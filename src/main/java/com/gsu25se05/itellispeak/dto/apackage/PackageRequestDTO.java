package com.gsu25se05.itellispeak.dto.apackage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PackageRequestDTO {
    private String packageName;
    private String description;
    private Double price;
    private Integer interviewCount;
    private Integer cvAnalyzeCount;
    private Integer jdAnalyzeCount;
}
