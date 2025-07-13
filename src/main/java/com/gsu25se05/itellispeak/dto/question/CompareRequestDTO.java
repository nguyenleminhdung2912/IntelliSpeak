package com.gsu25se05.itellispeak.dto.question;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CompareRequestDTO {
    private String sentence1;
    private String sentence2;

    // Getters & setters
    public String getSentence1() {
        return sentence1;
    }

    public void setSentence1(String sentence1) {
        this.sentence1 = sentence1;
    }

    public String getSentence2() {
        return sentence2;
    }

    public void setSentence2(String sentence2) {
        this.sentence2 = sentence2;
    }
}
