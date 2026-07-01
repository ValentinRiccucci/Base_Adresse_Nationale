package com.natsystem.BAN.repository;

import com.natsystem.BAN.dto.FranceDTO;
import com.natsystem.BAN.model.France;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Repository
public interface FranceRepository extends JpaRepository<France, String> {

    @Query(value="select * from france h where h.code_postal = :cp",nativeQuery=true)
    Page<France> findByCode_postal(Pageable pageable,@Param("cp")Integer cp);

    @Query(value="select * from france h where LOWER(h.nom_voie) = LOWER(:voie)",nativeQuery=true)
    Page<France> findByNom_voieIgnoreCase (Pageable pageable,@Param("voie")String voie);

    @Query(value="select * from france h where LOWER(h.nom_commune) = LOWER(:commune)",nativeQuery=true)
    Page<France> findByNom_communeIgnoreCase(Pageable pageable,@Param("commune")String commune);

}
