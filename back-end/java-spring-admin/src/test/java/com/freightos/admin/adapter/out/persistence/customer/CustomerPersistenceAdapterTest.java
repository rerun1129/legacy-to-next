package com.freightos.admin.adapter.out.persistence.customer;

import com.freightos.admin.application.customer.command.SearchCustomerCommand;
import com.freightos.admin.application.customer.projection.CustomerSummary;
import com.freightos.admin.common.config.JpaAuditingConfig;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.customer.entity.Customer;
import com.freightos.admin.domain.customer.entity.CustomerType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({CustomerPersistenceAdapter.class, CustomerDomainToJpaMapper.class, CustomerJpaToDomainMapper.class, CustomerRepositoryImpl.class, JpaAuditingConfig.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:customerpa;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;INIT=CREATE SCHEMA IF NOT EXISTS admin",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=admin",
        "spring.flyway.enabled=false"
})
class CustomerPersistenceAdapterTest {

    @Autowired
    private CustomerPersistenceAdapter adapter;

    @Autowired
    private CustomerRepository customerRepository;

    // ── save → id 반환 + findById 재조회 + 필드 일치 ─────────────────────────

    @Test
    void save_thenFindById_fieldsMatch() {
        Customer customer = Customer.create("CUS-001", CustomerType.FORWARDER, "글로벌 포워더",
                "Global Forwarder", "123-45-67890", "홍길동", "010-1234-5678",
                "test@fwd.com", "서울시 강남구", "Seoul Gangnam", "메모 내용", true);

        Long id = adapter.save(customer);

        Optional<Customer> found = adapter.findById(id);
        assertThat(found).isPresent();
        Customer result = found.get();
        assertThat(result.getCustomerCode()).isEqualTo("CUS-001");
        assertThat(result.getCustomerType()).isEqualTo(CustomerType.FORWARDER);
        assertThat(result.getName()).isEqualTo("글로벌 포워더");
        assertThat(result.isActive()).isTrue();
        assertThat(result.isDeleted()).isFalse();
        // id는 동적 참조 (T1: 시퀀스 절대값 비교 금지)
        assertThat(result.getId()).isEqualTo(id);
    }

    // ── customer_code UNIQUE 위반 → DataIntegrityViolationException ──────────

    @Test
    void save_duplicateCustomerCode_throwsDataIntegrityViolation() {
        Customer first = Customer.create("DUP-001", CustomerType.SHIPPER, "첫 번째 화주",
                null, null, null, null, null, null, null, null, true);
        adapter.save(first);
        customerRepository.flush();

        Customer second = Customer.create("DUP-001", CustomerType.CARRIER, "두 번째 운송사",
                null, null, null, null, null, null, null, null, true);

        assertThatThrownBy(() -> {
            adapter.save(second);
            customerRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    // ── softDelete 후 findById 는 deletedAt이 있는 도메인 반환 ─────────────────

    @Test
    void softDelete_thenFindById_returnsDomainWithDeletedAt() {
        Customer customer = Customer.create("SHP-001", CustomerType.SHIPPER, "화주 회사",
                null, null, null, null, null, null, null, null, true);
        Long id = adapter.save(customer);

        adapter.softDelete(id);

        Optional<Customer> found = adapter.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().isDeleted()).isTrue();
        assertThat(found.get().isActive()).isFalse();
        assertThat(found.get().getDeletedAt()).isNotNull();
    }

    // ── searchSummaries: scope=ALL(기본값) → soft deleted 제외 ──────────────

    @Test
    void searchSummaries_scopeAll_softDeletedNotReturned() {
        Customer active = Customer.create("ACT-001", CustomerType.AGENT, "활성 에이전트",
                null, null, null, null, null, null, null, null, true);
        Long activeId = adapter.save(active);

        Customer toDelete = Customer.create("DEL-001", CustomerType.AGENT, "삭제될 에이전트",
                null, null, null, null, null, null, null, null, true);
        Long deletedId = adapter.save(toDelete);
        adapter.softDelete(deletedId);

        SearchCustomerCommand command = new SearchCustomerCommand(null, null, null, "ALL", 0, 20);
        PagedResult<CustomerSummary> result = adapter.searchSummaries(command);

        List<Long> ids = result.getContent().stream().map(CustomerSummary::id).toList();
        assertThat(ids).contains(activeId);
        assertThat(ids).doesNotContain(deletedId);
    }

    // ── searchSummaries: scope=DELETED → soft deleted 만 반환 ────────────────

    @Test
    void searchSummaries_scopeDeleted_onlySoftDeletedReturned() {
        Customer active = Customer.create("INC-ACT", CustomerType.FORWARDER, "포함 활성",
                null, null, null, null, null, null, null, null, true);
        Long activeId = adapter.save(active);

        Customer deleted = Customer.create("INC-DEL", CustomerType.FORWARDER, "포함 삭제",
                null, null, null, null, null, null, null, null, true);
        Long deletedId = adapter.save(deleted);
        adapter.softDelete(deletedId);

        SearchCustomerCommand command = new SearchCustomerCommand("INC-", null, null, "DELETED", 0, 20);
        PagedResult<CustomerSummary> result = adapter.searchSummaries(command);

        List<Long> ids = result.getContent().stream().map(CustomerSummary::id).toList();
        assertThat(ids).contains(deletedId);
        assertThat(ids).doesNotContain(activeId);
    }

    // ── searchSummaries: customerType 필터 ───────────────────────────────────

    @Test
    void searchSummaries_filterByCustomerType_returnsOnlyMatchingType() {
        adapter.save(Customer.create("TYPE-FWD", CustomerType.FORWARDER, "포워더 회사", null, null, null, null, null, null, null, null, true));
        adapter.save(Customer.create("TYPE-SHP", CustomerType.SHIPPER, "화주 회사", null, null, null, null, null, null, null, null, true));
        adapter.save(Customer.create("TYPE-CSG", CustomerType.CONSIGNEE, "수하인 회사", null, null, null, null, null, null, null, null, true));

        SearchCustomerCommand command = new SearchCustomerCommand("TYPE-", null, "FORWARDER", "ALL", 0, 20);
        PagedResult<CustomerSummary> result = adapter.searchSummaries(command);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).customerType()).isEqualTo(CustomerType.FORWARDER);
    }

