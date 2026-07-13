package com.natsystem.BAN.Listener;

import com.natsystem.BAN.repository.FranceRepository;
import com.natsystem.BAN.services.ApiService;
import io.micrometer.core.instrument.MeterRegistry;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.ArrayList;

import static com.natsystem.BAN.processor.FranceProcessor.listePasSuppression;

@Configuration
public class JobProgressListener {
    private static final Logger log = LoggerFactory.getLogger(JobProgressListener.class);
    private final MeterRegistry meterRegistry;
    private final ApiService apiService;
    public static ArrayList<String> listeDB = new ArrayList<>();


    public JobProgressListener(MeterRegistry meterRegistry, ApiService apiService, FranceRepository franceRepository) {
        this.meterRegistry = meterRegistry;
        this.apiService = apiService;
    }

    @Bean
    public JobExecutionListener jobMetricsListener() {
        return new JobExecutionListener() {

            public void beforeJob(@NonNull JobExecution jobExecution) {
                log.info("Démarrage du job [{}] avec les paramètres : {}",
                        jobExecution.getJobInstance().getJobName(),
                        jobExecution.getJobParameters());
                listePasSuppression = apiService.findAllId();
                //franceRepository.deleteAll();
            }

            @Override
            public void afterJob(@NonNull JobExecution jobExecution) {

                ArrayList<String> liste = new ArrayList<>();
                listeDB.stream()
                        .filter(listePasSuppression::contains)
                        .forEach(liste::add);
                
                int nbLigneSuppr = apiService.deleteAllById(liste);
                log.info("Nombre de lignes supprimées : {}", nbLigneSuppr);

                assert jobExecution.getStartTime() != null;
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
