package com.gsu25se05.itellispeak.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ICloudinaryUtils {
    String uploadImage(MultipartFile file) throws IOException;
}
