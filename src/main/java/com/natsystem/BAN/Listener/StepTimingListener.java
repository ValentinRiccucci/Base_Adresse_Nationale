package com.natsystem.BAN.Listener;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

// StepExecutionListener
@Component
public class StepTimingListener implements StepExecutionListener {

    private static final Logger log =
            LoggerFactory.getLogger(StepTimingListener.class);


    @Override
    public void beforeStep(StepExecution stepExecution) {
       log.info("Step [{}] : début de l'exécution", stepExecution.getStepName());
    }

    @Override
    public @Nullable ExitStatus afterStep(StepExecution stepExecution) {
        log.info("Step [{}] terminé avec le status {} : {} éléments lus, {} écrits, {} ignorés, {} filtré, {} nb de commit",
                stepExecution.getStepName(),
                stepExecution.getStatus(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getSkipCount(),
                stepExecution.getFilterCount(),
                stepExecution.getCommitCount()
        );
        return null;
    }
}
