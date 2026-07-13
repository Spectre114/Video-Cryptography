package com.major.project.crypto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class VideoCryptographyApplication {
	public static void main(String[] args) {

		LOGGER.info("Video Cryptography Application Started");
		SpringApplication.run(VideoCryptographyApplication.class, args);
		//TODO add readme.md file
		//TODO add an endpoint to clear the queues
		//TODO make it such that the video that is consumed can be viewed again (retention)
		//TODO make UI better
		//TODO add missing javadocs
		//TODO see if we can leverage database
		//TODO complete unit and integration tests
	}
}
