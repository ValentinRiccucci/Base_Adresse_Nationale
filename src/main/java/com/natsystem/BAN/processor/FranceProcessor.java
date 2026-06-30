package com.natsystem.BAN.processor;

import com.natsystem.BAN.dto.FranceDTO;
import com.natsystem.BAN.model.France;
import com.natsystem.BAN.repository.FranceRepository;
import io.micrometer.core.instrument.MeterRegistry;

import org.hibernate.cache.spi.support.AbstractReadWriteAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Pattern;

@StepScope
@Component("franceProcessor")
public class FranceProcessor implements ItemProcessor<FranceDTO, France> {
    private static final Logger log = LoggerFactory.getLogger(FranceProcessor.class);
    private static final Pattern pattern = Pattern.compile("^([0-9]){5}_[A-Za-z0-9]{1,9}_([0-9]){5}(?:_[A-Za-z0-9\\s_-]+)?$");
    private final MeterRegistry meterRegistry;
    private final FranceRepository franceRepository;
    private static ArrayList<France> database = new ArrayList<>();
    private static ArrayList<String> liste_id = new ArrayList<>();
    public static ArrayList<String> listePasSuppression = new ArrayList<>();
    public static ArrayList<String> listeSuppression = new ArrayList<>();
    private France france;



    @Value("#{jobParameters['code_Postal']}")
    private Long code_Postal;
    @Value("#{jobParameters['CodePostalFilter']}")
    private Long CodePostalFilter;

    public FranceProcessor(MeterRegistry meterRegistry, FranceRepository franceRepository) {
        this.meterRegistry = meterRegistry;
        this.franceRepository = franceRepository;
        database = (ArrayList<France>) franceRepository.findAll();
    }

    @Override
    public France process(FranceDTO item) throws Exception {
        Boolean pass = false;

        meterRegistry.counter("ban.france.lignes").increment();
        meterRegistry.counter("ban.france.invalid_postal").count();
        France databaseItem = franceRepository.findById(item.id()).orElse(null);
        //On regarde si on pas déja traité avec l'ID
        if (liste_id.contains(item.id()) ) {
            meterRegistry.counter("ban.france.duplicates").increment();
            log.warn("ID déjà existant en base id={}",
                    item.id()
            );
            pass = true;
        }
        //On regarde si la ligne était déja dans la table mais qu'elle est différente et pas déja traité
        else if (databaseItem != null && !isDuplicate(item, Optional.of(databaseItem)) ) {
            log.info("Ancienne ligne : {}", databaseItem.toString());
            log.info("Nouvelle ligne : {}", item.toString());
            liste_id.add(item.id());
            //Élimination des ID qui n'ont pas le bon format
        } else if ( !pattern.matcher(item.id()).matches()) {
            log.warn("ID Null ou pas bon format dans le CSV: id={} / pattern = {}",
                    item.id(),
                    !pattern.matcher(item.id()).matches()
            );
            pass = true;
        }

        //Application d'un filtre sur le code postal
        if (CodePostalFilter == 1 && !code_Postal.equals(Long.valueOf(item.code_postal()))) {
            meterRegistry.counter("ban.france.invalid_postal").increment();
            pass = true;
        }

        if (!pass) {
            listePasSuppression.add(item.id());
            France france = new France(item.id(),
                    item.id_fantoir(),
                    item.numero(),
                    item.rep(),
                    item.nom_voie(),
                    item.code_postal(),
                    item.code_insee(),
                    item.nom_commune(),
                    item.code_insee_ancienne_commune(),
                    item.nom_ancienne_commune(),
                    item.x(),
                    item.y(),
                    item.lon(),
                    item.lat(),
                    item.type_position(),
                    item.alias(),
                    item.nom(),
                    item.libelle_acheminement(),
                    item.nom_afnor(),
                    item.source_position(),
                    item.source_nom_voie(),
                    item.certification_commune(),
                    item.cad_parcelles());
            return france;
        }
        else return null;
    }

    private Boolean isDuplicate(FranceDTO france, Optional<France> france2) {

        Optional<France> france3 = Optional.of(new France(france.id(), france.id_fantoir(), france.numero(), france.rep(), france.nom_voie(), france.code_postal(), france.code_insee(), france.nom_commune(), france.code_insee_ancienne_commune(), france.nom_ancienne_commune(), france.x(), france.y(), france.lon(), france.lat(), france.type_position(), france.alias(), france.nom(), france.libelle_acheminement(), france.nom_afnor(), france.source_position(), france.source_nom_voie(), france.certification_commune(), france.cad_parcelles()));
//        log.info("france2: {}", france2.toString());
//        log.info("france3: {}", france3.toString());
        return france3.equals(france2);

//
//        if (( france.id() != null && france.id().equals(String.valueOf(france2.get().getId())) ) &&
//                ( france.id_fantoir() != null && france.id_fantoir().equals(String.valueOf(france2.get().getId_fantoir())) ) &&
//                ( france.numero() != null && france.numero().equals(france2.get().getNumero()) )  &&
//                ( france.rep() != null && france.rep().equals(france2.get().getRep()) )  &&
//                ( france.nom_voie() != null && france.nom_voie().equals(france2.get().getNom_voie()) )  &&
//                ( france.code_postal() != null && france.code_postal().equals(france2.get().getCode_postal()) )  &&
//                ( france.code_insee() != null && france.code_insee().equals(france2.get().getCode_insee()) )  &&
//                ( france.nom_commune() != null && france.nom_commune().equals(france2.get().getNom_commune()) ) &&
//                ( france.code_insee_ancienne_commune() != null && france.code_insee_ancienne_commune().equals(france2.get().getCode_insee_ancienne_commune()) )  &&
//                ( france.nom_ancienne_commune() != null && france.nom_ancienne_commune().equals(france2.get().getNom_ancienne_commune()) )  &&
//                ( france.x() != null && france.x().equals(france2.get().getX()) )  &&
//                ( france.y() != null && france.y().equals(france2.get().getY()) )  &&
//                ( france.lon() != null && france.lon().equals(france2.get().getLon()) )  &&
//                ( france.lat() != null && france.lat().equals(france2.get().getLat()) )  &&
//                ( france.type_position() != null &&  france.type_position().equals(france2.get().getType_position()) )  &&
//                ( france.alias() != null && france.alias().equals(france2.get().getAlias()) )  &&
//                ( france.nom() != null && france.nom().equals(france2.get().getNom()) )  &&
//                ( france.libelle_acheminement() != null && france.libelle_acheminement().equals(france2.get().getLibelle_acheminement()) )  &&
//                ( france.nom_afnor() != null && france.nom_afnor().equals(france2.get().getNom_afnor()) )  &&
//                ( france.source_position() != null && france.source_position().equals(france2.get().getSource_position()) )  &&
//                ( france.source_nom_voie() != null && france.source_nom_voie().equals(france2.get().getSource_nom_voie()) )  &&
//                ( france.certification_commune() != null && france.certification_commune().equals(france2.get().getCertification_commune()) )  &&
//                ( france.cad_parcelles() != null && france.cad_parcelles().equals(france2.get().getCad_parcelles()) )
//        ) {
//            return true;
//        }
//        else {
//            return false;
//        }
    }

}
