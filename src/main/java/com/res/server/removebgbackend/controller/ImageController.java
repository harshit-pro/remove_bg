//package com.res.server.removebgbackend.controller;
//
//import com.res.server.removebgbackend.dto.UserDto;
//import com.res.server.removebgbackend.response.RemoveBgResponse;
//import com.res.server.removebgbackend.service.RemoveBGService;
//import com.res.server.removebgbackend.service.RemoveBgServiceImpl;
//import com.res.server.removebgbackend.service.UserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Base64;
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/images")
//@RequiredArgsConstructor
//public class ImageController {
//    private final RemoveBGService removeBgService;
//    private final UserService userService;
//
//    @PostMapping("/remove-background")
//    public ResponseEntity<?> removeBackgroundImage(@RequestParam("file") MultipartFile file, Authentication auth) {
//        RemoveBgResponse response=null;
//        Map<String,Object> responseMap=new HashMap<>();
//
//        try {
//            if (auth.getName().isEmpty() || auth.getName()==null){
//                response=RemoveBgResponse.builder()
//                        .success(false)
//                        .statusCode(HttpStatus.FORBIDDEN)
//                        .data("Unauthorized access: User not authenticated.")
//                        .build();
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
//            }
//            UserDto userDto = userService.getUserByClerkId(auth.getName());
//            if(userDto.getCredits()==0){
//                responseMap.put("message","Insufficient credits to remove background.");
//                responseMap.put("creditBalance",userDto.getCredits());
//                response=RemoveBgResponse.builder()
//                        .success(false)
//                        .statusCode(HttpStatus.OK)
//                        .data(responseMap)
//                        .build();
//                return ResponseEntity.status(HttpStatus.OK).body(response);
//            }
//            byte[] imageBytes= removeBgService.removeBackground(file);
//            String base64Image= Base64.getEncoder().encodeToString(imageBytes);
//            userDto.setCredits(userDto.getCredits()-1);
//            userService.saveUser(userDto);
//            return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(base64Image);
//        } catch (Exception e) {
//            response=RemoveBgResponse.builder()
//                    .success(false)
//                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .data("Error removing background: " + e.getMessage())
//                    .build();
//            System.out.println("Error removing background: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }
//}
package com.res.server.removebgbackend.controller;
import com.res.server.removebgbackend.dto.UserDto;
import com.res.server.removebgbackend.response.RemoveBgResponse;
import com.res.server.removebgbackend.service.RemoveBGService;
import com.res.server.removebgbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {
    private final RemoveBGService removeBgService;
    private final UserService userService;

    @PostMapping(value = "/remove-background", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> removeBackgroundImage(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {

        try {
            // More robust authentication check
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(RemoveBgResponse.builder()
                                .success(false)
                                .statusCode(HttpStatus.FORBIDDEN)
                                .data("Unauthorized access")
                                .build());
            }

            UserDto userDto = userService.getUserByClerkId(auth.getName());

            if(userDto.getCredits() <= 0) {
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("message", "Insufficient credits");
                responseMap.put("creditBalance", userDto.getCredits());
                return ResponseEntity.ok()
                        .body(RemoveBgResponse.builder()
                                .success(false)
                                .statusCode(HttpStatus.OK)
                                .data(responseMap)
                                .build());
            }

            byte[] imageBytes = removeBgService.removeBackground(file);
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Update credits
            userDto.setCredits(userDto.getCredits() - 1);
            userService.saveUser(userDto);

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(base64Image);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RemoveBgResponse.builder()
                            .success(false)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                            .data("Error: " + e.getMessage())
                            .build());
        }
    }
}