package com.irrigation.erp.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class ErpBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ErpBackendApplication.class, args);
	}

}
