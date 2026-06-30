package com.natsystem.BAN.services;

import com.natsystem.BAN.dto.FranceDTO;
import com.natsystem.BAN.repository.FranceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApiService {
    private final FranceRepository franceRepository;


    @Transactional(readOnly = true)
    public Page<FranceDTO> recherche( Pageable pageable) {
        return franceRepository.findAll(pageable)
                .map(FranceDTO::from);
    }
}
