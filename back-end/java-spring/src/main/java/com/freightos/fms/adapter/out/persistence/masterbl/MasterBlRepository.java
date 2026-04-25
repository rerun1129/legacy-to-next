package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.domain.housebl.enums.Bound;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MasterBlRepository extends JpaRepository<MasterBl, UUID> {

    Optional<MasterBl> findByMblNo(String mblNo);

    boolean existsByMblNo(String mblNo);

    Page<MasterBl> findAllByBound(Bound bound, Pageable pageable);
}
