package com.gsu25se05.itellispeak.utils;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.gsu25se05.itellispeak.entity.Difficulty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TranslationUtil {

//    private final Translate translate;
//
//    // Constructor để khởi tạo Google Translate API với API key từ cấu hình
//    public TranslationUtil(@Value("${google.cloud.api-key}") String apiKey) {
//        this.translate = TranslateOptions.newBuilder().setApiKey(apiKey).build().getService();
//    }
//
//    // Dịch chuỗi từ tiếng Anh sang tiếng Việt
//    public String translateToVietnamese(String text) {
//        if (text == null || text.isEmpty()) {
//            return text;
//        }
//        try {
//            Translation translation = translate.translate(
//                    text,
//                    Translate.TranslateOption.sourceLanguage("en"),
//                    Translate.TranslateOption.targetLanguage("vi")
//            );
//            return translation.getTranslatedText();
//        } catch (Exception e) {
//            System.err.println("Translation failed for text: " + text + ", error: " + e.getMessage());
//            return text; // Trả về văn bản gốc nếu dịch thất bại
//        }
//    }
//
//    // Dịch độ khó (difficulty) sang tiếng Việt
//    public String translateDifficulty(String difficulty) {
//        if (difficulty.equals(Difficulty.EASY)) {
//            return "Dễ";
//        }
//        else if (difficulty.equals(Difficulty.MEDIUM)) {
//            return "Trung bình";
//        }
//        else if (difficulty.equals(Difficulty.HARD)) {
//            return "Khó";
//        }
//        return translateToVietnamese(difficulty);
//    }
}
