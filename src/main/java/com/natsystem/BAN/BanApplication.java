package com.natsystem.BAN;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@SpringBootApplication
@Component
public class BanApplication {

	public static void main(String[] args) {
		SpringApplication.run(BanApplication.class, args);
		System.out.println("BAN Application Started");

	}
	// Pour lancer le job au démarrage (mode démo) :
//	@Bean
//	public CommandLineRunner run(JobLauncher launcher, Job helloWorldJob) {
//		return args -> {
//			JobParameters params = new JobParametersBuilder()
//					.addLong("startAt", System.currentTimeMillis())
//					.toJobParameters();
//			launcher.run(helloWorldJob, params);
//		};
//	}
	@Bean
	public CommandLineRunner run(JobLauncher launcher, Job importFranceJob) {
		return args -> {
			JobParameters params = new JobParametersBuilder()
					.addLong("startAt", System.currentTimeMillis())
					.addLong("code_Postal", (long) 79400)
					.addLong("CodePostalFilter", (long) 0)
					.toJobParameters();
			launcher.run(importFranceJob, params);
		};
	}

}
