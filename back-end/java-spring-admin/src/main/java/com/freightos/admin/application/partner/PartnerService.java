package com.freightos.admin.application.partner;

import com.freightos.admin.application.partner.command.CreatePartnerCommand;
import com.freightos.admin.application.partner.command.SearchPartnerCommand;
import com.freightos.admin.application.partner.command.UpdatePartnerCommand;
import com.freightos.admin.application.partner.port.in.PartnerUseCase;
import com.freightos.admin.application.partner.port.out.PartnerPort;
import com.freightos.admin.application.partner.projection.PartnerSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.partner.entity.Partner;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartnerService implements PartnerUseCase {

    private final PartnerPort partnerPort;
    private final PartnerFactory partnerFactory;

    @Override
    public PagedResult<PartnerSummary> searchPartners(SearchPartnerCommand command) {
        return partnerPort.searchSummaries(command);
    }

    @Override
    public Partner getPartnerById(Long id) {
        return partnerPort.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("PARTNER_NOT_FOUND", MessageCode.PARTNER_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createPartner(CreatePartnerCommand command) {
        try {
            return partnerPort.save(partnerFactory.from(command));
        } catch (DataIntegrityViolationException e) {
            throw ApplicationException.conflict("PARTNER_DUPLICATE_CODE", MessageCode.PARTNER_DUPLICATE_CODE.getMessage());
        }
    }

    @Override
    @Transactional
    public void updatePartner(Long id, UpdatePartnerCommand command) {
        Partner existing = getPartnerById(id);
        if (existing.isDeleted()) {
            throw ApplicationException.conflict("PARTNER_ALREADY_DELETED", MessageCode.PARTNER_ALREADY_DELETED.getMessage());
        }
        existing.applyUpdate(
                command.partnerType(), command.name(), command.nameEn(),
                command.businessNo(), command.representative(), command.phone(),
                command.email(), command.address(), command.memo(), command.active()
        );
        partnerPort.update(id, existing);
    }

    @Override
    @Transactional
    public void deletePartner(Long id) {
        Partner existing = getPartnerById(id);
        if (existing.isDeleted()) {
            throw ApplicationException.conflict("PARTNER_ALREADY_DELETED", MessageCode.PARTNER_ALREADY_DELETED.getMessage());
        }
        partnerPort.softDelete(id);
    }
}
