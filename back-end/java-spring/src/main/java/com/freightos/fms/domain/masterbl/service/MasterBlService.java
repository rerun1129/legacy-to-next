package com.freightos.fms.domain.masterbl.service;

import com.freightos.fms.domain.housebl.enums.Bound;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MasterBlService {

    /** 리스트 조회 */
    Page<MasterBl> list(Bound bound, Pageable pageable);

    /** 단건 조회 */
    MasterBl getById(UUID id);

    /** 삭제 */
    void delete(UUID id);
}
