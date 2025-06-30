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
    return clipDropClient.removeBackground(file,apiKey);
    }
}
