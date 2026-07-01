package com.natsystem.BAN.Listener;

import com.natsystem.BAN.model.France;
import com.natsystem.BAN.processor.FranceProcessor;
import com.natsystem.BAN.repository.FranceRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.natsystem.BAN.processor.FranceProcessor.listePasSuppression;

@Configuration
public class JobProgressListener {
    private static final Logger log = LoggerFactory.getLogger(JobProgressListener.class);
    private final FranceRepository franceRepository;
    private final MeterRegistry meterRegistry;


    public JobProgressListener(FranceRepository franceRepository, MeterRegistry meterRegistry) {
        this.franceRepository = franceRepository;
        this.meterRegistry = meterRegistry;
    }

    @Bean
    public JobExecutionListener jobMetricsListener(MeterRegistry registry, FranceProcessor franceProcessor) {
        return new JobExecutionListener() {

            public void beforeJob(JobExecution jobExecution) {
                log.info("Démarrage du job [{}] avec les paramètres : {}",
                        jobExecution.getJobInstance().getJobName(),
                        jobExecution.getJobParameters());
                //franceRepository.deleteAll();
            }

            @Override
            public void afterJob(JobExecution jobExecution) {

                Set<String> allDatabaseId = new HashSet<>(franceRepository.findAll().stream().map(France::getId).collect(Collectors.toSet()));
                log.info("AllDatabaseId size {}", allDatabaseId.size());
                log.info("listePasSuppression size {}", listePasSuppression.size());

                List<String> aSupprimer = allDatabaseId.stream()
                        .filter(item -> !listePasSuppression.contains(item))
                        .toList();

                log.info("Nombre de lignes à supprimer : {}", aSupprimer.size());

                if (!aSupprimer.isEmpty()) {
                    franceRepository.deleteAllById(aSupprimer);
                }


                log.info("Job [{}] terminé avec le statut : {} en {} ms",
                        jobExecution.getJobInstance().getJobName(),
                        jobExecution.getStatus(),
                        Duration.between(
                                jobExecution.getStartTime(),
                                jobExecution.getEndTime()).toMillis()
                );
                log.info("Nombre de lignes traitées : {} | Nombre de lignes dupliqués : {}",
                        meterRegistry.get("ban.france.lignes").counter().count(),
                        meterRegistry.get("ban.france.duplicates").counter().count()
                );

                log.info("Nombre de ligne ignoré à cause du code postal : {}", meterRegistry.get("ban.france.invalid_postal").counter().count());
            }
        };
    }


}
