package com.natsystem.BAN.controller;

import com.natsystem.BAN.dto.FranceDTO;
import com.natsystem.BAN.services.ApiService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/79")
@RequiredArgsConstructor
public class ApiController {
    private final ApiService apiService;



    @GetMapping("/page")
    public Page<FranceDTO> francePage(
            @ParameterObject
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return apiService.recherche(
                pageable
        );
    }

    @GetMapping("/page/cp")
    public Page<FranceDTO> cp(
            @RequestParam("cp") Integer cp,
            @ParameterObject
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return apiService.rechercheCp(
                pageable,cp
        );
    }

    @GetMapping("/page/voie")
    public Page<FranceDTO> rechercheVoie(
            @RequestParam("voie") String voie,
            @ParameterObject
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return apiService.rechercheVoie(
                pageable,voie
        );
    }

    @GetMapping("/page/commune")
    public Page<FranceDTO> rechercheCommune(
            @RequestParam("commune") String commune,
            @ParameterObject
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return apiService.rechercheCommune(
                pageable,commune
        );
    }
}
