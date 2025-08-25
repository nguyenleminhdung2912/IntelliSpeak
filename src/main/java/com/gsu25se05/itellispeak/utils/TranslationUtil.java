package com.gsu25se05.itellispeak.utils;

import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.TranslationServiceClient;
import com.google.cloud.translate.v3.TranslationServiceSettings;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.v3.Translation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TranslationUtil {
    private final TranslationServiceClient client;
    private final String projectId;

    // Constructor để khởi tạo TranslationServiceClient với service account key
    public TranslationUtil() throws IOException {
        String credentialsPath = "src\\main\\resources\\translate-key.json";
        String projectId = "mentor-booking-3d46a";
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath))
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/cloud-platform"));
        TranslationServiceSettings settings = TranslationServiceSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();
        this.client = TranslationServiceClient.create(settings);
        this.projectId = projectId;
    }

    // Dịch một chuỗi từ tiếng Anh sang tiếng Việt
    public String translateToVietnamese(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        try {
            LocationName parent = LocationName.of(projectId, "global");
            TranslateTextRequest request = TranslateTextRequest.newBuilder()
                    .setParent(parent.toString())
                    .setSourceLanguageCode("en")
                    .setTargetLanguageCode("vi")
                    .addContents(text)
                    .setMimeType("text/plain")
                    .build();

            TranslateTextResponse response = client.translateText(request);
            return response.getTranslationsList().get(0).getTranslatedText();
        } catch (Exception e) {
            System.err.println("Translation failed for text: " + text + ", error: " + e.getMessage());
            return text; // Trả về văn bản gốc nếu dịch thất bại
        }
    }

    // Dịch hàng loạt chuỗi từ tiếng Anh sang tiếng Việt
    public List<String> translateBatchToVietnamese(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return texts;
        }
        try {
            LocationName parent = LocationName.of(projectId, "global");
            TranslateTextRequest request = TranslateTextRequest.newBuilder()
                    .setParent(parent.toString())
                    .setSourceLanguageCode("en")
                    .setTargetLanguageCode("vi")
                    .setMimeType("text/plain")
                    .addAllContents(texts)
                    .build();

            TranslateTextResponse response = client.translateText(request);
            return response.getTranslationsList().stream()
                    .map(Translation::getTranslatedText)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Batch translation failed: " + e.getMessage());
            return texts; // Trả về danh sách gốc nếu dịch thất bại
        }
    }

    // Dịch độ khó (difficulty) sang tiếng Việt
    public String translateDifficulty(String difficulty) {
        if (difficulty == null) {
            return null;
        }
        return translateToVietnamese(difficulty);
    }

    // Đóng client khi ứng dụng dừng
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
