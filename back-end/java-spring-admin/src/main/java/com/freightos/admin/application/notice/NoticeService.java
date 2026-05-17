package com.freightos.admin.application.notice;

import com.freightos.admin.application.notice.command.CreateNoticeCommand;
import com.freightos.admin.application.notice.command.SearchNoticeCommand;
import com.freightos.admin.application.notice.command.UpdateNoticeCommand;
import com.freightos.admin.application.notice.port.in.NoticeUseCase;
import com.freightos.admin.application.notice.port.out.NoticePort;
import com.freightos.admin.application.notice.projection.NoticeSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.notice.entity.Notice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService implements NoticeUseCase {

    private final NoticePort noticePort;
    private final NoticeFactory noticeFactory;

    @Override
    public PagedResult<NoticeSummary> searchNotices(SearchNoticeCommand command) {
        return noticePort.searchSummaries(command);
    }

    @Override
    public Notice getNoticeById(Long id) {
        return noticePort.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("NOTICE_NOT_FOUND", MessageCode.NOTICE_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createNotice(CreateNoticeCommand command) {
        return noticePort.save(noticeFactory.from(command));
    }

    @Override
    @Transactional
    public void updateNotice(Long id, UpdateNoticeCommand command) {
        Notice existing = getNoticeById(id);
        if (existing.isDeleted()) {
            throw ApplicationException.conflict("NOTICE_ALREADY_DELETED", MessageCode.NOTICE_ALREADY_DELETED.getMessage());
        }
        existing.applyUpdate(command.title(), command.content(), command.pinned(), command.active(), command.publishedAt(), command.expiresAt());
        noticePort.update(id, existing);
    }

    @Override
    @Transactional
    public void deleteNotice(Long id) {
        Notice existing = getNoticeById(id);
        if (existing.isDeleted()) {
            throw ApplicationException.conflict("NOTICE_ALREADY_DELETED", MessageCode.NOTICE_ALREADY_DELETED.getMessage());
        }
        noticePort.softDelete(id);
    }
}
