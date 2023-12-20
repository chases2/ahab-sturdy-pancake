package com.google.cloud.releng.ahabpublisher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AhabStreamingSubscriberApplication {

	final static String TOPIC_NAME = "ahab";

	public static void main(String[] args) {
		SpringApplication.run(AhabStreamingSubscriberApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			System.out.println("Service is running...");
		};
	}

}
