package com.res.server.removebgbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clerk_id", unique = true, nullable = false)
    private String clerkId;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "credits")
    private Integer credits;

    @Column(name = "photo_url", length = 1000) // Increased length for URL
    private String photoUrl;

    @PrePersist
    public void prePersist() {
        if (credits == null) {
            credits = 5;
        }
    }
}