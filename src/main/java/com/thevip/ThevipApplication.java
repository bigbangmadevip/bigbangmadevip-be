package com.thevip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ThevipApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThevipApplication.class, args);
	}

}
