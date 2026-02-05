package fsa.project.online_shop.services.implement;

import fsa.project.online_shop.services.FileService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final Cloudinary cloudinary;
    private static final String[] IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};

    @Override
    public String handleUploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (!isValidImageFile(originalFilename)) {
            throw new IllegalArgumentException("Invalid file type. Only image files are allowed.");
        }
        
        // Upload to Cloudinary
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return (String) uploadResult.get("secure_url");
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
    public boolean handleDeleteImage(String url) throws IOException {
        if (url == null || url.isEmpty()) {
            return false;
        }

        try {
            // Extract public ID from URL
            String publicId = extractPublicIdFromUrl(url);
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return "ok".equals(result.get("result"));
        } catch (Exception e) {
            // Log error?
            return false;
        }
    }

    private String extractPublicIdFromUrl(String url) {
        // Example URL: https://res.cloudinary.com/cloudname/image/upload/v1234567890/public_id.jpg
        // We need 'public_id'
        try {
            int lastSlashIndex = url.lastIndexOf('/');
            int lastDotIndex = url.lastIndexOf('.');
            if (lastSlashIndex != -1 && lastDotIndex != -1 && lastDotIndex > lastSlashIndex) {
                 return url.substring(lastSlashIndex + 1, lastDotIndex);
            }
        } catch (Exception e) {
            // fallback or return null
        }
        return null;
    }
}
