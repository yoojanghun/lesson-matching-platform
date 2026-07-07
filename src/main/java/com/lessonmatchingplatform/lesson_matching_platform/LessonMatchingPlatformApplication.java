package com.lessonmatchingplatform.lesson_matching_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class LessonMatchingPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(LessonMatchingPlatformApplication.class, args);
	}
    
}
