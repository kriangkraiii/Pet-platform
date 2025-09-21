package com.example.petshop.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class FileStorageUtil {

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    public String storeFile(MultipartFile file, String subDir) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Cannot store empty file");
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir, subDir);
            Files.createDirectories(uploadPath);

            // Generate unique filename
            String originalFileName = file.getOriginalFilename();
            String extension = originalFileName != null && originalFileName.contains(".") 
                    ? originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
            String fileName = UUID.randomUUID().toString() + extension;

            // Copy file to target location
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return subDir + "/" + fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }
}
