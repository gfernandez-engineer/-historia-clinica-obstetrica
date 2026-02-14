package com.clinica.transcripcion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.clinica.transcripcion", "com.clinica.shared"})
public class TranscripcionApplication {

    public static void main(String[] args) {
        SpringApplication.run(TranscripcionApplication.class, args);
    }
}
