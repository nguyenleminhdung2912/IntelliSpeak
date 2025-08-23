package com.gsu25se05.itellispeak.utils;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
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
//    // Dịch chuỗi từ tiếng Anh sang tiếng Việt bằng Google Translate API
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
//            // Trả về văn bản gốc nếu dịch thất bại
//            return text;
//        }
//    }
//
//    // Dịch độ khó (difficulty) sang tiếng Việt bằng Google Translate API
//    public String translateDifficulty(String difficulty) {
//        if (difficulty == null) {
//            return null;
//        }
//        return translateToVietnamese(difficulty);
//    }
}
