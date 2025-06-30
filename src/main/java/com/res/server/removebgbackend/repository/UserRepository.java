package com.res.server.removebgbackend.repository;

import com.res.server.removebgbackend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
     Optional<UserEntity> findByClerkId(String clerkId);

    boolean existsByClerkId(String clerkId);
}
