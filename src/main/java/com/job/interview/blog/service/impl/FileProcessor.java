package com.job.interview.blog.service.impl;

import com.job.interview.blog.exception.FileValidationException;
import com.job.interview.blog.exception.ResourceNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;

@Log4j2
public abstract class FileProcessor {
    private static final long MAX_FILE_SIZE = 500 * 1024; // 500KB limit //TODO can be moved to app props for easier maintain
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("text/html", "application/xhtml+xml");

    public void validateAndSanitize(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileValidationException("File is empty!");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new FileValidationException("Invalid file type! Only HTML files are allowed.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileValidationException("File is too large! Max 5MB allowed.");
        }
        //TODO sanitize html content - did not found any java html sanitizer without vulnerabilities!
    }

    public String saveToDisc(MultipartFile htmlFile, Long postId) {
        String filePath = "uploads/" + postId.toString() + ".html"; //TODO can be moved to applicationProperties
        File file = new File(filePath);

        try{
            var success = file.getParentFile().mkdirs();
        }catch (Exception e){
            throw new RuntimeException("Error while creating directory", e);
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(htmlFile.getBytes());
        } catch (IOException e) {
            throw new FileValidationException("Error while saving the HTML file", e);
        }

        log.info("File saved at: {}", filePath);
        return filePath;
    }

    public Resource downloadFile(String filePath) {
        File dir = new File(filePath);
        try{
            if(dir.exists()){
                Resource resource = new UrlResource(dir.toURI());
                return resource;
            }
        }catch (Exception e){
            throw new ResourceNotFoundException("Error while getting html content", e);
        }
        return null;
    }
}