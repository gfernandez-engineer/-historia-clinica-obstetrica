package com.clinica.exportacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.clinica.exportacion", "com.clinica.shared"})
public class ExportacionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExportacionApplication.class, args);
    }
}
