package com.driftstay.gallery.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Local filesystem storage implementation.
 * For development and testing only - never use in production!
 * Files are stored in the configured upload directory.
 */
@Slf4j
@Component
@org.springframework.context.annotation.Primary
public class LocalStorageService implements ImageStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8000}")
    private String baseUrl;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath();
        try {
            Files.createDirectories(uploadPath);
            log.info("Local storage initialized at: {}", uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadPath, e);
        }
    }

    @Override
    public String getProviderName() {
        return "LOCAL";
    }

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        validateFile(file);

        String filename = generateFilename(file.getOriginalFilename());
        Path targetPath = resolvePath(folder, filename);

        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Image saved locally: {}", targetPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + filename, e);
        }

        return getImagePath(folder, filename);
    }

    @Override
    public List<String> uploadImages(List<MultipartFile> files, String folder) {
        return files.stream()
                .map(file -> uploadImage(file, folder))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteImage(String imageUrl) {
        try {
            // Extract the relative path from the URL
            String relativePath = imageUrl.replace(baseUrl + "/", "");
            Path filePath = uploadPath.resolve(relativePath);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Failed to delete image: {}", imageUrl, e);
            return false;
        }
    }

    @Override
    public String getImageUrl(String imagePath) {
        return baseUrl + "/" + imagePath;
    }

    @Override
    public boolean isAccessible() {
        return Files.isReadable(uploadPath) && Files.isWritable(uploadPath);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
    }

    private String generateFilename(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    private Path resolvePath(String folder, String filename) {
        if (folder != null && !folder.isBlank()) {
            return uploadPath.resolve(folder).resolve(filename);
        }
        return uploadPath.resolve(filename);
    }

    private String getImagePath(String folder, String filename) {
        if (folder != null && !folder.isBlank()) {
            return folder + "/" + filename;
        }
        return filename;
    }
}
