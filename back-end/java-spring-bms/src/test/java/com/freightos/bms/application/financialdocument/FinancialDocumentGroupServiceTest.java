package com.freightos.bms.application.financialdocument;

import com.freightos.bms.application.financialdocument.command.ApplyGroupingCommand;
import com.freightos.bms.application.financialdocument.port.out.FinancialDocumentPort;
import com.freightos.bms.application.financialdocument.port.out.GroupDocumentSnapshot;
import com.freightos.bms.application.financialdocument.port.out.GroupNumberGenerator;
import com.freightos.bms.domain.financialdocument.enums.GroupCategory;
import com.freightos.common.exception.FmsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * FinancialDocumentGroupService Mockito 단위 테스트.
 * 고정 입력·고정 기대값. 시간/랜덤/병렬 없음(T1).
 */
@ExtendWith(MockitoExtension.class)
class FinancialDocumentGroupServiceTest {

    @Mock
    private FinancialDocumentPort financialDocumentPort;

    @Mock
    private GroupNumberGenerator groupNumberGenerator;

    @InjectMocks
    private FinancialDocumentGroupService service;

    // ── 스냅샷 헬퍼 ───────────────────────────────────────────────────────────

    private GroupDocumentSnapshot snap(Long id, String type, String status, String groupNo) {
        return new GroupDocumentSnapshot(id, "CUST001", type, status, groupNo, "20260601");
    }

    // ── 테스트 케이스 ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("set diff: scope=[1,2,3], grouped=[1,2] → 1·2 부여, 3 해제")
    void applyGrouping_setDiff_assignAndRelease() {
        given(financialDocumentPort.loadGroupSnapshots(anyList())).willReturn(List.of(
            snap(1L, "INVOICE", "CREATED", null),
            snap(2L, "INVOICE", "CREATED", null),
            snap(3L, "INVOICE", "GROUPED", "GI260600001")
        ));
        given(groupNumberGenerator.nextSeq(GroupCategory.INVOICE, "2606")).willReturn(2);

        ApplyGroupingCommand cmd = new ApplyGroupingCommand(
            List.of(1L, 2L), List.of(1L, 2L, 3L)
        );
        GroupResult result = service.applyGrouping(cmd);

        assertThat(result.groupFinancialNo()).isEqualTo("GI260600002");
        assertThat(result.groupedDocumentIds()).containsExactlyInAnyOrder(1L, 2L);
        assertThat(result.ungroupedDocumentIds()).containsExactly(3L);

        then(financialDocumentPort).should().bulkAssignGroupNo(anyList(), eq("GI260600002"));
        then(financialDocumentPort).should().bulkClearGroupNo(List.of(3L));
        // 3번은 GROUPED 상태이므로 CREATED 강등
        then(financialDocumentPort).should().bulkUpdateDocumentStatus(List.of(3L), "CREATED");
    }

    @Test
    @DisplayName("합류: 우측 1건이 기존 그룹 → 신규 채번 미호출, 기존 그룹번호 재사용")
    void applyGrouping_joinExistingGroup_noNewSeq() {
        given(financialDocumentPort.loadGroupSnapshots(anyList())).willReturn(List.of(
            snap(1L, "INVOICE", "GROUPED", "GI260600001"),
            snap(2L, "INVOICE", "CREATED", null)
        ));

        ApplyGroupingCommand cmd = new ApplyGroupingCommand(
            List.of(1L, 2L), List.of(1L, 2L)
        );
        GroupResult result = service.applyGrouping(cmd);

        assertThat(result.groupFinancialNo()).isEqualTo("GI260600001");
        then(groupNumberGenerator).should(never()).nextSeq(any(), any());
        // 2번은 아직 그룹 없음이므로 부여
        then(financialDocumentPort).should().bulkAssignGroupNo(anyList(), eq("GI260600001"));
    }

    @Test
    @DisplayName("신규 채번: 전원 groupFinancialNo null → nextSeq 1회 호출")
    void applyGrouping_allNull_callsNextSeqOnce() {
        given(financialDocumentPort.loadGroupSnapshots(anyList())).willReturn(List.of(
            snap(1L, "INVOICE", "CREATED", null),
            snap(2L, "INVOICE", "CREATED", null)
        ));
        given(groupNumberGenerator.nextSeq(GroupCategory.INVOICE, "2606")).willReturn(1);

        ApplyGroupingCommand cmd = new ApplyGroupingCommand(
            List.of(1L, 2L), null
        );
        GroupResult result = service.applyGrouping(cmd);

        assertThat(result.groupFinancialNo()).isEqualTo("GI260600001");
        then(groupNumberGenerator).should().nextSeq(GroupCategory.INVOICE, "2606");
    }

