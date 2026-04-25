package com.freightos.fms.domain.housebl.port.out;

import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface HouseBlPort {
    Optional<HouseBl> findById(UUID id);
    Page<HouseBl> findAllByJobDivAndBoundOrderByCreatedAtDesc(JobDiv jobDiv, Bound bound, Pageable pageable);
    Page<HouseBl> findBySchedule(JobDiv jobDiv, Bound bound, LocalDate from, LocalDate to, Pageable pageable);
    long countByMasterBlId(UUID masterBlId);
    HouseBl save(HouseBl houseBl);
    void delete(HouseBl houseBl);
}
