package com.gsu25se05.itellispeak.dto.company;

import lombok.Data;

@Data
public class CreateCompanyRequestDTO {
    private String name;
    private String shortName;
    private String description;
    private String logoUrl;
    private String website;
}
