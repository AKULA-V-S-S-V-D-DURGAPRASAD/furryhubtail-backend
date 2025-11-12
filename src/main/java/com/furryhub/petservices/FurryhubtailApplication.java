package com.furryhub.petservices;

import com.furryhub.petservices.config.CustomBanner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FurryhubtailApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(FurryhubtailApplication.class);
        app.setBanner(new CustomBanner());
        app.run(args);
    }
}