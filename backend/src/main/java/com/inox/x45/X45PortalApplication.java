package com.inox.x45;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class X45PortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(X45PortalApplication.class, args);
    }
}
