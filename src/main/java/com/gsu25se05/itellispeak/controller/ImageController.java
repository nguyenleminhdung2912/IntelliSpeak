package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.utils.CloudinaryUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("image")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class ImageController {

    @Autowired
    private CloudinaryUtils cloudinaryUtils;

    @PostMapping("/upload")
    public String uploadImage(@RequestParam("image") MultipartFile file) throws IOException {
        return cloudinaryUtils.uploadImage(file);
    }
}
