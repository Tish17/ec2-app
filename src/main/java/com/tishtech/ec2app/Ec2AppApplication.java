package com.tishtech.ec2app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class Ec2AppApplication {

    public static void main(String[] args) {
        SpringApplication.run(Ec2AppApplication.class, args);
    }

}
