package com.freightos.fms.adapter.out.persistence.freight;

import com.freightos.fms.application.common.codename.CodeNameResolver;
import com.freightos.fms.application.freight.FreightView;
import com.freightos.fms.application.freight.command.FreightInputCommand;
import com.freightos.fms.application.freight.port.out.FreightInputPort;
import com.freightos.fms.domain.freight.enums.FreightBlType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * FreightInputPort 아웃바운드 어댑터 구현.
 * bms.freight_header + bms.freight_line 영속화 담당.
 */
@Component
@RequiredArgsConstructor
public class FreightInputPersistenceAdapter implements FreightInputPort {

    private final FreightHeaderJpaRepository headerRepository;
    private final FreightDomainToJpaMapper domainToJpaMapper;
    private final FreightJpaToDomainMapper jpaToDomainMapper;
    private final CodeNameResolver codeNameResolver;

    @Override
    @Transactional
    public void saveFreight(FreightBlType blType, String blId, FreightInputCommand cmd) {
        String blTypeName = blType.name();

        // 헤더 upsert: bl_type+bl_id로 조회 후 없으면 신규 생성
        FreightHeaderJpaEntity header = headerRepository
            .findByBlTypeAndBlId(blTypeName, blId)
            .orElseGet(FreightHeaderJpaEntity::new);

        boolean isNew = (header.getFreightHeaderId() == null);
        domainToJpaMapper.applyHeaderFields(cmd, header);

        if (isNew) {
            header.setBlType(blTypeName);
            header.setBlId(blId);
        }

        // 헤더 먼저 저장(신규 시 PK 확정)
        FreightHeaderJpaEntity savedHeader = headerRepository.save(header);

        // 라인 customer_code 집합으로 customer_type 1회 조회
        Set<String> customerCodes = extractCustomerCodes(cmd);
        Map<String, String> customerTypes = customerCodes.isEmpty()
            ? Collections.emptyMap()
            : codeNameResolver.findCustomerTypes(customerCodes);

        // 라인 전량 재구성 (orphanRemoval로 기존 라인 자동 DELETE)
        List<FreightLineJpaEntity> newLines = domainToJpaMapper.buildLineEntities(
            cmd.lines() != null ? cmd.lines() : Collections.emptyList(),
            cmd,
            customerTypes,
            savedHeader
        );
        savedHeader.syncLines(newLines);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FreightView> findFreightByBl(FreightBlType blType, String blId) {
        return headerRepository
            .findByBlTypeAndBlId(blType.name(), blId)
            .map(jpaToDomainMapper::toFreightView);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsFreightLines(FreightBlType blType, String blId) {
        return headerRepository
            .findByBlTypeAndBlId(blType.name(), blId)
            .map(header -> !header.getLines().isEmpty())
            .orElse(false);
    }

    @Override
    @Transactional
    public void deleteFreight(FreightBlType blType, String blId) {
        headerRepository.findByBlTypeAndBlId(blType.name(), blId)
            .ifPresent(headerRepository::delete);
        // orphanRemoval이 라인을 자동 삭제한다.
    }

    private Set<String> extractCustomerCodes(FreightInputCommand cmd) {
        if (cmd.lines() == null) return Collections.emptySet();
        return cmd.lines().stream()
            .map(line -> line.customerCode())
            .filter(code -> code != null && !code.isBlank())
            .collect(Collectors.toSet());
    }
}
