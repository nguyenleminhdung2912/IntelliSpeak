package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.utils.CloudinaryUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("image")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class ImageController {

    @Autowired
    private CloudinaryUtils cloudinaryUtils;

    @PostMapping("/upload")
    public List<String> uploadImages(@RequestParam("images") List<MultipartFile> files) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(cloudinaryUtils.uploadImage(file));
        }
        return urls;
    }
}
