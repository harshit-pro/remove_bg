package com.res.server.removebgbackend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;



@FeignClient(
        name = "clipdropClient",
        url = "https://clipdrop-api.co"
)
public interface ClipDropClient {
    @PostMapping(value = "/remove-background/v1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    byte[] removeBackground(@RequestPart("image_file") MultipartFile file,
                            @RequestHeader("x-api-key") String apiKey);
//    /    MultipartFile is an interface in Spring Framework that represents an uploaded file received in a multipart
//    request — usually from an HTML form or REST client.
//    It is commonly used when you build file upload features
//    (images, documents, etc.) in your web applications.
}