package com.andah.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BeanConfigurations {

    @Bean
    public PasswordEncoder passwordEncoder() {
        System.out.println("Creating PasswordEncoder bean in BeanConfigurations");
        return new BCryptPasswordEncoder();
    }
}
