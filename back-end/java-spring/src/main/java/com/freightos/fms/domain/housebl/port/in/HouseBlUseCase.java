package com.freightos.fms.domain.housebl.port.in;

import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlSummaryResponse;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface HouseBlUseCase {
    Page<HouseBlSummaryResponse> list(JobDiv jobDiv, Bound bound, Pageable pageable);
    HouseBl getById(UUID id);
    HouseBl save(HouseBl houseBl);
    void delete(UUID id);
}
