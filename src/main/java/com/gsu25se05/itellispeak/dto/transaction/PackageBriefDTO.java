package com.gsu25se05.itellispeak.dto.transaction;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageBriefDTO {
    private Long packageId;
    private String packageName;
    private String description;
    private Double price;
    private Integer interviewCount;
    private Integer cvAnalyzeCount;
    private Integer jdAnalyzeCount;
}
