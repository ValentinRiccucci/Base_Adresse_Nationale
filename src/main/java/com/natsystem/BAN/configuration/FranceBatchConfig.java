package com.natsystem.BAN.configuration;

import com.natsystem.BAN.Listener.StepTimingListener;
import com.natsystem.BAN.dto.FranceDTO;
import com.natsystem.BAN.model.France;
import com.natsystem.BAN.processor.FranceProcessor;
import com.natsystem.BAN.repository.FranceRepository;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileParseException;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.transform.FieldSet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;

@Configuration
public class FranceBatchConfig {
    @Bean
    public FlatFileItemReader<FranceDTO> csvReader() {
        return new FlatFileItemReaderBuilder<FranceDTO>()
                .name("adressesCsvReader")
                .resource(new ClassPathResource("adresses-79.csv"))
                .delimited()
                .delimiter(";")
                .names(
                        "id",
                        "id_fantoir",
                        "numero",
                        "rep",
                        "nom_voie",
                        "code_postal",
                        "code_insee",
                        "nom_commune",
                        "code_insee_ancienne_commune",
                        "nom_ancienne_commune",
                        "x",
                        "y",
                        "lon",
                        "lat",
                        "type_position",
                        "alias",
                        "nom",
                        "libelle_acheminement",
                        "nom_afnor",
                        "source_position",
                        "source_nom_voie",
                        "certification_commune",
                        "cad_parcelles"
                )
                .fieldSetMapper(fieldSet -> new FranceDTO(
                        fieldSet.readString("id"),
                        fieldSet.readString("id_fantoir"),
                        readInteger(fieldSet, "numero"),
                        fieldSet.readString("rep"),
                        fieldSet.readString("nom_voie"),
                        readInteger(fieldSet, "code_postal"),
                        readInteger(fieldSet, "code_insee"),
                        fieldSet.readString("nom_commune"),
                        readInteger(fieldSet, "code_insee_ancienne_commune"),
                        fieldSet.readString("nom_ancienne_commune"),
                        readBigDecimal(fieldSet, "x"),
                        readBigDecimal(fieldSet, "y"),
                        readBigDecimal(fieldSet, "lon"),
                        readBigDecimal(fieldSet, "lat"),
                        fieldSet.readString("type_position"),
                        fieldSet.readString("alias"),
                        fieldSet.readString("nom"),
                        fieldSet.readString("libelle_acheminement"),
                        fieldSet.readString("nom_afnor"),
                        fieldSet.readString("source_position"),
                        fieldSet.readString("source_nom_voie"),
                        readBoolean(fieldSet, "certification_commune"),
                        fieldSet.readString("cad_parcelles")
                ))
                .linesToSkip(1)
                .build();
    }

    @Bean
    public RepositoryItemWriter<France> franceWriter(FranceRepository FranceRepository) {
        RepositoryItemWriter<France> writer = new RepositoryItemWriter<>(FranceRepository);
        writer.setRepository(FranceRepository);
        return writer;
    }

    @Bean
    public JdbcBatchItemWriter<France> jdbcWriter(DataSource ds) {
        return new JdbcBatchItemWriterBuilder<France>()
                .dataSource(ds)
                .sql("""
INSERT INTO france (
id,
    id_fantoir,
    numero,
    rep,
    nom_voie,
    code_postal,
    code_insee,
    nom_commune,
    code_insee_ancienne_commune,
    nom_ancienne_commune,
    x,
    y,
    lon,
    lat,
    type_position,
    alias,
    nom_ld,
    libelle_acheminement,
    nom_afnor,
    source_position,
    source_nom_voie,
    certification_commune,
    cad_parcelles

)
VALUES (
:id,
:id_fantoir,
:numero,
:rep,
:nom_voie,
:code_postal,
:code_insee,
:nom_commune,
:code_insee_ancienne_commune,
:nom_ancienne_commune,
:x,
:y,
:lon,
:lat,
:type_position,
:alias,
:nom,
:libelle_acheminement,
:nom_afnor,
:source_position,
:source_nom_voie,
:certification_commune,
:cad_parcelles
)
ON CONFLICT (id) DO UPDATE
SET  id =                           EXCLUDED.id,
id_fantoir =                    EXCLUDED.id_fantoir,
numero =                        EXCLUDED.numero,
rep =                           EXCLUDED.rep,
nom_voie =                      EXCLUDED.nom_voie,
code_postal =                   EXCLUDED.code_postal,
code_insee =                    EXCLUDED.code_insee,
nom_commune =                   EXCLUDED.nom_commune,
code_insee_ancienne_commune =   EXCLUDED.code_insee_ancienne_commune,
nom_ancienne_commune =          EXCLUDED.nom_ancienne_commune,
x =                             EXCLUDED.x,
y =                             EXCLUDED.y,
lon =                           EXCLUDED.lon,
lat =                           EXCLUDED.lat,
type_position =                 EXCLUDED.type_position,
alias =                         EXCLUDED.alias,
nom_ld =                        EXCLUDED.nom_ld,
libelle_acheminement =          EXCLUDED.libelle_acheminement,
nom_afnor =                     EXCLUDED.nom_afnor,
source_position =               EXCLUDED.source_position,
source_nom_voie =               EXCLUDED.source_nom_voie,
certification_commune =         EXCLUDED.certification_commune,
cad_parcelles =                  EXCLUDED.cad_parcelles
""")
                .beanMapped() // utilise les noms de propriétés JavaBean
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor("spring_batch");
    }

    @Bean
    public Step importFranceStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<FranceDTO> csvReader,
            FranceProcessor franceProcessor,
            RepositoryItemWriter<France> franceWriter,
            JdbcBatchItemWriter<France> franceJdbcWriter,
            TaskExecutor taskExecutor
    ) {
        return new StepBuilder("importFranceStep", jobRepository)
                .<FranceDTO, France>chunk(4000, transactionManager)
                .reader(csvReader)
                .processor(franceProcessor)
                .writer(franceJdbcWriter)
                .listener(new StepTimingListener())
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skipLimit(1000)
                //.taskExecutor(taskExecutor) erreur SQLLITE base locked
                .build();
    }

    @Bean
    public Job importFranceJob(JobRepository jobRepository,
                               Step importFranceStep,
                               JobExecutionListener jobMetricsListener) {
        return new JobBuilder("importFranceJob", jobRepository)
                .listener(jobMetricsListener)
                .start(importFranceStep)
                .build();
    }



    private Integer readInteger(FieldSet fieldSet, String name) {
        String value = fieldSet.readString(name);
        return value == null || value.isBlank() ? null : Integer.valueOf(value);
    }

    private BigDecimal readBigDecimal(FieldSet fieldSet, String name) {
        String value = fieldSet.readString(name);
        return value == null || value.isBlank() ? null : new BigDecimal(value);
    }

    private Boolean readBoolean(FieldSet fieldSet, String name) {
        String value = fieldSet.readString(name);
        return value == null || value.isBlank() ? null : "1".equals(value) || Boolean.parseBoolean(value);
    }



}
