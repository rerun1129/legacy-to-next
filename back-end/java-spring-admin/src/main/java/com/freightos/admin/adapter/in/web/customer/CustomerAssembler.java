package com.freightos.admin.adapter.in.web.customer;

import com.freightos.admin.adapter.in.web.customer.dto.CreateCustomerRequest;
import com.freightos.admin.adapter.in.web.customer.dto.CustomerDetailResponse;
import com.freightos.admin.adapter.in.web.customer.dto.CustomerSummaryResponse;
import com.freightos.admin.adapter.in.web.customer.dto.SearchCustomerRequest;
import com.freightos.admin.adapter.in.web.customer.dto.UpdateCustomerRequest;
import com.freightos.admin.application.customer.command.CreateCustomerCommand;
import com.freightos.admin.application.customer.command.SearchCustomerCommand;
import com.freightos.admin.application.customer.command.UpdateCustomerCommand;
import com.freightos.admin.application.customer.projection.CustomerSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.customer.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerAssembler {

    public SearchCustomerCommand toSearchCommand(SearchCustomerRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchCustomerCommand(req.customerCode(), req.name(), req.customerType(), req.scope(), req.page(), size);
    }

    public CreateCustomerCommand toCreateCommand(CreateCustomerRequest req) {
        return new CreateCustomerCommand(req.customerCode(), req.customerType(), req.name(), req.nameEn(), req.businessNo(), req.representative(), req.phone(), req.email(), req.customerLocalAddress(), req.customerEnglishAddress(), req.memo(), req.active());
    }

    public UpdateCustomerCommand toUpdateCommand(UpdateCustomerRequest req) {
        return new UpdateCustomerCommand(req.customerType(), req.name(), req.nameEn(), req.businessNo(), req.representative(), req.phone(), req.email(), req.customerLocalAddress(), req.customerEnglishAddress(), req.memo(), req.active());
    }

    public CustomerSummaryResponse toSummaryResponse(CustomerSummary p) {
        return new CustomerSummaryResponse(p.id(), p.customerCode(), p.customerType(), p.name(), p.nameEn(), p.businessNo(), p.representative(), p.phone(), p.email(), p.customerLocalAddress(), p.customerEnglishAddress(), p.memo(), p.active(), p.deletedAt(), p.updatedAt());
    }

    public CustomerDetailResponse toDetail(Customer domain) {
        return new CustomerDetailResponse(
                domain.getId(), domain.getCustomerCode(), domain.getCustomerType(),
                domain.getName(), domain.getNameEn(), domain.getBusinessNo(),
                domain.getRepresentative(), domain.getPhone(), domain.getEmail(),
                domain.getCustomerLocalAddress(), domain.getCustomerEnglishAddress(),
                domain.getMemo(), domain.isActive(), domain.getDeletedAt(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<CustomerSummaryResponse> toSummaryPage(PagedResult<CustomerSummary> src) {
        return src.map(this::toSummaryResponse);
    }
}