    // ── searchSummaries: 정렬 customer_code asc + id asc tie-break ───────────

    @Test
    void searchSummaries_sortByCustomerCodeAscThenIdAsc() {
        adapter.save(Customer.create("SORT-C", CustomerType.CARRIER, "운송사 C", null, null, null, null, null, null, null, null, true));
        adapter.save(Customer.create("SORT-A", CustomerType.AGENT, "에이전트 A", null, null, null, null, null, null, null, null, true));
        adapter.save(Customer.create("SORT-B", CustomerType.CUSTOMS_BROKER, "관세사 B", null, null, null, null, null, null, null, null, true));

        SearchCustomerCommand command = new SearchCustomerCommand("SORT-", null, null, "ALL", 0, 20);
        PagedResult<CustomerSummary> result = adapter.searchSummaries(command);

        assertThat(result.getContent()).hasSize(3);
        // customer_code asc 정렬 — A, B, C 순
        assertThat(result.getContent().get(0).customerCode()).isEqualTo("SORT-A");
        assertThat(result.getContent().get(1).customerCode()).isEqualTo("SORT-B");
        assertThat(result.getContent().get(2).customerCode()).isEqualTo("SORT-C");
    }

    // ── searchSummaries: name 부분일치 LIKE ──────────────────────────────────

    @Test
    void searchSummaries_filterByNameLike_returnsMatchingNames() {
        adapter.save(Customer.create("NAME-001", CustomerType.FORWARDER, "글로벌 물류", null, null, null, null, null, null, null, null, true));
        adapter.save(Customer.create("NAME-002", CustomerType.SHIPPER, "로컬 화주", null, null, null, null, null, null, null, null, true));
        adapter.save(Customer.create("NAME-003", CustomerType.CONSIGNEE, "글로벌 수하인", null, null, null, null, null, null, null, null, true));

        SearchCustomerCommand command = new SearchCustomerCommand(null, "글로벌", null, "ALL", 0, 20);
        PagedResult<CustomerSummary> result = adapter.searchSummaries(command);

        assertThat(result.getTotalElements()).isEqualTo(2L);
        List<String> names = result.getContent().stream().map(CustomerSummary::customerCode).toList();
        assertThat(names).containsExactlyInAnyOrder("NAME-001", "NAME-003");
    }
}
