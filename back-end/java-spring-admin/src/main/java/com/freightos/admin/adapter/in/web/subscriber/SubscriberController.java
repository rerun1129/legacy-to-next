package com.freightos.admin.adapter.in.web.subscriber;

import com.freightos.admin.adapter.in.web.subscriber.dto.CreateSubscriberRequest;
import com.freightos.admin.adapter.in.web.subscriber.dto.SaveSubscriberChangesRequest;
import com.freightos.admin.adapter.in.web.subscriber.dto.SearchSubscriberRequest;
import com.freightos.admin.adapter.in.web.subscriber.dto.SubscriberDetailResponse;
import com.freightos.admin.adapter.in.web.subscriber.dto.SubscriberSummaryResponse;
import com.freightos.admin.adapter.in.web.subscriber.dto.UpdateSubscriberRequest;
import com.freightos.admin.application.subscriber.port.in.SubscriberUseCase;
import com.freightos.admin.application.subscriber.projection.SubscriberSummary;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.subscriber.entity.Subscriber;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/subscriber")
@RequiredArgsConstructor
@Validated
public class SubscriberController {

    private final SubscriberUseCase subscriberUseCase;
    private final SubscriberAssembler subscriberAssembler;

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('MENU_ADMIN_SUBSCRIBER_LIST')")
    public ResponseEntity<ApiResponse<PagedResult<SubscriberSummaryResponse>>> search(
            @Valid @RequestBody SearchSubscriberRequest req) {
        PagedResult<SubscriberSummary> result = subscriberUseCase.searchSubscribers(subscriberAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(subscriberAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU_ADMIN_SUBSCRIBER_LIST')")
    public ResponseEntity<ApiResponse<SubscriberDetailResponse>> getById(@PathVariable Long id) {
        Subscriber domain = subscriberUseCase.getSubscriberById(id);
        return ResponseEntity.ok(ApiResponse.of(subscriberAssembler.toDetail(domain)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_ADMIN_SUBSCRIBER_LIST_SAVE')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreateSubscriberRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = subscriberUseCase.createSubscriber(subscriberAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/subscriber/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.SUBSCRIBER_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_SUBSCRIBER_LIST_SAVE')")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubscriberRequest req) {
        subscriberUseCase.updateSubscriber(id, subscriberAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.SUBSCRIBER_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_SUBSCRIBER_LIST_SAVE')")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        subscriberUseCase.deleteSubscriber(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.SUBSCRIBER_DELETED.getMessage()));
    }

    @PostMapping("/save-changes")
    @PreAuthorize("hasAuthority('BTN_ADMIN_SUBSCRIBER_LIST_SAVE')")
    public ResponseEntity<ApiResponse<SaveChangesResult>> saveChanges(
            @Valid @RequestBody SaveSubscriberChangesRequest req) {
        SaveChangesResult result = subscriberUseCase.saveSubscriberChanges(subscriberAssembler.toSaveChangesCommand(req));
        return ResponseEntity.ok(ApiResponse.of(result, MessageCode.SUBSCRIBER_SAVE_CHANGES.getMessage()));
    }

    @GetMapping("/autocomplete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AutocompleteItem>>> autocomplete(
            @RequestParam String q,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit) {
        return ResponseEntity.ok(ApiResponse.of(subscriberUseCase.autocompleteSubscribers(q, limit)));
    }
}
