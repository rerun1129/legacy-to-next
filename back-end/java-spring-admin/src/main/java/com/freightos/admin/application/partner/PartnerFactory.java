package com.freightos.admin.application.partner;

import com.freightos.admin.application.partner.command.CreatePartnerCommand;
import com.freightos.admin.domain.partner.entity.Partner;
import org.springframework.stereotype.Component;

@Component
public class PartnerFactory {

    public Partner from(CreatePartnerCommand command) {
        return Partner.create(
                command.partnerCode(),
                command.partnerType(),
                command.name(),
                command.nameEn(),
                command.businessNo(),
                command.representative(),
                command.phone(),
                command.email(),
                command.address(),
                command.memo(),
                command.active()
        );
    }
}
