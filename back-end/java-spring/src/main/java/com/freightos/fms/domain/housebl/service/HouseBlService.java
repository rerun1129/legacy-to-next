package com.freightos.fms.domain.housebl.service;

import com.freightos.fms.domain.housebl.api.dto.HouseBlSummaryResponse;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface HouseBlService {

    /** 리스트 조회 */
    Page<HouseBlSummaryResponse> list(JobDiv jobDiv, Bound bound, Pageable pageable);

    /** 단건 조회 */
    HouseBl getById(UUID id);

    /** 저장 (신규 / 수정 공통) */
    HouseBl save(HouseBl houseBl);

    /** 삭제 */
    void delete(UUID id);
}
