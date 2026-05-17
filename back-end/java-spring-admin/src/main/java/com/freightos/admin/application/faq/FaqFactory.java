package com.freightos.admin.application.faq;

import com.freightos.admin.application.faq.command.CreateFaqCommand;
import com.freightos.admin.domain.faq.entity.Faq;
import org.springframework.stereotype.Component;

@Component
public class FaqFactory {

    public Faq from(CreateFaqCommand command) {
        return Faq.create(command.faqCategoryId(), command.question(), command.answer(), command.sortOrder(), command.active());
    }
}
