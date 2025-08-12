package com.gsu25se05.itellispeak.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PdfToImageConverter {

    public static List<MultipartFile> convertPdfToMultipartImages(InputStream pdfInputStream, String baseFilename) throws Exception {
        List<MultipartFile> imageFiles = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfInputStream)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300); // high quality
                MultipartFile multipartImage = bufferedImageToMultipartFile(bim, baseFilename + "_page_" + (page + 1));
                imageFiles.add(multipartImage);
            }
        }

        return imageFiles;
    }

    public static MultipartFile bufferedImageToMultipartFile(BufferedImage image, String filename) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] bytes = baos.toByteArray();

        return new MockMultipartFile(
                filename,                       // name
                filename + ".png",              // original file name
                "image/png",                    // content type
                bytes                           // file content
        );
    }
}
