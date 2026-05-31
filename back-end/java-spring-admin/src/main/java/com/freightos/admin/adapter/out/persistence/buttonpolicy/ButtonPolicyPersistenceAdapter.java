package com.freightos.admin.adapter.out.persistence.buttonpolicy;

import com.freightos.admin.application.buttonpolicy.port.out.ButtonPolicyPort;
import com.freightos.admin.common.security.ButtonEvalRow;
import com.freightos.admin.common.security.PolicyRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ButtonPolicyPersistenceAdapter implements ButtonPolicyPort {

    private final ButtonPolicyRepository buttonPolicyRepository;

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
            byButtonId.computeIfAbsent(row.getButtonId(), id -> new ButtonEvalRow(id, row.getButtonCode(), row.getLabel(), row.getLabelEn(), new ArrayList<>()));
            if (row.getAttributeKey() != null) {
                ((ArrayList<PolicyRow>) byButtonId.get(row.getButtonId()).policies())
                        .add(new PolicyRow(row.getAttributeKey(), row.getRequiredValue()));
            }
        }
        return new ArrayList<>(byButtonId.values());
    }
}
