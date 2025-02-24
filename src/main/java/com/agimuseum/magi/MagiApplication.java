package com.agimuseum.magi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MagiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MagiApplication.class, args);
	}

}
