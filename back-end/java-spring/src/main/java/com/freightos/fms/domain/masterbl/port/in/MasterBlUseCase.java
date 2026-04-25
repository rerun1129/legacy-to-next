package com.freightos.fms.domain.masterbl.port.in;

import com.freightos.fms.domain.housebl.enums.Bound;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MasterBlUseCase {
    Page<MasterBl> list(Bound bound, Pageable pageable);
    MasterBl getById(UUID id);
    void delete(UUID id);
}
