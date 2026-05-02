package com.freightos.fms.adapter.in.web.housebl;

import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlDetailResponse;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlSummaryResponse;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.entity.HouseBlAir;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
import com.freightos.fms.domain.housebl.projection.HouseBlSummary;
import org.springframework.stereotype.Component;

/**
 * 도메인 엔티티를 House B/L 응답 DTO로 변환한다.
 * 컨트롤러는 매핑을 직접 호출하지 않고 본 어셈블러에 위임한다.
 */
@Component
public class HouseBlAssembler {

    public PagedResult<HouseBlSummaryResponse> toSummaryPage(PagedResult<HouseBlSummary> source) {
        return source.map(HouseBlSummaryResponse::from);
    }

    public HouseBlDetailResponse toDetail(HouseBl source) {
        return HouseBlDetailResponse.from(source);
    }

    /**
     * 요청 DTO로부터 jobDiv에 맞는 HouseBl 서브타입 엔티티를 생성한다.
     * TRUCK jobDiv는 지원하지 않는다(별도 전용 엔드포인트 경유).
     *
     * @throws UnsupportedOperationException jobDiv가 SEA/AIR/NON_BL 외인 경우
     */
    public HouseBl toEntity(CreateHouseBlRequest request) {
        return switch (request.jobDiv()) {
            case SEA   -> HouseBlSea.create(request.bound());
            case AIR   -> HouseBlAir.create(request.bound());
            case TRUCK -> throw new UnsupportedOperationException(
                    "TRUCK jobDiv는 House B/L 기본 POST 엔드포인트를 지원하지 않습니다.");
            case NON_BL -> throw new UnsupportedOperationException(
                    "NON_BL jobDiv는 아직 지원되지 않습니다.");
        };
    }
}
