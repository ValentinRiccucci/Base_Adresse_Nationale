package com.natsystem.BAN.controller;

import com.natsystem.BAN.dto.FranceDTO;
import com.natsystem.BAN.services.ApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/79")
@RequiredArgsConstructor
public class ApiController {

    private final ApiService apiService;

    @GetMapping("/page")
    public Page<FranceDTO> francePage(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return apiService.recherche(
                pageable
        );
    }
}
