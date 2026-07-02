package com.natsystem.BAN.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "France")
@Getter
@Setter
public class France {
    @Id
    String id;
    @Column(name = "id_fantoir")
    String id_fantoir;
    @Column(name = "numero")
    Integer numero;
    @Column(name = "rep")
    String rep;
    @Column(name = "nom_voie")
    String nom_voie;
    @Column(name = "code_postal")
    Integer code_postal;
    @Column(name = "code_insee")
    Integer code_insee;
    @Column(name = "nom_commune")
    String nom_commune;
    @Column(name = "code_insee_ancienne_commune")
    Integer code_insee_ancienne_commune;
    @Column(name = "nom_ancienne_commune")
    String nom_ancienne_commune;
    @Column(name = "x")
    BigDecimal x;
    @Column(name = "y")
    BigDecimal y;
    @Column(name = "lon")
    BigDecimal lon;
    @Column(name = "lat")
    BigDecimal lat;
    @Column(name = "type_position")
    String type_position;
    @Column(name = "alias")
    String alias;
    @Column(name = "nom_ld")
    String nom;
    @Column(name = "libelle_acheminement")
    String libelle_acheminement;
    @Column(name = "nom_afnor")
    String nom_afnor;
    @Column(name = "source_position")
    String source_position;
    @Column(name = "source_nom_voie")
    String source_nom_voie;
    @Column(name = "certification_commune")
    Boolean certification_commune;
    @Column(name = "cad_parcelles")
    String cad_parcelles;

    @Override
    public String toString() {
        return "France{" +
                "id='" + id + '\'' +
                ", code_postal=" + code_postal +
                ", nom_commune='" + nom_commune + '\'' +
                ", numero=" + numero + '\'' +
                ", nom_voie='" + nom_voie +
                '}';
    }
}
