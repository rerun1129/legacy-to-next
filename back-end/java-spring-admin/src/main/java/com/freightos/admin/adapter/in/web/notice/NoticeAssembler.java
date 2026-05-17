package com.freightos.admin.adapter.in.web.notice;

import com.freightos.admin.adapter.in.web.notice.dto.CreateNoticeRequest;
import com.freightos.admin.adapter.in.web.notice.dto.NoticeDetailResponse;
import com.freightos.admin.adapter.in.web.notice.dto.NoticeSummaryResponse;
import com.freightos.admin.adapter.in.web.notice.dto.SearchNoticeRequest;
import com.freightos.admin.adapter.in.web.notice.dto.UpdateNoticeRequest;
import com.freightos.admin.application.notice.command.CreateNoticeCommand;
import com.freightos.admin.application.notice.command.SearchNoticeCommand;
import com.freightos.admin.application.notice.command.UpdateNoticeCommand;
import com.freightos.admin.application.notice.projection.NoticeSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.notice.entity.Notice;
import org.springframework.stereotype.Component;

@Component
public class NoticeAssembler {

    public SearchNoticeCommand toSearchCommand(SearchNoticeRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchNoticeCommand(req.title(), req.pinned(), req.scope(), req.publishedOnly(), req.page(), size);
    }

    public CreateNoticeCommand toCreateCommand(CreateNoticeRequest req) {
        return new CreateNoticeCommand(req.title(), req.content(), req.pinned(), req.active(), req.publishedAt(), req.expiresAt());
    }

    public UpdateNoticeCommand toUpdateCommand(UpdateNoticeRequest req) {
        return new UpdateNoticeCommand(req.title(), req.content(), req.pinned(), req.active(), req.publishedAt(), req.expiresAt());
    }

    public NoticeSummaryResponse toSummaryResponse(NoticeSummary s) {
        return new NoticeSummaryResponse(s.id(), s.title(), s.pinned(), s.active(), s.publishedAt(), s.expiresAt(), s.deletedAt(), s.updatedAt());
    }

    public NoticeDetailResponse toDetail(Notice domain) {
        return new NoticeDetailResponse(
                domain.getId(), domain.getTitle(), domain.getContent(),
                domain.isPinned(), domain.isActive(),
                domain.getPublishedAt(), domain.getExpiresAt(), domain.getDeletedAt(),
                domain.getCreatedAt(), domain.getUpdatedAt(),
                domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<NoticeSummaryResponse> toSummaryPage(PagedResult<NoticeSummary> src) {
        return src.map(this::toSummaryResponse);
    }
}
