package com.freightos.fms.application.housebl.port.out;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.housebl.HouseBlFilter;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlAirSummary;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSeaSummary;
import com.freightos.fms.domain.housebl.projection.ConsoledSeaContainer;
import com.freightos.fms.application.housebl.projection.HouseBlSummary;

import java.util.List;
import java.util.Optional;

public interface HouseBlPort {
    Optional<HouseBl> findHouseBlById(Long id);
    PagedResult<HouseBlSummary> searchHouseBls(HouseBlFilter filter, PageRequest pageRequest);
    PagedResult<HouseBl> findHouseBlsBySchedule(JobDiv jobDiv, Bound bound, String from, String to, PageRequest pageRequest);
    long countHouseBlsByMasterBlId(Long masterBlId);
    HouseBl saveHouseBl(HouseBl houseBl);
    Optional<JobDiv> findJobDivById(Long id);
    void deleteByIdAndJobDiv(Long id, JobDiv jobDiv);
    List<ConsoledHouseBlSeaSummary> findConsoledSeaSummariesByMasterBlId(Long masterBlId);
    List<ConsoledSeaContainer> findConsoledSeaContainersByMasterBlId(Long masterBlId);
    List<ConsoledHouseBlAirSummary> findConsoledAirSummariesByMasterBlId(Long masterBlId);
    /**
     * hbl_no 단일 컬럼을 부분 UPDATE한다. SELECT 없이 직접 DB 갱신.
     * expectedJobDiv == null 이면 jobDiv 조건을 WHERE에 포함하지 않는다.
     * @return 영향받은 row 수 (0이면 id 미존재 또는 jobDiv 불일치)
     */
    long updateHblNoById(Long id, BlNumber newHblNo, JobDiv expectedJobDiv);

    /**
     * masterBlId 참조 House B/L의 master_bl_id·mbl_no·master_ref_no 일괄 NULL화.
     * House B/L 행은 유지. SEA/AIR 무관.
     * @return affected rows.
     */
    int nullifyMasterRefByMasterBlId(Long masterBlId);

    /**
     * masterBlId 참조 House B/L의 mbl_no·master_ref_no를 일괄 UPDATE.
     * master_bl_id·jobDiv 등은 건드리지 않는다.
     * @return affected rows (0이면 해당 masterBlId를 가진 house_bl 없음).
     */
    int updateMasterRefByMasterBlId(Long masterBlId, String newMblNo, String newMasterRefNo);

    /** hbl_no EXACT 매칭으로 house_bl_id PK 목록 조회 (최대 2건). jobDiv로 도메인 격리. */
    List<Long> findHouseBlKeysByHblNoExact(String hblNo, JobDiv jobDiv);
}
