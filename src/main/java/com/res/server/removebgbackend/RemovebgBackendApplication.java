package com.res.server.removebgbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class RemovebgBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RemovebgBackendApplication.class, args);
    }

}
