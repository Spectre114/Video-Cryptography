package com.major.project.crypto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@Slf4j
public class VideoCryptographyApplication {

	public static void main(String[] args) {
		LOGGER.info("Video Cryptography Application Started");
		ConfigurableApplicationContext context = SpringApplication.run(VideoCryptographyApplication.class, args);
		SpringApplication.exit(context, () -> 0);
	}


}
