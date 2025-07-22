package fsa.project.online_shop.services.implement;

import fsa.project.online_shop.services.FileService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
    private static final String UPLOAD_DIR = "upload/";
    private static final String[] IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};

    public String handleUploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (!isValidImageFile(originalFilename)) {
            throw new IllegalArgumentException("Invalid file type. Only image files are allowed.");
        }
        String newFilename = UUID.randomUUID().toString() + System.currentTimeMillis()  + originalFilename;
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return "/upload/" + newFilename;
    }

    private boolean isValidImageFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        String lowerCaseFilename = filename.toLowerCase();
        for (String extension : IMAGE_EXTENSIONS) {
            if (lowerCaseFilename.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean handleDeleteImage(String filename) throws IOException {
        if (filename == null || filename.isEmpty()) {
            return false;
        }

        // Remove "/upload/" prefix if it exists
        if (filename.startsWith("/upload/")) {
            filename = filename.substring("/upload/".length());
        }

        Path uploadPath = Paths.get(UPLOAD_DIR);
        Path targetPath = uploadPath.resolve(filename);
        return Files.deleteIfExists(targetPath);
    }

}
