package com.vlog.vlog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VlogApplication {

	public static void main(String[] args) {
		SpringApplication.run(VlogApplication.class, args);
	}

}
