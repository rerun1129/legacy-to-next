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
import com.freightos.fms.application.housebl.projection.HouseBlSummary;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface HouseBlPort {
    Optional<HouseBl> findHouseBlById(Long id);
    PagedResult<HouseBlSummary> searchHouseBls(HouseBlFilter filter, PageRequest pageRequest);
    PagedResult<HouseBl> findHouseBlsBySchedule(JobDiv jobDiv, Bound bound, String from, String to, PageRequest pageRequest);
    long countHouseBlsByMasterBlId(Long masterBlId);
    HouseBl saveHouseBl(HouseBl houseBl);
    Optional<JobDiv> findJobDivById(Long id);
    void deleteByIdAndJobDiv(Long id, JobDiv jobDiv);
    List<ConsoledHouseBlSeaSummary> findConsoledSeaSummariesByMasterBlId(Long masterBlId);
    List<ConsoledHouseBlAirSummary> findConsoledAirSummariesByMasterBlId(Long masterBlId);
    /**
     * 동일 트랜잭션 내에서 fetch → mutator 적용 → save를 1회 호출로 묶는다.
     * 1차 캐시 hit으로 재조회 SELECT 제거.
     */
    void update(Long id, Consumer<HouseBl> mutator);
    /**
     * hbl_no 단일 컬럼을 부분 UPDATE한다. SELECT 없이 직접 DB 갱신.
     * expectedJobDiv == null 이면 jobDiv 조건을 WHERE에 포함하지 않는다.
     * @return 영향받은 row 수 (0이면 id 미존재 또는 jobDiv 불일치)
     */
    long updateHblNoById(Long id, BlNumber newHblNo, JobDiv expectedJobDiv);
}
