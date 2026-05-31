package com.freightos.admin.adapter.in.web.subscription;

import com.freightos.admin.adapter.in.web.subscription.dto.SaveSubscriptionChangesRequest;
import com.freightos.admin.adapter.in.web.subscription.dto.SubscriptionItemResponse;
import com.freightos.admin.application.subscription.port.in.SubscriptionUseCase;
import com.freightos.admin.application.subscription.projection.SubscriptionSummary;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.SaveChangesResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/subscriber/{subscriberId}/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionUseCase subscriptionUseCase;
    private final SubscriptionAssembler subscriptionAssembler;

    @GetMapping
    @PreAuthorize("hasAuthority('MENU_ADMIN_SUBSCRIBER_LIST')")
    public ResponseEntity<ApiResponse<List<SubscriptionItemResponse>>> getBySubscriberId(
            @PathVariable Long subscriberId) {
        List<SubscriptionSummary> summaries = subscriptionUseCase.getSubscriptionsBySubscriberId(subscriberId);
        List<SubscriptionItemResponse> responses = summaries.stream()
                .map(subscriptionAssembler::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.of(responses));
    }

    @PostMapping("/save-changes")
    @PreAuthorize("hasAuthority('BTN_ADMIN_SUBSCRIBER_SUBSCRIPTION_SAVE')")
    public ResponseEntity<ApiResponse<SaveChangesResult>> saveChanges(
            @PathVariable Long subscriberId,
            @Valid @RequestBody SaveSubscriptionChangesRequest req) {
        SaveChangesResult result = subscriptionUseCase.saveSubscriptionChanges(
                subscriptionAssembler.toSaveChangesCommand(subscriberId, req));
        return ResponseEntity.ok(ApiResponse.of(result, MessageCode.SUBSCRIPTION_SAVE_CHANGES.getMessage()));
    }
}
