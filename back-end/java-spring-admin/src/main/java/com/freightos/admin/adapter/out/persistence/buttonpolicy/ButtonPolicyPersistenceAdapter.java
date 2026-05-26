package com.freightos.admin.adapter.out.persistence.buttonpolicy;

import com.freightos.admin.application.buttonpolicy.command.SearchButtonPolicyCommand;
import com.freightos.admin.application.buttonpolicy.port.out.ButtonPolicyPort;
import com.freightos.admin.application.buttonpolicy.projection.ButtonPolicySummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.security.ButtonEvalRow;
import com.freightos.admin.common.security.PolicyRow;
import com.freightos.admin.domain.buttonpolicy.entity.ButtonPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ButtonPolicyPersistenceAdapter implements ButtonPolicyPort {

    private final ButtonPolicyRepository buttonPolicyRepository;
    private final ButtonPolicyDomainToJpaMapper buttonPolicyDomainToJpaMapper;
    private final ButtonPolicyJpaToDomainMapper buttonPolicyJpaToDomainMapper;

    @Override
    public PagedResult<ButtonPolicySummary> searchSummaries(SearchButtonPolicyCommand command) {
        return buttonPolicyRepository.searchSummaries(command);
    }

    @Override
    public Optional<ButtonPolicy> findButtonPolicyById(Long policyId) {
        return buttonPolicyRepository.findById(policyId).map(buttonPolicyJpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(ButtonPolicy buttonPolicy) {
        ButtonPolicyJpaEntity entity = buttonPolicyDomainToJpaMapper.toNewJpa(buttonPolicy);
        buttonPolicyRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void deleteButtonPolicyById(Long policyId) {
        if (!buttonPolicyRepository.existsById(policyId)) {
            throw ApplicationException.notFound("BUTTON_POLICY_NOT_FOUND", MessageCode.BUTTON_POLICY_NOT_FOUND.getMessage());
        }
        buttonPolicyRepository.deleteById(policyId);
    }

    @Override
    public boolean existsById(Long policyId) {
        return buttonPolicyRepository.existsById(policyId);
    }

    @Override
    public boolean existsByCompositeKey(Long buttonId, String attributeKey, String requiredValue) {
        return buttonPolicyRepository.existsByButtonIdAndAttributeKeyAndRequiredValue(buttonId, attributeKey, requiredValue);
    }

    @Override
    public boolean existsByButtonId(Long buttonId) {
        return buttonPolicyRepository.existsByButtonId(buttonId);
    }

    @Override
    public boolean existsByAttributeKey(String attributeKey) {
        return buttonPolicyRepository.existsByAttributeKey(attributeKey);
    }

    @Override
    public List<ButtonEvalRow> findAllActiveForEvaluation() {
        List<ButtonPolicyEvalProjection> rows = buttonPolicyRepository.findAllActiveButtonsWithPolicies();
        Map<Long, ButtonEvalRow> byButtonId = new HashMap<>();
        for (ButtonPolicyEvalProjection row : rows) {
            byButtonId.computeIfAbsent(row.getButtonId(), id -> new ButtonEvalRow(id, row.getButtonCode(), row.getLabel(), new ArrayList<>()));
            if (row.getAttributeKey() != null) {
                ((ArrayList<PolicyRow>) byButtonId.get(row.getButtonId()).policies())
                        .add(new PolicyRow(row.getAttributeKey(), row.getRequiredValue()));
            }
        }
        return new ArrayList<>(byButtonId.values());
    }
}
