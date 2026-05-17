package com.freightos.admin.application.terms;

import com.freightos.admin.application.terms.command.CreateTermsCommand;
import com.freightos.admin.domain.terms.entity.Terms;
import com.freightos.admin.domain.terms.entity.TermsType;
import org.springframework.stereotype.Component;

@Component
public class TermsFactory {

    public Terms from(CreateTermsCommand command) {
        TermsType type = TermsType.valueOf(command.type());
        return Terms.create(type, command.version(), command.effectiveAt(), command.content(), command.summary());
    }
}
