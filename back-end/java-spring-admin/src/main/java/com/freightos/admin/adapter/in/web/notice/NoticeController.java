package com.freightos.admin.adapter.in.web.notice;

import com.freightos.admin.adapter.in.web.notice.dto.CreateNoticeRequest;
import com.freightos.admin.adapter.in.web.notice.dto.NoticeDetailResponse;
import com.freightos.admin.adapter.in.web.notice.dto.NoticeSummaryResponse;
import com.freightos.admin.adapter.in.web.notice.dto.SearchNoticeRequest;
import com.freightos.admin.adapter.in.web.notice.dto.UpdateNoticeRequest;
import com.freightos.admin.application.notice.port.in.NoticeUseCase;
import com.freightos.admin.application.notice.projection.NoticeSummary;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.notice.entity.Notice;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/cms/notice")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAuthority('CMS_MANAGE') or hasRole('ADMIN')")
public class NoticeController {

    private final NoticeUseCase noticeUseCase;
    private final NoticeAssembler noticeAssembler;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<NoticeSummaryResponse>>> search(
            @Valid @RequestBody SearchNoticeRequest req) {
        PagedResult<NoticeSummary> result = noticeUseCase.searchNotices(noticeAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(noticeAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeDetailResponse>> getById(@PathVariable Long id) {
        Notice domain = noticeUseCase.getNoticeById(id);
        return ResponseEntity.ok(ApiResponse.of(noticeAssembler.toDetail(domain)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreateNoticeRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = noticeUseCase.createNotice(noticeAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/cms/notice/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.NOTICE_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNoticeRequest req) {
        noticeUseCase.updateNotice(id, noticeAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.NOTICE_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        noticeUseCase.deleteNotice(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.NOTICE_DELETED.getMessage()));
    }
}
