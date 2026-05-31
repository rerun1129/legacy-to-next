package com.freightos.admin.application.subscription;

import com.freightos.admin.application.subscriber.port.in.SubscriberUseCase;
import com.freightos.admin.application.subscription.command.SaveSubscriptionChangesCommand;
import com.freightos.admin.application.subscription.port.out.SubscriptionPort;
import com.freightos.admin.application.subscription.projection.SubscriptionSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.subscriber.entity.Subscriber;
import com.freightos.admin.domain.subscription.entity.Subscription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionPort subscriptionPort;

    @Mock
    private SubscriberUseCase subscriberUseCase;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private static final LocalDate START = LocalDate.of(2025, 1, 1);
    private static final LocalDate END = LocalDate.of(2025, 12, 31);

    private static Subscriber makeSubscriber(Long id) {
        Subscriber s = Subscriber.create("SUB001", "테스트", null, null, null, null, null, null, true);
        s.assignIdentity(id, null, null, null, null);
        return s;
    }

    // ── getSubscriptionsBySubscriberId: 구독사 검증 후 목록 반환 ──────────────

    @Test
    void getSubscriptionsBySubscriberId_returnsList() {
        given(subscriberUseCase.getSubscriberById(1L)).willReturn(makeSubscriber(1L));
        SubscriptionSummary summary = new SubscriptionSummary(10L, 1L, "FMS", START, END, true,
                LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 1, 1, 0, 0));
        given(subscriptionPort.findBySubscriberId(1L)).willReturn(List.of(summary));

        List<SubscriptionSummary> result = subscriptionService.getSubscriptionsBySubscriberId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).moduleCode()).isEqualTo("FMS");
    }

    // ── saveSubscriptionChanges: create 성공 ──────────────────────────────────

    @Test
    void saveSubscriptionChanges_createSuccess() {
        given(subscriberUseCase.getSubscriberById(1L)).willReturn(makeSubscriber(1L));
        given(subscriptionPort.existsBySubscriberIdAndModuleCode(1L, "FMS")).willReturn(false);
        given(subscriptionPort.save(any())).willReturn(100L);

        SaveSubscriptionChangesCommand cmd = new SaveSubscriptionChangesCommand(
                1L,
                List.of(new SaveSubscriptionChangesCommand.CreateEntry("FMS", START, END, true)),
                List.of(),
                List.of()
        );

        SaveChangesResult result = subscriptionService.saveSubscriptionChanges(cmd);

        assertThat(result.createdCount()).isEqualTo(1);
        then(subscriptionPort).should().save(any(Subscription.class));
    }

    // ── saveSubscriptionChanges: 동일 moduleCode 중복 → SUBSCRIPTION_DUPLICATE_MODULE 409 ──

    @Test
    void saveSubscriptionChanges_duplicateModule_throwsConflict() {
        given(subscriberUseCase.getSubscriberById(1L)).willReturn(makeSubscriber(1L));
        given(subscriptionPort.existsBySubscriberIdAndModuleCode(1L, "FMS")).willReturn(true);

        SaveSubscriptionChangesCommand cmd = new SaveSubscriptionChangesCommand(
                1L,
                List.of(new SaveSubscriptionChangesCommand.CreateEntry("FMS", START, END, true)),
                List.of(),
                List.of()
        );

        assertThatThrownBy(() -> subscriptionService.saveSubscriptionChanges(cmd))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("SUBSCRIPTION_DUPLICATE_MODULE");
                });
    }

    // ── saveSubscriptionChanges: startDate > endDate → SUBSCRIPTION_DATE_RANGE_INVALID 400 ──

    @Test
    void saveSubscriptionChanges_invalidDateRange_throwsBadRequest() {
        given(subscriberUseCase.getSubscriberById(1L)).willReturn(makeSubscriber(1L));

        LocalDate invalidStart = LocalDate.of(2025, 12, 31);
        LocalDate invalidEnd = LocalDate.of(2025, 1, 1);

        SaveSubscriptionChangesCommand cmd = new SaveSubscriptionChangesCommand(
                1L,
                List.of(new SaveSubscriptionChangesCommand.CreateEntry("FMS", invalidStart, invalidEnd, true)),
                List.of(),
                List.of()
        );

        assertThatThrownBy(() -> subscriptionService.saveSubscriptionChanges(cmd))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(appEx.getErrorCode()).isEqualTo("SUBSCRIPTION_DATE_RANGE_INVALID");
                });
    }

    // ── saveSubscriptionChanges: update 날짜 역전 → 400 ─────────────────────

    @Test
    void saveSubscriptionChanges_updateInvalidDateRange_throwsBadRequest() {
        given(subscriberUseCase.getSubscriberById(1L)).willReturn(makeSubscriber(1L));

        LocalDate invalidStart = LocalDate.of(2025, 12, 31);
        LocalDate invalidEnd = LocalDate.of(2025, 1, 1);

        SaveSubscriptionChangesCommand cmd = new SaveSubscriptionChangesCommand(
                1L,
                List.of(),
                List.of(new SaveSubscriptionChangesCommand.UpdateEntry(10L, invalidStart, invalidEnd, true)),
                List.of()
        );

        assertThatThrownBy(() -> subscriptionService.saveSubscriptionChanges(cmd))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    // ── saveSubscriptionChanges: delete 성공 ─────────────────────────────────

    @Test
    void saveSubscriptionChanges_deleteSuccess() {
        given(subscriberUseCase.getSubscriberById(1L)).willReturn(makeSubscriber(1L));

        SaveSubscriptionChangesCommand cmd = new SaveSubscriptionChangesCommand(
                1L, List.of(), List.of(), List.of(10L)
        );

        SaveChangesResult result = subscriptionService.saveSubscriptionChanges(cmd);

        assertThat(result.deletedCount()).isEqualTo(1);
        then(subscriptionPort).should().delete(10L);
    }
}
