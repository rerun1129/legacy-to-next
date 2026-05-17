package com.freightos.admin.application.partner;

import com.freightos.admin.application.partner.command.CreatePartnerCommand;
import com.freightos.admin.application.partner.command.UpdatePartnerCommand;
import com.freightos.admin.application.partner.port.out.PartnerPort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.domain.partner.entity.Partner;
import com.freightos.admin.domain.partner.entity.PartnerType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
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
class PartnerServiceTest {

    @Mock
    private PartnerPort partnerPort;

    @Mock
    private PartnerFactory partnerFactory;

    @InjectMocks
    private PartnerService partnerService;

    // ── createPartner: 정상 → id 반환 ───────────────────────────────────────────

    @Test
    void createPartner_callsFactoryAndPortSaveReturnsId() {
        CreatePartnerCommand command = new CreatePartnerCommand(
                "FWD-001", PartnerType.FORWARDER, "테스트 포워더", null,
                null, null, null, null, null, null, true);
        Partner domain = Partner.create("FWD-001", PartnerType.FORWARDER, "테스트 포워더",
                null, null, null, null, null, null, null, true);
        given(partnerFactory.from(command)).willReturn(domain);
        given(partnerPort.save(domain)).willReturn(10L);

        Long id = partnerService.createPartner(command);

        assertThat(id).isEqualTo(10L);
        then(partnerFactory).should().from(command);
        then(partnerPort).should().save(domain);
    }

    // ── createPartner: partner_code 중복 → DataIntegrityViolationException → 409 ─

    @Test
    void createPartner_duplicateCode_throwsConflict() {
        CreatePartnerCommand command = new CreatePartnerCommand(
                "FWD-001", PartnerType.FORWARDER, "중복 포워더", null,
                null, null, null, null, null, null, true);
        Partner domain = Partner.create("FWD-001", PartnerType.FORWARDER, "중복 포워더",
                null, null, null, null, null, null, null, true);
        given(partnerFactory.from(command)).willReturn(domain);
        given(partnerPort.save(domain)).willThrow(new DataIntegrityViolationException("uq_admin_partner_code"));

        assertThatThrownBy(() -> partnerService.createPartner(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("PARTNER_DUPLICATE_CODE");
                });
    }

    // ── getPartnerById: not_found → 404 ─────────────────────────────────────────

    @Test
    void getPartnerById_notFound_throwsNotFound() {
        given(partnerPort.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> partnerService.getPartnerById(99L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(appEx.getErrorCode()).isEqualTo("PARTNER_NOT_FOUND");
                });
    }

    // ── getPartnerById: 존재 → 도메인 반환 ──────────────────────────────────────

    @Test
    void getPartnerById_found_returnsDomain() {
        Partner domain = Partner.create("FWD-001", PartnerType.FORWARDER, "테스트",
                null, null, null, null, null, null, null, true);
        given(partnerPort.findById(1L)).willReturn(Optional.of(domain));

        Partner result = partnerService.getPartnerById(1L);

        assertThat(result).isEqualTo(domain);
    }

    // ── updatePartner: 정상 → port.update 호출 ──────────────────────────────────

    @Test
    void updatePartner_normal_callsPortUpdate() {
        Partner existing = Partner.create("FWD-001", PartnerType.FORWARDER, "기존 이름",
                null, null, null, null, null, null, null, true);
        UpdatePartnerCommand command = new UpdatePartnerCommand(
                PartnerType.SHIPPER, "변경 이름", null,
                null, null, null, null, null, null, true);
        given(partnerPort.findById(1L)).willReturn(Optional.of(existing));

        partnerService.updatePartner(1L, command);

        then(partnerPort).should().update(eq(1L), any(Partner.class));
    }

    // ── updatePartner: 이미 삭제된 협력사 → 409 ─────────────────────────────────

    @Test
    void updatePartner_deletedPartner_throwsConflict() {
        Partner deleted = Partner.create("FWD-001", PartnerType.FORWARDER, "삭제된 포워더",
                null, null, null, null, null, null, null, true);
        deleted.assignDeletedAt(LocalDateTime.of(2024, 1, 1, 0, 0));
        UpdatePartnerCommand command = new UpdatePartnerCommand(
                PartnerType.FORWARDER, "변경 이름", null,
                null, null, null, null, null, null, true);
        given(partnerPort.findById(1L)).willReturn(Optional.of(deleted));

        assertThatThrownBy(() -> partnerService.updatePartner(1L, command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("PARTNER_ALREADY_DELETED");
                });
    }

    // ── deletePartner: 정상 → port.softDelete 호출 ──────────────────────────────

    @Test
    void deletePartner_normal_callsSoftDelete() {
        Partner existing = Partner.create("FWD-001", PartnerType.FORWARDER, "테스트",
                null, null, null, null, null, null, null, true);
        given(partnerPort.findById(5L)).willReturn(Optional.of(existing));

        partnerService.deletePartner(5L);

        then(partnerPort).should().softDelete(5L);
    }

    // ── deletePartner: 이미 삭제된 협력사 → 409 ─────────────────────────────────

    @Test
    void deletePartner_deletedPartner_throwsConflict() {
        Partner deleted = Partner.create("FWD-001", PartnerType.FORWARDER, "삭제된 포워더",
                null, null, null, null, null, null, null, true);
        deleted.assignDeletedAt(LocalDateTime.of(2024, 1, 1, 0, 0));
        given(partnerPort.findById(5L)).willReturn(Optional.of(deleted));

        assertThatThrownBy(() -> partnerService.deletePartner(5L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("PARTNER_ALREADY_DELETED");
                });
    }
}
