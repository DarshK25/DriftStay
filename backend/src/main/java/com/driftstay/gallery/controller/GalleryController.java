package com.driftstay.gallery.controller;

import com.driftstay.gallery.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/gallery")
@RequiredArgsConstructor
public class GalleryController {

    private final ImageStorageService imageStorageService;

    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(defaultValue = "general") String folder) {
        List<String> urls = imageStorageService.uploadImages(files, folder);
        return ResponseEntity.ok(urls);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteImage(@RequestParam String imageUrl) {
        imageStorageService.deleteImage(imageUrl);
        return ResponseEntity.noContent().build();
    }
}
