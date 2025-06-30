package com.res.server.removebgbackend.controller;

import com.res.server.removebgbackend.dto.UserDto;
import com.res.server.removebgbackend.response.RemoveBgResponse;
import com.res.server.removebgbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
//The @RequiredArgsConstructor annotation is a Lombok feature that automatically
// generates a constructor for all final fields and fields annotated with @NonNull in your class.
public class UserController {

    private final UserService userService;

    @PostMapping("/create-or-update")
    public ResponseEntity<?> createOrUpdateUser(@RequestBody UserDto userDto, Authentication authentication) {


        RemoveBgResponse response=null;
        try {

            if(!authentication.getName().equals(userDto.getClerkId())) {
                System.out.println("Unauthorized access: User ID does not match authenticated user.");
              response=  RemoveBgResponse.builder()
                        .success(false)
                        .statusCode(HttpStatus.FORBIDDEN)
                        .data("Unauthorized access: User ID does not match authenticated user.")
                        .build();
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            UserDto user = userService.saveUser(userDto);
            response= RemoveBgResponse.builder()
                    .success(true)
                    .statusCode(HttpStatus.OK)
                    .data("User created or updated successfully: " + user)
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (RuntimeException e) {
             response=RemoveBgResponse.builder()
                    .success(false)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .data(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/credits")
    public ResponseEntity<?> getUserCredits(Authentication authentication) {
        RemoveBgResponse response=null;
        try {
            if (authentication.getName().isEmpty() || authentication.getName() == null) {
                response = RemoveBgResponse.builder()
                        .success(false)
                        .statusCode(HttpStatus.UNAUTHORIZED)
                        .data("Unauthorized access: No authenticated user found.")
                        .build();
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            String clerkId = authentication.getName();
            UserDto existingUser = userService.getUserByClerkId(clerkId);
            Map<String, Integer> map = new HashMap<>();
            map.put("credits", existingUser.getCredits());
            response = RemoveBgResponse.builder()
                    .success(true)
                    .statusCode(HttpStatus.OK)
                    .data(map)
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            response = RemoveBgResponse.builder()
                    .success(false)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .data("Error retrieving user credits: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}




// The UserController class handles user-related operations such as creating or updating a user.

