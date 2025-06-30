package com.res.server.removebgbackend.service;

import org.springframework.web.multipart.MultipartFile;

public interface RemoveBGService {

     byte[] removeBackground(MultipartFile file);
}
