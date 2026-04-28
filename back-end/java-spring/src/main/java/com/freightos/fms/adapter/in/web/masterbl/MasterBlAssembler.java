package com.freightos.fms.adapter.in.web.masterbl;

import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlDetailResponse;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlSummaryResponse;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import org.springframework.stereotype.Component;

@Component
public class MasterBlAssembler {

    public PagedResult<MasterBlSummaryResponse> toSummaryPage(PagedResult<MasterBl> source) {
        return source.map(MasterBlSummaryResponse::from);
    }

    public MasterBlDetailResponse toDetail(MasterBl source) {
        return MasterBlDetailResponse.from(source);
    }
}
