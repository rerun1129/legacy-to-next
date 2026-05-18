package com.freightos.admin.application.buttonpolicy;

import com.freightos.admin.application.attributedefinition.port.out.AttributeDefinitionPort;
import com.freightos.admin.application.button.port.out.ButtonPort;
import com.freightos.admin.application.buttonpolicy.command.CreateButtonPolicyCommand;
import com.freightos.admin.application.buttonpolicy.command.SearchButtonPolicyCommand;
import com.freightos.admin.application.buttonpolicy.port.in.ButtonPolicyUseCase;
import com.freightos.admin.application.buttonpolicy.port.out.ButtonPolicyPort;
import com.freightos.admin.application.buttonpolicy.projection.ButtonPolicySummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.buttonpolicy.entity.ButtonPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ButtonPolicyService implements ButtonPolicyUseCase {

    private final ButtonPolicyPort buttonPolicyPort;
    private final ButtonPort buttonPort;
    private final AttributeDefinitionPort attributeDefinitionPort;

    @Override
    public PagedResult<ButtonPolicySummary> searchButtonPolicies(SearchButtonPolicyCommand command) {
        return buttonPolicyPort.searchSummaries(command);
    }

    @Override
    public ButtonPolicy findButtonPolicyById(Long policyId) {
        return buttonPolicyPort.findButtonPolicyById(policyId)
                .orElseThrow(() -> ApplicationException.notFound("BUTTON_POLICY_NOT_FOUND", MessageCode.BUTTON_POLICY_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createButtonPolicy(CreateButtonPolicyCommand command) {
        if (!buttonPort.existsById(command.buttonId())) {
            throw ApplicationException.notFound("BUTTON_NOT_FOUND", MessageCode.BUTTON_NOT_FOUND.getMessage());
        }
        if (!attributeDefinitionPort.existsByKey(command.attributeKey())) {
            throw ApplicationException.notFound("ATTRIBUTE_DEFINITION_NOT_FOUND", MessageCode.ATTRIBUTE_DEFINITION_NOT_FOUND.getMessage());
        }
        if (buttonPolicyPort.existsByCompositeKey(command.buttonId(), command.attributeKey(), command.requiredValue())) {
            throw ApplicationException.conflict("BUTTON_POLICY_DUPLICATE", MessageCode.BUTTON_POLICY_DUPLICATE.getMessage());
        }
        ButtonPolicy buttonPolicy = ButtonPolicy.create(command.buttonId(), command.attributeKey(), command.requiredValue());
        return buttonPolicyPort.save(buttonPolicy);
    }

    @Override
    @Transactional
    public void deleteButtonPoliciesByIds(List<Long> ids) {
        for (Long id : ids) {
            deleteButtonPolicyById(id);
        }
    }

    @Override
    @Transactional
    public void deleteButtonPolicyById(Long policyId) {
        if (!buttonPolicyPort.existsById(policyId)) {
            throw ApplicationException.notFound("BUTTON_POLICY_NOT_FOUND", MessageCode.BUTTON_POLICY_NOT_FOUND.getMessage());
        }
        buttonPolicyPort.deleteButtonPolicyById(policyId);
    }
}
