package com.poolapp.pool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PoolApplication {
    public static void main(String[] args) {
        SpringApplication.run(PoolApplication.class, args);
    }
}

