package com.freightos.admin.application.subscriber;

import com.freightos.admin.application.subscriber.command.CreateSubscriberCommand;
import com.freightos.admin.application.subscriber.command.SaveSubscriberChangesCommand;
import com.freightos.admin.application.subscriber.command.UpdateSubscriberCommand;
import com.freightos.admin.application.subscriber.port.out.SubscriberPort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.subscriber.entity.Subscriber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SubscriberServiceTest {

    @Mock
    private SubscriberPort subscriberPort;

    @InjectMocks
    private SubscriberService subscriberService;

    private static Subscriber makeSubscriber(String code) {
        return Subscriber.create(code, "테스트구독사", null, null, null, null, null, null, true);
    }

    // ── createSubscriber: save 호출 후 id 반환 ────────────────────────────────

    @Test
    void createSubscriber_callsPortSaveReturnsId() {
        CreateSubscriberCommand cmd = new CreateSubscriberCommand("SUB001", "테스트", null, null, null, null, null, null, true);
        given(subscriberPort.save(any())).willReturn(10L);

        Long id = subscriberService.createSubscriber(cmd);

        assertThat(id).isEqualTo(10L);
        then(subscriberPort).should().save(any(Subscriber.class));
    }

    // ── createSubscriber: 코드 중복 → SUBSCRIBER_DUPLICATE_CODE 409 ─────────

    @Test
    void createSubscriber_duplicateCode_throwsConflict() {
        CreateSubscriberCommand cmd = new CreateSubscriberCommand("SUB001", "테스트", null, null, null, null, null, null, true);
        given(subscriberPort.save(any())).willThrow(new DataIntegrityViolationException("uq"));

        assertThatThrownBy(() -> subscriberService.createSubscriber(cmd))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("SUBSCRIBER_DUPLICATE_CODE");
                });
    }

    // ── updateSubscriber: subscriberCode 불변 확인 ────────────────────────────

    @Test
    void updateSubscriber_codeIsImmutable() {
        Subscriber existing = makeSubscriber("SUB001");
        existing.assignIdentity(1L, null, null, null, null);
        UpdateSubscriberCommand cmd = new UpdateSubscriberCommand("새이름", null, null, null, null, null, null, true);
        given(subscriberPort.findById(1L)).willReturn(Optional.of(existing));

        subscriberService.updateSubscriber(1L, cmd);

        assertThat(existing.getSubscriberCode()).isEqualTo("SUB001");
        then(subscriberPort).should().update(eq(1L), any(Subscriber.class));
    }

    // ── updateSubscriber: 삭제된 구독사 → SUBSCRIBER_ALREADY_DELETED 409 ─────

    @Test
    void updateSubscriber_deleted_throwsConflict() {
        Subscriber deleted = makeSubscriber("SUB001");
        deleted.assignDeletedAt(OffsetDateTime.now());
        given(subscriberPort.findById(1L)).willReturn(Optional.of(deleted));
        UpdateSubscriberCommand cmd = new UpdateSubscriberCommand("새이름", null, null, null, null, null, null, true);

        assertThatThrownBy(() -> subscriberService.updateSubscriber(1L, cmd))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    // ── deleteSubscriber: softDelete 호출 ────────────────────────────────────

    @Test
    void deleteSubscriber_callsSoftDelete() {
        Subscriber existing = makeSubscriber("SUB001");
        existing.assignIdentity(5L, null, null, null, null);
        given(subscriberPort.findById(5L)).willReturn(Optional.of(existing));

        subscriberService.deleteSubscriber(5L);

        then(subscriberPort).should().softDelete(5L);
    }

    // ── deleteSubscriber: 이미 삭제 → 409 ────────────────────────────────────

    @Test
    void deleteSubscriber_alreadyDeleted_throwsConflict() {
        Subscriber deleted = makeSubscriber("SUB001");
        deleted.assignDeletedAt(OffsetDateTime.now());
        given(subscriberPort.findById(5L)).willReturn(Optional.of(deleted));

        assertThatThrownBy(() -> subscriberService.deleteSubscriber(5L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    // ── saveSubscriberChanges: creates/updates/deletes 카운트 반환 ─────────────

    @Test
    void saveSubscriberChanges_returnsCorrectCounts() {
        CreateSubscriberCommand create = new CreateSubscriberCommand("NEW001", "새구독사", null, null, null, null, null, null, true);
        Subscriber forUpdate = makeSubscriber("SUB002");
        forUpdate.assignIdentity(2L, null, null, null, null);
        Subscriber forDelete = makeSubscriber("SUB003");
        forDelete.assignIdentity(3L, null, null, null, null);

        given(subscriberPort.save(any())).willReturn(99L);
        given(subscriberPort.findById(2L)).willReturn(Optional.of(forUpdate));
        given(subscriberPort.findById(3L)).willReturn(Optional.of(forDelete));

        UpdateSubscriberCommand updateCmd = new UpdateSubscriberCommand("수정명", null, null, null, null, null, null, true);
        SaveSubscriberChangesCommand command = new SaveSubscriberChangesCommand(
                List.of(create),
                List.of(new SaveSubscriberChangesCommand.UpdateEntry(2L, updateCmd)),
                List.of(3L)
        );

        SaveChangesResult result = subscriberService.saveSubscriberChanges(command);

        assertThat(result.createdCount()).isEqualTo(1);
        assertThat(result.updatedCount()).isEqualTo(1);
        assertThat(result.deletedCount()).isEqualTo(1);
    }

    // ── getSubscriberById: not found → 404 ───────────────────────────────────

    @Test
    void getSubscriberById_notFound_throws404() {
        given(subscriberPort.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> subscriberService.getSubscriberById(99L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
