package com.freightos.admin.adapter.out.persistence.notice;

import com.freightos.admin.application.notice.command.SearchNoticeCommand;
import com.freightos.admin.application.notice.port.out.NoticePort;
import com.freightos.admin.application.notice.projection.NoticeSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.notice.entity.Notice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NoticePersistenceAdapter implements NoticePort {

    private final NoticeRepository noticeRepository;
    private final NoticeDomainToJpaMapper domainToJpaMapper;
    private final NoticeJpaToDomainMapper jpaToDomainMapper;

    @Override
    public PagedResult<NoticeSummary> searchSummaries(SearchNoticeCommand command) {
        return noticeRepository.searchSummaries(command);
    }

    @Override
    public Optional<Notice> findById(Long id) {
        return noticeRepository.findById(id).map(jpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(Notice notice) {
        NoticeJpaEntity entity = domainToJpaMapper.toNewJpa(notice);
        noticeRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, Notice patchData) {
        NoticeJpaEntity entity = noticeRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("NOTICE_NOT_FOUND", MessageCode.NOTICE_NOT_FOUND.getMessage()));
        domainToJpaMapper.applyUpdateFields(entity, patchData);
        // 영속 컨텍스트 dirty checking으로 flush 시 자동 UPDATE
    }

    @Override
    public void softDelete(Long id) {
        NoticeJpaEntity entity = noticeRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("NOTICE_NOT_FOUND", MessageCode.NOTICE_NOT_FOUND.getMessage()));
        entity.setDeletedAt(LocalDateTime.now());
        entity.setActive(false);
    }
}
