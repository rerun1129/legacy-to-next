package com.freightos.admin.application.faqcategory;

import com.freightos.admin.application.faqcategory.command.CreateFaqCategoryCommand;
import com.freightos.admin.domain.faqcategory.entity.FaqCategory;
import org.springframework.stereotype.Component;

@Component
public class FaqCategoryFactory {

    public FaqCategory from(CreateFaqCategoryCommand command) {
        return FaqCategory.create(command.name(), command.sortOrder(), command.active());
    }
}
