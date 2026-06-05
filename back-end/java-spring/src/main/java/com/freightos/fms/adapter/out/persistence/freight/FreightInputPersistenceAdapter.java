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
    private final FinancialDocumentRefRepository financialDocumentRefRepository;

    @Override
    @Transactional
    public void saveFreight(FreightBlType blType, Long blId, FreightInputCommand cmd) {
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

        // 미발행 라인만 교체 — 발행 라인(financial_document_id != null)은 보존.
        // FE는 발행 라인을 write payload에서 제외하므로 cmd.lines()는 미발행 재구성분만 담긴다.
        List<FreightLineJpaEntity> newLines = domainToJpaMapper.buildLineEntities(
            cmd.lines() != null ? cmd.lines() : Collections.emptyList(),
            cmd,
            customerTypes,
            savedHeader
        );
        savedHeader.syncUnissuedLines(newLines);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FreightView> findFreightByBl(FreightBlType blType, Long blId) {
        return headerRepository
            .findByBlTypeAndBlId(blType.name(), blId)
            .map(header -> {
                Set<String> customerCodes = header.getLines().stream()
                    .map(FreightLineJpaEntity::getCustomerCode)
                    .filter(c -> c != null && !c.isBlank())
                    .collect(Collectors.toSet());
                Set<String> freightCodes = header.getLines().stream()
                    .map(FreightLineJpaEntity::getFreightCode)
                    .filter(c -> c != null && !c.isBlank())
                    .collect(Collectors.toSet());
                Set<Long> documentIds = header.getLines().stream()
                    .map(FreightLineJpaEntity::getFinancialDocumentId)
                    .filter(id -> id != null)
                    .collect(Collectors.toSet());

                Map<String, String> customerNames = customerCodes.isEmpty()
                    ? Collections.emptyMap()
                    : codeNameResolver.findCustomerNames(customerCodes);
                Map<String, String> freightNames = freightCodes.isEmpty()
                    ? Collections.emptyMap()
                    : codeNameResolver.findFreightNames(freightCodes);
                Map<Long, String> documentNoMap = documentIds.isEmpty()
                    ? Collections.emptyMap()
                    : buildDocumentNoMap(documentIds);

                return jpaToDomainMapper.toFreightView(header, customerNames, freightNames, documentNoMap);
            });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsFreightLines(FreightBlType blType, Long blId) {
        return headerRepository
            .findByBlTypeAndBlId(blType.name(), blId)
            .map(header -> !header.getLines().isEmpty())
            .orElse(false);
    }

    @Override
    @Transactional
    public void deleteFreight(FreightBlType blType, Long blId) {
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

    /**
     * financial_document_id 집합 → id:document_no 맵 조회.
     * 빈 입력 시 호출하지 말 것(호출 전 isEmpty 체크 필수).
     */
    private Map<Long, String> buildDocumentNoMap(Set<Long> documentIds) {
        return financialDocumentRefRepository.findByFinancialDocumentIdIn(documentIds).stream()
            .collect(Collectors.toMap(
                FinancialDocumentRefJpaEntity::getFinancialDocumentId,
                FinancialDocumentRefJpaEntity::getDocumentNo
            ));
    }
}
