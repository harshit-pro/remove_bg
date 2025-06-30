package com.res.server.removebgbackend.service;

import com.res.server.removebgbackend.dto.UserDto;
import com.res.server.removebgbackend.entity.UserEntity;
import com.res.server.removebgbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

   private final UserRepository userRepository;

    @Override
    public UserDto saveUser(UserDto userDto) {
        System.out.println("Saving user with data: " + userDto); // Add this

        Optional<UserEntity> optionalUser = userRepository.findByClerkId(userDto.getClerkId());
        if (optionalUser.isPresent()) {
            UserEntity existingUser = optionalUser.get();
            System.out.println("Existing user before update: " + existingUser); // Add this

            existingUser.setEmail(userDto.getEmail());
            existingUser.setUsername(userDto.getUsername());
            existingUser.setFirstName(userDto.getFirstName());
            existingUser.setLastName(userDto.getLastName());
            existingUser.setPhotoUrl(userDto.getPhotoUrl());
            if (userDto.getCredits() != null) {
                existingUser.setCredits(userDto.getCredits());
            }

            UserEntity savedUser = userRepository.save(existingUser);
            System.out.println("Existing user after update: " + savedUser); // Add this
            return mapToDto(savedUser);
        }

        UserEntity newUser = mapTOEntity(userDto);
        UserEntity savedNewUser = userRepository.save(newUser);
        return mapToDto(savedNewUser);
    }
    @Override
    public UserDto getUserByClerkId(String clerkId) {
      UserEntity userEntity= userRepository.findByClerkId(clerkId).orElseThrow(()->
                new UsernameNotFoundException("User not found with clerkId: " + clerkId));
        return mapToDto(userEntity);
    }
    @Override
    public void deleteUserByClerkId(String clerkId) {
      UserEntity userEntity=userRepository.findByClerkId(clerkId).orElseThrow(()->
          new UsernameNotFoundException("User not found with clerkId: " + clerkId));
        userRepository.delete(userEntity);

    }
    private UserEntity mapTOEntity(UserDto userDto) {
    return  UserEntity.builder()
            .clerkId(userDto.getClerkId())
            .username(userDto.getUsername())
            .firstName(userDto.getFirstName())
            .lastName(userDto.getLastName())
            .email(userDto.getEmail())
            .photoUrl(userDto.getPhotoUrl())
            .credits(userDto.getCredits() != null ? userDto.getCredits() : 5)
            .build();
    }

    private UserDto mapToDto(UserEntity userEntity) {
        return UserDto.builder()
                .clerkId(userEntity.getClerkId())
                .username(userEntity.getUsername())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .email(userEntity.getEmail())
                .photoUrl(userEntity.getPhotoUrl())
                .credits(userEntity.getCredits())
                .build();
    }
}
