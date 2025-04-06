package com.example.ss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SsApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ioc = SpringApplication.run(SsApplication.class, args);
        System.out.println(111);
    }

}
