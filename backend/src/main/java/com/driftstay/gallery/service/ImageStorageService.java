package com.driftstay.gallery.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Strategy interface for image storage.
 * Implementations can use different backends:
 * - LocalStorageService: Saves to local filesystem (dev/test)
 * - S3StorageService: Uploads to AWS S3 (production)
 * - CloudinaryStorageService: Uploads to Cloudinary (production)
 *
 * Files are never saved directly to the application directory.
 */
public interface ImageStorageService {

    /**
     * The provider name identifier.
     */
    String getProviderName();

    /**
     * Upload an image to the storage backend.
     *
     * @param file       The image file to upload
     * @param folder     The folder/directory to store in
     * @return The URL/path of the stored image
     */
    String uploadImage(MultipartFile file, String folder);

    /**
     * Upload multiple images at once.
     *
     * @param files      List of image files to upload
     * @param folder     The folder/directory to store in
     * @return List of URLs/paths of the stored images
     */
    List<String> uploadImages(List<MultipartFile> files, String folder);

    /**
     * Delete an image from storage.
     *
     * @param imageUrl The URL/path of the image to delete
     * @return true if deletion was successful
     */
    boolean deleteImage(String imageUrl);

    /**
     * Get the public URL for a stored image.
     *
     * @param imagePath The internal path/identifier
     * @return The public-facing URL
     */
    String getImageUrl(String imagePath);

    /**
     * Check if the storage backend is accessible.
     */
    boolean isAccessible();
}
