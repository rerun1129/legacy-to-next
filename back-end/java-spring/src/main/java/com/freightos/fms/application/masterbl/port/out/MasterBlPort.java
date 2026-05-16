package com.freightos.fms.application.masterbl.port.out;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.masterbl.projection.MasterBlSummaryResult;
import com.freightos.fms.domain.masterbl.MasterBlFilter;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;

import java.util.List;
import java.util.Optional;

public interface MasterBlPort {
    Optional<MasterBl> findMasterBlById(Long id);
    PagedResult<MasterBl> getMasterBlsByBound(Bound bound, PageRequest pageRequest);
    PagedResult<MasterBlSummaryResult> searchMasterBls(MasterBlFilter filter, PageRequest pageRequest);
    Optional<MasterBl> findMasterBlByMblNo(String mblNo);
    boolean existsByMblNo(String mblNo);
    /**
     * 신규 MasterBl을 영속화하고 master_bl_id를 반환한다.
     * domain에 포함된 desc는 null이 아닌 경우에만 함께 insert된다.
     */
    Long createMasterBl(MasterBl domain);

    /**
     * 기존 master_bl_id row를 update한다.
     * 호출 전 domain.getId()가 non-null임을 보장해야 한다.
     * 응답 데이터가 필요하면 호출자가 findMasterBlById로 재조회한다.
     */
    void updateMasterBl(MasterBl domain);
    Optional<MasterBlJobDiv> findJobDivById(Long id);
    void deleteByIdAndJobDiv(Long id, MasterBlJobDiv jobDiv);
    List<Long> findMasterBlKeysByMblNoExact(String mblNo);

    /**
     * master_bl의 mbl_no·master_ref_no를 부분 UPDATE (SELECT 없이 직접 DB 갱신).
     * @return affected row 수 (0이면 id 미존재)
     */
    long updateMblNoAndMasterRefById(Long id, String newMblNo, String newMasterRefNo);
}
