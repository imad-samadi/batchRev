package com.example.batchRev;

import com.example.batchRev.app.JobExecutor;
import com.example.batchRev.commons.BatchProperties;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication

public class BatchRevApplication {

	public static void main(String[] args) {
		SpringApplication.run(BatchRevApplication.class, args);
	}
	@Bean
	public ApplicationRunner launchJob(JobExecutor jobExecutor) {
		return args -> jobExecutor.executeSimpleJob();
	}
}
