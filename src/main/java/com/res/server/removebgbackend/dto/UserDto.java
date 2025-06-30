package com.res.server.removebgbackend.dto;

import jakarta.persistence.Column;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserDto {
    private String clerkId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String photoUrl;
    private Integer credits;
}
