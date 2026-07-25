package com.driftstay.gallery.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * AWS S3 storage implementation (stub).
 * To be implemented when AWS credentials are available.
 * <p>
 * Steps to implement:
 * 1. Add spring-cloud-starter-aws dependency
 * 2. Configure AWS credentials
 * 3. Create S3 bucket
 * 4. Implement upload/download/delete methods
 */
@Slf4j
@Component
public class S3StorageService implements ImageStorageService {

    @Override
    public String getProviderName() {
        return "S3";
    }

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        log.warn("S3StorageService is not yet implemented. Falling back to local storage.");
        throw new UnsupportedOperationException("S3 storage not implemented yet. Configure AWS credentials.");
    }

    @Override
    public List<String> uploadImages(List<MultipartFile> files, String folder) {
        throw new UnsupportedOperationException("S3 storage not implemented yet.");
    }

    @Override
    public boolean deleteImage(String imageUrl) {
        throw new UnsupportedOperationException("S3 storage not implemented yet.");
    }

    @Override
    public String getImageUrl(String imagePath) {
        throw new UnsupportedOperationException("S3 storage not implemented yet.");
    }

    @Override
    public boolean isAccessible() {
        return false;
    }
}
