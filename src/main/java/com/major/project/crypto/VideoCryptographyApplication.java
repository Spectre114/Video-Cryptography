package com.major.project.crypto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class VideoCryptographyApplication {

	public static void main(String[] args) {
		LOGGER.info("Video Cryptography Application Started");
		SpringApplication.run(VideoCryptographyApplication.class, args);
	}


}
