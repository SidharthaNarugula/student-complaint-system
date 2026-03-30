package com.example.compsysten;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
public class  CompSystenApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompSystenApplication.class, args);
    }

}
