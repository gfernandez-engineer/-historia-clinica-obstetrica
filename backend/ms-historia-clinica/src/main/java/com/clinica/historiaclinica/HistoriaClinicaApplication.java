package com.clinica.historiaclinica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.clinica.historiaclinica", "com.clinica.shared"})
public class HistoriaClinicaApplication {

    public static void main(String[] args) {
        SpringApplication.run(HistoriaClinicaApplication.class, args);
    }
}
