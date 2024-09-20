package com.example.smartcloset.global.common.controller;

import com.example.smartcloset.global.common.response.ApiResponse;
import com.example.smartcloset.global.common.service.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

@RestController
@RequiredArgsConstructor
@RequestMapping("/s3")
@CrossOrigin
public class AmazonS3Controller {

    private final AwsS3Service awsS3Service;

    @PostMapping("/image")
    public ResponseEntity<String> uploadImage(@RequestParam("multipartFile") MultipartFile multipartFile) {
        return ApiResponse.success(awsS3Service.uploadImage(multipartFile));
    }

    @DeleteMapping("/image/delete")
    public ResponseEntity<Void> deleteImage(@RequestParam String fileName) {
        awsS3Service.deleteImage(fileName);
        return ApiResponse.success(null);
    }
}