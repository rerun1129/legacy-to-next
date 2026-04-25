package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.port.out.HouseBlPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HouseBlPersistenceAdapter implements HouseBlPort {

    private final HouseBlRepository houseBlRepository;

    @Override
    public Optional<HouseBl> findById(UUID id) {
        return houseBlRepository.findById(id);
    }

    @Override
    public Page<HouseBl> findAllByJobDivAndBoundOrderByCreatedAtDesc(JobDiv jobDiv, Bound bound, Pageable pageable) {
        return houseBlRepository.findAllByJobDivAndBoundOrderByCreatedAtDesc(jobDiv, bound, pageable);
    }

    @Override
    public Page<HouseBl> findBySchedule(JobDiv jobDiv, Bound bound, LocalDate from, LocalDate to, Pageable pageable) {
        return houseBlRepository.findBySchedule(jobDiv, bound, from, to, pageable);
    }

    @Override
    public long countByMasterBlId(UUID masterBlId) {
        return houseBlRepository.countByMasterBlId(masterBlId);
    }

    @Override
    public HouseBl save(HouseBl houseBl) {
        return houseBlRepository.save(houseBl);
    }

    @Override
    public void delete(HouseBl houseBl) {
        houseBlRepository.delete(houseBl);
    }
}
