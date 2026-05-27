package com.freightos.admin.application.customer;

import com.freightos.admin.application.customer.command.CreateCustomerCommand;
import com.freightos.admin.domain.customer.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerFactory {

    public Customer from(CreateCustomerCommand command) {
        return Customer.create(
                command.customerCode(),
                command.customerType(),
                command.name(),
                command.nameEn(),
                command.businessNo(),
                command.representative(),
                command.phone(),
                command.email(),
                command.customerLocalAddress(),
                command.customerEnglishAddress(),
                command.countryCode(),
                command.memo(),
                command.active()
        );
    }
}
