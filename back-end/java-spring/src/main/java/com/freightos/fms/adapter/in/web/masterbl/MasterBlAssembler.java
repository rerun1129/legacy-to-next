package com.freightos.fms.adapter.in.web.masterbl;

import com.freightos.fms.adapter.in.web.masterbl.dto.CreateMasterBlRequest;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlDetailResponse;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlSummaryResponse;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.masterbl.MasterBlDetail;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import org.springframework.stereotype.Component;

@Component
public class MasterBlAssembler {

    public PagedResult<MasterBlSummaryResponse> toSummaryPage(PagedResult<MasterBl> source) {
        return source.map(MasterBlSummaryResponse::from);
    }

    public MasterBlDetailResponse toDetail(MasterBlDetail source) {
        return MasterBlDetailResponse.from(source);
    }

    /**
     * 요청 DTO로부터 jobDiv에 맞는 MasterBl 서브타입 엔티티를 생성한다.
     *
     * @throws UnsupportedOperationException jobDiv가 SEA/AIR 외인 경우
     */
    public MasterBl toEntity(CreateMasterBlRequest request) {
        return switch (request.jobDiv()) {
            case SEA -> MasterBlSea.create(request.bound());
            case AIR -> MasterBlAir.create(request.bound());
        };
    }
}
