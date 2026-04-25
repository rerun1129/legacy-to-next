package com.freightos.fms.domain.masterbl.port.out;

import com.freightos.fms.domain.housebl.enums.Bound;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface MasterBlPort {
    Optional<MasterBl> findById(UUID id);
    Page<MasterBl> findAllByBound(Bound bound, Pageable pageable);
    Optional<MasterBl> findByMblNo(String mblNo);
    boolean existsByMblNo(String mblNo);
    void delete(MasterBl masterBl);
}
