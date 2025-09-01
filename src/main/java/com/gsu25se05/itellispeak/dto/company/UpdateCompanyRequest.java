package com.gsu25se05.itellispeak.dto.company;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompanyRequest {
    private String name;
    private String shortName;
    private String description;
    private String logoUrl;
    private String website;
}
