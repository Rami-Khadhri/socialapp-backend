package projetvue.springboot_backend.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file) throws IOException {
        // Validate file type (only image or video allowed)
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.startsWith("video/"))) {
            throw new IllegalArgumentException("Unsupported file type");
        }

        // Generate unique file name
        String uniqueFileName = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(file.getOriginalFilename());
        Path targetLocation = Paths.get(uploadDir).resolve(uniqueFileName);

        // Create the parent directories if they don't exist
        Files.createDirectories(targetLocation.getParent());

        // Write the file to the target location
        try {
            Files.write(targetLocation, file.getBytes());
        } catch (IOException e) {
            throw new IOException("Could not store file " + uniqueFileName, e);
        }

        // Return the URL or relative path for accessing the file
        return "" + uniqueFileName;
    }
    @DeleteMapping("/remove-file")
    public ResponseEntity<Void> removeFile(@RequestBody Map<String, String> request) {
        String fileUrl = request.get("fileUrl");

        try {
            // Call the deleteFile method to remove the file
            deleteFile(fileUrl);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    public void deleteFile(String fileUrl) throws IOException {
        // Extract file name from the URL and create the full file path
        String fileName = fileUrl.replace("/uploads/", "");
        Path filePath = Paths.get(uploadDir, fileName);

        // Delete the file if it exists
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        } else {
            throw new IOException("File not found: " + fileName);
        }
    }
}
