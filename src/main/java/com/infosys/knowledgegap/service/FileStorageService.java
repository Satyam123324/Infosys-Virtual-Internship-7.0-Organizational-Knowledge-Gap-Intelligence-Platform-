package com.infosys.knowledgegap.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> ALLOWED_RESUME_TYPES = Set.of("application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;   // 5 MB
    private static final long MAX_RESUME_SIZE = 10L * 1024 * 1024; // 10 MB

    /** Stores a profile photo under uploads/photos/{userId}/ and returns the public URL path. */
    public String storeProfilePhoto(MultipartFile file, Long userId) throws IOException {
        validate(file, ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE, "Image");
        return store(file, "photos", userId);
    }

    /** Stores a resume under uploads/resumes/{userId}/ and returns the public URL path. */
    public String storeResume(MultipartFile file, Long userId) throws IOException {
        validate(file, ALLOWED_RESUME_TYPES, MAX_RESUME_SIZE, "Resume");
        return store(file, "resumes", userId);
    }

    private void validate(MultipartFile file, Set<String> allowedTypes, long maxSize, String label) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(label + " file is empty");
        }
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException(label + " exceeds the maximum allowed size of "
                    + (maxSize / (1024 * 1024)) + "MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new IllegalArgumentException(label + " must be one of: " + allowedTypes);
        }
    }

    private String store(MultipartFile file, String category, Long userId) throws IOException {
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')) : "";
        String storedFileName = UUID.randomUUID() + extension;

        Path targetDir = Paths.get(uploadDir, category, String.valueOf(userId)).toAbsolutePath().normalize();
        Files.createDirectories(targetDir);

        Path targetFile = targetDir.resolve(storedFileName);
        Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + category + "/" + userId + "/" + storedFileName;
    }
}
