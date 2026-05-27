package com.freightos.admin.application.customer;

import com.freightos.admin.application.customer.command.CreateCustomerCommand;
import com.freightos.admin.application.customer.command.UpdateCustomerCommand;
import com.freightos.admin.application.customer.port.out.CustomerPort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.domain.customer.entity.Customer;
import com.freightos.admin.domain.customer.entity.CustomerType;
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
class CustomerServiceTest {

    @Mock
    private CustomerPort customerPort;

    @Mock
    private CustomerFactory customerFactory;

    @InjectMocks
    private CustomerService customerService;

    // ── createCustomer: 정상 → id 반환 ──────────────────────────────────────────

    @Test
    void createCustomer_callsFactoryAndPortSaveReturnsId() {
        CreateCustomerCommand command = new CreateCustomerCommand(
                "CUS-001", CustomerType.CUSTOMER, "테스트 포워더", null,
                null, null, null, null, null, null, null, null, true);
        Customer domain = Customer.create("CUS-001", CustomerType.CUSTOMER, "테스트 포워더",
                null, null, null, null, null, null, null, null, null, true);
        given(customerFactory.from(command)).willReturn(domain);
        given(customerPort.save(domain)).willReturn(10L);

        Long id = customerService.createCustomer(command);

        assertThat(id).isEqualTo(10L);
        then(customerFactory).should().from(command);
        then(customerPort).should().save(domain);
    }

    // ── createCustomer: customer_code 중복 → DataIntegrityViolationException → 409 ─

    @Test
    void createCustomer_duplicateCode_throwsConflict() {
        CreateCustomerCommand command = new CreateCustomerCommand(
                "CUS-001", CustomerType.CUSTOMER, "중복 포워더", null,
                null, null, null, null, null, null, null, null, true);
        Customer domain = Customer.create("CUS-001", CustomerType.CUSTOMER, "중복 포워더",
                null, null, null, null, null, null, null, null, null, true);
        given(customerFactory.from(command)).willReturn(domain);
        given(customerPort.save(domain)).willThrow(new DataIntegrityViolationException("uq_admin_customer_code"));

        assertThatThrownBy(() -> customerService.createCustomer(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("CUSTOMER_DUPLICATE_CODE");
                });
    }

    // ── getCustomerById: not_found → 404 ─────────────────────────────────────────

    @Test
    void getCustomerById_notFound_throwsNotFound() {
        given(customerPort.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById(99L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(appEx.getErrorCode()).isEqualTo("CUSTOMER_NOT_FOUND");
                });
    }

    // ── getCustomerById: 존재 → 도메인 반환 ─────────────────────────────────────

    @Test
    void getCustomerById_found_returnsDomain() {
        Customer domain = Customer.create("CUS-001", CustomerType.CUSTOMER, "테스트",
                null, null, null, null, null, null, null, null, null, true);
        given(customerPort.findById(1L)).willReturn(Optional.of(domain));

        Customer result = customerService.getCustomerById(1L);

        assertThat(result).isEqualTo(domain);
    }

    // ── updateCustomer: 정상 → port.update 호출 ─────────────────────────────────

    @Test
    void updateCustomer_normal_callsPortUpdate() {
        Customer existing = Customer.create("CUS-001", CustomerType.CUSTOMER, "기존 이름",
                null, null, null, null, null, null, null, null, null, true);
        UpdateCustomerCommand command = new UpdateCustomerCommand(
                CustomerType.PARTNER, "변경 이름", null,
                null, null, null, null, null, null, null, null, true);
        given(customerPort.findById(1L)).willReturn(Optional.of(existing));

        customerService.updateCustomer(1L, command);

        then(customerPort).should().update(eq(1L), any(Customer.class));
    }

    // ── updateCustomer: 이미 삭제된 고객 → 409 ───────────────────────────────────

    @Test
    void updateCustomer_deletedCustomer_throwsConflict() {
        Customer deleted = Customer.create("CUS-001", CustomerType.CUSTOMER, "삭제된 포워더",
                null, null, null, null, null, null, null, null, null, true);
        deleted.assignDeletedAt(LocalDateTime.of(2024, 1, 1, 0, 0));
        UpdateCustomerCommand command = new UpdateCustomerCommand(
                CustomerType.CUSTOMER, "변경 이름", null,
                null, null, null, null, null, null, null, null, true);
        given(customerPort.findById(1L)).willReturn(Optional.of(deleted));

        assertThatThrownBy(() -> customerService.updateCustomer(1L, command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("CUSTOMER_ALREADY_DELETED");
                });
    }

    // ── deleteCustomer: 정상 → port.softDelete 호출 ─────────────────────────────

    @Test
    void deleteCustomer_normal_callsSoftDelete() {
        Customer existing = Customer.create("CUS-001", CustomerType.CUSTOMER, "테스트",
                null, null, null, null, null, null, null, null, null, true);
        given(customerPort.findById(5L)).willReturn(Optional.of(existing));

        customerService.deleteCustomer(5L);

        then(customerPort).should().softDelete(5L);
    }

    // ── deleteCustomer: 이미 삭제된 고객 → 409 ──────────────────────────────────

    @Test
    void deleteCustomer_deletedCustomer_throwsConflict() {
        Customer deleted = Customer.create("CUS-001", CustomerType.CUSTOMER, "삭제된 포워더",
                null, null, null, null, null, null, null, null, null, true);
        deleted.assignDeletedAt(LocalDateTime.of(2024, 1, 1, 0, 0));
        given(customerPort.findById(5L)).willReturn(Optional.of(deleted));

        assertThatThrownBy(() -> customerService.deleteCustomer(5L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("CUSTOMER_ALREADY_DELETED");
                });
    }
}
