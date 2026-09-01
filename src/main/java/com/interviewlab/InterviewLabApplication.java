package com.interviewlab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.*;

@SpringBootApplication
@EnableCaching
public class InterviewLabApplication {

	public static void main(String[] args) {
		SpringApplication.run(InterviewLabApplication.class, args);
	}

}
