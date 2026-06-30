package com.natsystem.BAN.dto;

import com.natsystem.BAN.model.France;
import lombok.Getter;

import java.math.BigDecimal;

public record FranceDTO(
        String id,
        String id_fantoir,
        Integer numero,
        String rep,
        String nom_voie,
        Integer code_postal,
        Integer code_insee,
        String nom_commune,
        Integer code_insee_ancienne_commune,
        String nom_ancienne_commune,
        BigDecimal x,
        BigDecimal y,
        BigDecimal lon,
        BigDecimal lat,
        String type_position,
        String alias,
        String nom,
        String libelle_acheminement,
        String nom_afnor,
        String source_position,
        String source_nom_voie,
        Boolean certification_commune,
        String cad_parcelles) {
    public static FranceDTO from(France france) {
        return new FranceDTO(
                france.getId(),
                france.getId_fantoir(),
                france.getNumero(),
                france.getRep(),
                france.getNom_voie(),
                france.getCode_postal(),
                france.getCode_insee(),
                france.getNom_commune(),
                france.getCode_insee_ancienne_commune(),
                france.getNom_ancienne_commune(),
                france.getX(),france.getY(),
                france.getLon(),france.getLat(),
                france.getType_position(),
                france.getAlias(),
                france.getNom(),
                france.getLibelle_acheminement(),
                france.getNom_afnor(),
                france.getSource_position(),
                france.getSource_nom_voie(),
                france.getCertification_commune(),
                france.getCad_parcelles()
        );
    };
}
