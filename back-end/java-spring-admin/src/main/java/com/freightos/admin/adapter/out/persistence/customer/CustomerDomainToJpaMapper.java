package com.freightos.admin.adapter.out.persistence.customer;

import com.freightos.admin.domain.customer.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerDomainToJpaMapper {

    /** 신규 저장용 엔티티 생성. id는 null — JPA가 채운다. */
    public CustomerJpaEntity toNewJpa(Customer domain) {
        CustomerJpaEntity entity = new CustomerJpaEntity();
        entity.setCustomerCode(domain.getCustomerCode());
        entity.setCustomerType(domain.getCustomerType());
        entity.setName(domain.getName());
        entity.setNameEn(domain.getNameEn());
        entity.setBusinessNo(domain.getBusinessNo());
        entity.setRepresentative(domain.getRepresentative());
        entity.setPhone(domain.getPhone());
        entity.setEmail(domain.getEmail());
        entity.setCustomerLocalAddress(domain.getCustomerLocalAddress());
        entity.setCustomerEnglishAddress(domain.getCustomerEnglishAddress());
        entity.setCountryCode(domain.getCountryCode());
        entity.setMemo(domain.getMemo());
        entity.setActive(domain.isActive());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    /**
     * 갱신 가능한 필드만 적용. customerCode는 불변이므로 건드리지 않는다.
     */
    public void applyUpdateFields(CustomerJpaEntity entity, Customer patch) {
        entity.setCustomerType(patch.getCustomerType());
        entity.setName(patch.getName());
        entity.setNameEn(patch.getNameEn());
        entity.setBusinessNo(patch.getBusinessNo());
        entity.setRepresentative(patch.getRepresentative());
        entity.setPhone(patch.getPhone());
        entity.setEmail(patch.getEmail());
        entity.setCustomerLocalAddress(patch.getCustomerLocalAddress());
        entity.setCustomerEnglishAddress(patch.getCustomerEnglishAddress());
        entity.setCountryCode(patch.getCountryCode());
        entity.setMemo(patch.getMemo());
        entity.setActive(patch.isActive());
    }
}
