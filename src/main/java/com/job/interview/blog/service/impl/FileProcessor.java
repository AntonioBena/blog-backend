package com.job.interview.blog.service.impl;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@Log4j2
public abstract class FileProcessor {

    public String saveToDisc(MultipartFile htmlFile, Long postId) {
        String filePath = "uploads/" + postId.toString() + ".html";
        File file = new File(filePath);

        try{
            var success = file.getParentFile().mkdirs();
        }catch (Exception e){
            throw new RuntimeException("Error while creating directory", e);
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(htmlFile.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error while saving the HTML file", e); //TODO add error handling
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
            System.out.println(e.getMessage());
            return null;
        }
        return null;
    }
}