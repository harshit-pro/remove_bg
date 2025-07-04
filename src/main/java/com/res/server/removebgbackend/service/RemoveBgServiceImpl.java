package com.res.server.removebgbackend.service;

import com.res.server.removebgbackend.client.ClipDropClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class RemoveBgServiceImpl implements RemoveBGService {

    // firstt of all  add api key in application.properties file
    @Value("${clip.apikey}")
    private  String apiKey;

    private final ClipDropClient clipDropClient;
    @Override
    public byte[] removeBackground(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty or missing.");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("ClipDrop API key is missing.");
        }

        try {
            return clipDropClient.removeBackground(file, apiKey);
        } catch (Exception e) {
            System.out.println("Error calling ClipDrop API: " + e.getMessage());
            e.printStackTrace(); // ➕ Add this to see full stack trace on Railway logs
            throw new RuntimeException("Failed to remove background using ClipDrop", e);
        }
    }

}
