package com.test.imageupload.rest;

import com.test.imageupload.data.ImageUploadResult;
import com.test.imageupload.services.ImageUploadService;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("image")
@RequiredArgsConstructor
public class ImageUploadController {

    private final ImageUploadService imageUploadService;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ImageUploadResult upload(@RequestParam(value = "files", required = false) MultipartFile[] files,
        @RequestParam(value = "urls", required = false) URL[] urls, @RequestBody(required = false) String base64Images) {

        return imageUploadService.upload(files, urls, base64Images);
    }
}
