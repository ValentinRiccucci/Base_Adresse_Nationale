package com.natsystem.BAN;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@SpringBootApplication
@Component
public class BanApplication {

	static void main(String[] args) {
		SpringApplication.run(BanApplication.class, args);
		System.out.println("BAN Application Started");
	}

	@Bean
	public CommandLineRunner run(JobOperator jobOperator, Job importFranceJob) {
		return args -> {
			JobParameters params = new JobParametersBuilder()
					.addLong("startAt", System.currentTimeMillis())
					.addLong("code_Postal", (long) 79400)
					.addLong("CodePostalFilter", (long) 0)
					.toJobParameters();
			jobOperator.start(importFranceJob, params);
		};
	}


}
