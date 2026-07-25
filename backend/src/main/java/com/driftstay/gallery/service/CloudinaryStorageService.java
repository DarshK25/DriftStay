package com.driftstay.gallery.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Cloudinary storage implementation (stub).
 * To be implemented when Cloudinary credentials are available.
 * <p>
 * Steps to implement:
 * 1. Add cloudinary-http44 dependency
 * 2. Configure Cloudinary API keys
 * 3. Implement upload/download/delete methods
 */
@Slf4j
@Component
public class CloudinaryStorageService implements ImageStorageService {

    @Override
    public String getProviderName() {
        return "CLOUDINARY";
    }

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        log.warn("CloudinaryStorageService is not yet implemented.");
        throw new UnsupportedOperationException("Cloudinary storage not implemented yet.");
    }

    @Override
    public List<String> uploadImages(List<MultipartFile> files, String folder) {
        throw new UnsupportedOperationException("Cloudinary storage not implemented yet.");
    }

    @Override
    public boolean deleteImage(String imageUrl) {
        throw new UnsupportedOperationException("Cloudinary storage not implemented yet.");
    }

    @Override
    public String getImageUrl(String imagePath) {
        throw new UnsupportedOperationException("Cloudinary storage not implemented yet.");
    }

    @Override
    public boolean isAccessible() {
        return false;
    }
}
