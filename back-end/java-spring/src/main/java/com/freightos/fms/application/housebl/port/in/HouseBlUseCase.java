package com.freightos.fms.application.housebl.port.in;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.housebl.command.ChangeHouseBlNoCommand;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.SearchHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.housebl.projection.HouseBlDetailView;
import com.freightos.fms.application.housebl.projection.HouseBlSummary;
import com.freightos.fms.domain.housebl.enums.JobDiv;

import java.util.List;

public interface HouseBlUseCase {
    PagedResult<HouseBlSummary> searchHouseBls(SearchHouseBlCommand cmd, PageRequest pageRequest);
    HouseBlDetailView findHouseBlById(Long id);
    Long createHouseBl(CreateHouseBlCommand command);
    /** TRUCK/NON_BL House B/L update. SEA/AIR는 각각 updateSeaHbl/updateAirHbl 사용. */
    void updateHouseBl(Long id, UpdateHouseBlCommand command);
    /** Sea House B/L 전용 update — §6.35 Port+Adapter 패턴. void 반환으로 Controller ApiResponse<Void>화. */
    void updateSeaHbl(Long id, UpdateHouseBlCommand command);
    /** Air House B/L 전용 update — §6.35 Port+Adapter 패턴. void 반환으로 Controller ApiResponse<Void>화. */
    void updateAirHbl(Long id, UpdateHouseBlCommand command);
    void deleteHouseBlById(Long id);
    void changeHblNo(Long id, ChangeHouseBlNoCommand command);
    /** hbl_no EXACT 매칭으로 house_bl_id PK 목록 조회 (최대 2건). jobDiv로 도메인 격리. */
    List<Long> findHouseBlKeysByHblNoExact(String hblNo, JobDiv jobDiv);
}
