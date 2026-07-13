package com.natsystem.BAN.repository;

import com.natsystem.BAN.model.France;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface FranceRepository extends JpaRepository<France, String> {

    @Query(value="select * from france h where h.code_postal = :cp",nativeQuery=true)
    Page<France> findByCode_postal(Pageable pageable,@Param("cp")Integer cp);

    @Query(value="select * from france h where LOWER(h.nom_voie) = LOWER(:voie)",nativeQuery=true)
    Page<France> findByNom_voieIgnoreCase (Pageable pageable,@Param("voie")String voie);

    @Query(value="select * from france h where LOWER(h.nom_commune) = LOWER(:commune)",nativeQuery=true)
    Page<France> findByNom_communeIgnoreCase(Pageable pageable,@Param("commune")String commune);

    @Modifying
    @Query(value="delete from france where id not in (:ids)",nativeQuery=true)
    int deleteAllByIdNotIn(ArrayList<String> ids);

    @Modifying
    @Query(value="delete from france where id in (:ids)",nativeQuery=true)
    int deleteAllById(ArrayList<String> ids);

    @Query(value="select id from france",nativeQuery=true)
    ArrayList<String> findAllId();
}