    @Test
    @DisplayName("동일 고객사 위반: customerCode 2종 → CONFLICT")
    void applyGrouping_mixedCustomer_throwsConflict() {
        given(financialDocumentPort.loadGroupSnapshots(anyList())).willReturn(List.of(
            new GroupDocumentSnapshot(1L, "CUST001", "INVOICE", "CREATED", null, "20260601"),
            new GroupDocumentSnapshot(2L, "CUST002", "INVOICE", "CREATED", null, "20260601")
        ));

        ApplyGroupingCommand cmd = new ApplyGroupingCommand(
            List.of(1L, 2L), null
        );

        assertThatThrownBy(() -> service.applyGrouping(cmd))
            .isInstanceOf(FmsException.class)
            .satisfies(e -> assertThat(((FmsException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    @DisplayName("카테고리 혼합(INVOICE+PAYMENT): CONFLICT")
    void applyGrouping_invoiceAndPayment_throwsConflict() {
        given(financialDocumentPort.loadGroupSnapshots(anyList())).willReturn(List.of(
            snap(1L, "INVOICE", "CREATED", null),
            snap(2L, "PAYMENT", "CREATED", null)
        ));

        ApplyGroupingCommand cmd = new ApplyGroupingCommand(
            List.of(1L, 2L), null
        );

        assertThatThrownBy(() -> service.applyGrouping(cmd))
            .isInstanceOf(FmsException.class)
            .satisfies(e -> assertThat(((FmsException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    @DisplayName("DEBIT+CREDIT 혼합: 같은 DCNOTE 카테고리 → 정상 처리")
    void applyGrouping_debitAndCredit_sameCategory_passes() {
        given(financialDocumentPort.loadGroupSnapshots(anyList())).willReturn(List.of(
            snap(1L, "DEBIT", "CREATED", null),
            snap(2L, "CREDIT", "CREATED", null)
        ));
        given(groupNumberGenerator.nextSeq(GroupCategory.DCNOTE, "2606")).willReturn(1);

        ApplyGroupingCommand cmd = new ApplyGroupingCommand(
            List.of(1L, 2L), null
        );
        GroupResult result = service.applyGrouping(cmd);

        assertThat(result.groupFinancialNo()).isEqualTo("GD260600001");
    }

    @Test
    @DisplayName("다중 기존 그룹 차단: grouped에 서로 다른 그룹 2개 존재 → CONFLICT")
    void applyGrouping_multipleExistingGroups_throwsConflict() {
        given(financialDocumentPort.loadGroupSnapshots(anyList())).willReturn(List.of(
            snap(1L, "INVOICE", "GROUPED", "GI260600001"),
            snap(2L, "INVOICE", "GROUPED", "GI260600002")
        ));

        ApplyGroupingCommand cmd = new ApplyGroupingCommand(
            List.of(1L, 2L), null
        );

        assertThatThrownBy(() -> service.applyGrouping(cmd))
            .isInstanceOf(FmsException.class)
            .satisfies(e -> assertThat(((FmsException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    @DisplayName("상태 승급: CREATED → GROUPED, TAX는 bulkUpdateDocumentStatus 미호출")
    void applyGrouping_promote_createdOnly_taxPreserved() {
        given(financialDocumentPort.loadGroupSnapshots(anyList())).willReturn(List.of(
            snap(1L, "INVOICE", "CREATED", null),
            snap(2L, "INVOICE", "TAX", null)
        ));
        given(groupNumberGenerator.nextSeq(GroupCategory.INVOICE, "2606")).willReturn(1);

        ApplyGroupingCommand cmd = new ApplyGroupingCommand(
            List.of(1L, 2L), null
        );
        service.applyGrouping(cmd);

        // CREATED(1번)만 승급 대상
        then(financialDocumentPort).should().bulkUpdateDocumentStatus(List.of(1L), "GROUPED");
        // TAX(2번)는 승급 안 함 — 호출 총 1회만
    }

    @Test
    @DisplayName("상태 강등: GROUPED만 CREATED로 강등, TAX는 보존")
    void applyGrouping_demote_groupedOnly_taxPreserved() {
        given(financialDocumentPort.loadGroupSnapshots(anyList())).willReturn(List.of(
            snap(1L, "INVOICE", "GROUPED", "GI260600001"),
            snap(2L, "INVOICE", "TAX", "GI260600001")
        ));

        // grouped 빈 리스트, scope=[1,2] → 1·2 모두 해제
        ApplyGroupingCommand cmd = new ApplyGroupingCommand(
            List.of(), List.of(1L, 2L)
        );
        GroupResult result = service.applyGrouping(cmd);

        assertThat(result.groupFinancialNo()).isNull();
        assertThat(result.ungroupedDocumentIds()).containsExactlyInAnyOrder(1L, 2L);
        // GROUPED인 1번만 강등
        then(financialDocumentPort).should().bulkUpdateDocumentStatus(List.of(1L), "CREATED");
        // TAX인 2번은 강등 안 함
    }
}
