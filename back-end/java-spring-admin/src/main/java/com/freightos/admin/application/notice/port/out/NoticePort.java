package com.freightos.admin.application.notice.port.out;

import com.freightos.admin.application.notice.command.SearchNoticeCommand;
import com.freightos.admin.application.notice.projection.NoticeSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.notice.entity.Notice;

import java.util.Optional;

public interface NoticePort {
    PagedResult<NoticeSummary> searchSummaries(SearchNoticeCommand command);
    Optional<Notice> findById(Long id);
    Long save(Notice notice);
    void update(Long id, Notice patchData);
    void softDelete(Long id);
}
