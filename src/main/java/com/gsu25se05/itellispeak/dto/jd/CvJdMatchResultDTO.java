package com.gsu25se05.itellispeak.dto.jd;


import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CvJdMatchResultDTO {
    private Integer score;                 // 0..100
    private String verdict;                // STRONG_MATCH | PARTIAL_MATCH | LOW_MATCH

    private Double levelFit;               // 0..1
    private Double domainFit;              // 0..1

    private List<String> matchedMust;
    private List<String> missingMust;
    private List<String> matchedNice;
    private List<String> extraCvSkills;

    private List<String> reasons;          // vì sao ra điểm đó
    private List<String> recommendations;
}
