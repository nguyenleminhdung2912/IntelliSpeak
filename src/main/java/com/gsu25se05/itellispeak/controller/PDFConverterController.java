package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.utils.CloudinaryUtils;
import com.gsu25se05.itellispeak.utils.PdfToImageConverter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/pdf-converter")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class PDFConverterController {
    private final CloudinaryUtils cloudinaryUtils;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadCV(@RequestParam("file") MultipartFile file) {
        try {
            String baseName = file.getOriginalFilename()
                    .replaceAll(".pdf", "")
                    .replaceAll("\\s+", "_");

            // Convert PDF -> MultipartFile image(s) in memory
            List<MultipartFile> imageFiles = PdfToImageConverter.convertPdfToMultipartImages(file.getInputStream(), baseName);

            // Upload lên Cloudinary
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile img : imageFiles) {
                imageUrls.add(cloudinaryUtils.uploadImage(img));
            }

            return ResponseEntity.ok(imageUrls);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to convert and upload images.");
        }
    }

}
