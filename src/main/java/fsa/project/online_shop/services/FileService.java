package fsa.project.online_shop.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    public String handleUploadImage(MultipartFile file) throws IOException;
    public boolean handleDeleteImage(String filename) throws IOException;
}
