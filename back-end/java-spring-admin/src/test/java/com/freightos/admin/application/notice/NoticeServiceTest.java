package com.freightos.admin.application.notice;

import com.freightos.admin.application.notice.command.CreateNoticeCommand;
import com.freightos.admin.application.notice.command.UpdateNoticeCommand;
import com.freightos.admin.application.notice.port.out.NoticePort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.domain.notice.entity.Notice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private NoticePort noticePort;

    @Mock
    private NoticeFactory noticeFactory;

    @InjectMocks
    private NoticeService noticeService;

    // ── createNotice: 정상 → id 반환 ─────────────────────────────────────────

    @Test
    void createNotice_normal_callsFactoryAndPortSaveReturnsId() {
        CreateNoticeCommand command = new CreateNoticeCommand("제목", "내용", false, true, null, null);
        Notice domain = Notice.create("제목", "내용", false, true, null, null);
        given(noticeFactory.from(command)).willReturn(domain);
        given(noticePort.save(domain)).willReturn(10L);

        Long id = noticeService.createNotice(command);

        assertThat(id).isEqualTo(10L);
        then(noticeFactory).should().from(command);
        then(noticePort).should().save(domain);
    }

    // ── createNotice: plain text content(개행 \n) → factory/port 호출 시 그대로 전달 ──

    @Test
    void createNotice_plainTextContent_storedAsIs() {
        String multilineContent = "첫 번째 줄\n두 번째 줄\n세 번째 줄";
        CreateNoticeCommand command = new CreateNoticeCommand("제목", multilineContent, false, true, null, null);
        Notice domain = Notice.create("제목", multilineContent, false, true, null, null);
        given(noticeFactory.from(command)).willReturn(domain);
        given(noticePort.save(domain)).willReturn(20L);

        Long id = noticeService.createNotice(command);

        assertThat(id).isEqualTo(20L);
        // factory에 전달된 command의 content가 개행 포함 원본과 동일한지 검증
        then(noticeFactory).should().from(command);
        assertThat(command.content()).isEqualTo(multilineContent);
    }

    // ── getNoticeById: not_found → 404 NOTICE_NOT_FOUND ──────────────────────

    @Test
    void getNoticeById_notFound_throwsNotFound() {
        given(noticePort.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.getNoticeById(99L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(appEx.getErrorCode()).isEqualTo("NOTICE_NOT_FOUND");
                });
    }

    // ── updateNotice: 이미 삭제된 공지 → 409 NOTICE_ALREADY_DELETED ──────────

    @Test
    void updateNotice_alreadyDeleted_throwsConflict() {
        Notice deleted = Notice.create("제목", "내용", false, true, null, null);
        deleted.assignDeletedAt(LocalDateTime.of(2024, 1, 1, 0, 0));
        UpdateNoticeCommand command = new UpdateNoticeCommand("수정 제목", "수정 내용", false, true, null, null);
        given(noticePort.findById(1L)).willReturn(Optional.of(deleted));

        assertThatThrownBy(() -> noticeService.updateNotice(1L, command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("NOTICE_ALREADY_DELETED");
                });
    }

    // ── deleteNotice: 정상 → port.softDelete 호출 ────────────────────────────

    @Test
    void deleteNotice_normal_callsSoftDelete() {
        Notice existing = Notice.create("제목", "내용", false, true, null, null);
        given(noticePort.findById(5L)).willReturn(Optional.of(existing));

        noticeService.deleteNotice(5L);

        then(noticePort).should().softDelete(5L);
    }

    // ── deleteNotice: 이미 삭제된 공지 → 409 NOTICE_ALREADY_DELETED ──────────

    @Test
    void deleteNotice_alreadyDeleted_throwsConflict() {
        Notice deleted = Notice.create("제목", "내용", false, true, null, null);
        deleted.assignDeletedAt(LocalDateTime.of(2024, 1, 1, 0, 0));
        given(noticePort.findById(5L)).willReturn(Optional.of(deleted));

        assertThatThrownBy(() -> noticeService.deleteNotice(5L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("NOTICE_ALREADY_DELETED");
                });
    }

    // ── updateNotice: 정상 → port.update 호출 ────────────────────────────────

    @Test
    void updateNotice_normal_callsPortUpdate() {
        Notice existing = Notice.create("기존 제목", "기존 내용", false, true, null, null);
        UpdateNoticeCommand command = new UpdateNoticeCommand("수정 제목", "수정 내용", true, true, null, null);
        given(noticePort.findById(1L)).willReturn(Optional.of(existing));

        noticeService.updateNotice(1L, command);

        then(noticePort).should().update(eq(1L), any(Notice.class));
    }
}
