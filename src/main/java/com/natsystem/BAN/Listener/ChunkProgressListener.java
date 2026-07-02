package com.natsystem.BAN.Listener;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.annotation.AfterChunk;
import org.springframework.batch.core.annotation.BeforeChunk;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.stereotype.Component;

import java.time.Duration;


@Component
public class ChunkProgressListener implements ChunkListener {
    private static final Logger log =
            LoggerFactory.getLogger(ChunkProgressListener.class);
    private final MeterRegistry meterRegistry;
    private Timer.Sample timer;

    public ChunkProgressListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @BeforeChunk
    public void beforeChunk(Chunk chunk) {
        meterRegistry.counter("ban.france.chunk").increment();
        timer = Timer.start(meterRegistry);
        log.info("Chunk {} BEFORE",meterRegistry.counter("ban.france.chunk").count());
    }

    @AfterChunk
    public void afterChunk(Chunk chunk) {
       double timerChunk = timer.stop(Timer.builder("ban.france.chunkTimer").description("Chunk Timer").register(meterRegistry));
        log.info("Chunk {} COMPLETED with {} items in {} secondes ",meterRegistry.counter("ban.france.chunk").count(),chunk.size(), timerChunk/1000000000);
    }
}
