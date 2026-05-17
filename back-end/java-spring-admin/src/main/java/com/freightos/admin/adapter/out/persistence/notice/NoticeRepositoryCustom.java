package com.freightos.admin.adapter.out.persistence.notice;

import com.freightos.admin.application.notice.command.SearchNoticeCommand;
import com.freightos.admin.application.notice.projection.NoticeSummary;
import com.freightos.admin.common.response.PagedResult;

public interface NoticeRepositoryCustom {
    PagedResult<NoticeSummary> searchSummaries(SearchNoticeCommand command);
}
