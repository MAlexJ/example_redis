package com.malex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableScheduling
@SpringBootApplication
public class ApplicationPropertiesApplication {

  public static void main(String[] args) {
    SpringApplication.run(ApplicationPropertiesApplication.class, args);
  }
}
