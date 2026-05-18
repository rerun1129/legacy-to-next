package com.freightos.admin.application.notice.port.in;

import com.freightos.admin.application.notice.command.CreateNoticeCommand;
import com.freightos.admin.application.notice.command.SearchNoticeCommand;
import com.freightos.admin.application.notice.command.UpdateNoticeCommand;
import com.freightos.admin.application.notice.projection.NoticeSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.notice.entity.Notice;

import java.util.List;

public interface NoticeUseCase {
    PagedResult<NoticeSummary> searchNotices(SearchNoticeCommand command);
    Notice getNoticeById(Long id);
    Long createNotice(CreateNoticeCommand command);
    void updateNotice(Long id, UpdateNoticeCommand command);
    void deleteNotice(Long id);
    void deleteNotices(List<Long> ids);
}
