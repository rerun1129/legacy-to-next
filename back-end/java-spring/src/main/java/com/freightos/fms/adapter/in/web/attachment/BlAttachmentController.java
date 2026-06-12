package com.freightos.fms.adapter.in.web.attachment;

import com.freightos.common.response.ApiResponse;
import com.freightos.fms.adapter.in.web.attachment.dto.BlAttachmentResponse;
import com.freightos.fms.application.attachment.command.UploadBlAttachmentCommand;
import com.freightos.fms.application.attachment.port.in.BlAttachmentUseCase;
import com.freightos.fms.application.attachment.projection.BlAttachmentContent;
import com.freightos.fms.common.response.MessageCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Tag(name = "B/L 첨부파일", description = "B/L 첨부파일 업로드/다운로드/삭제")
@RestController
@RequestMapping("/api/bl-attachment")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('MENU_FMS_HOUSE_BL','MENU_FMS_MASTER_BL','MENU_FMS_TRUCK_BL','MENU_FMS_NON_BL')")
public class BlAttachmentController {

    private final BlAttachmentUseCase blAttachmentUseCase;
    private final BlAttachmentAssembler blAttachmentAssembler;

    @Operation(summary = "B/L 첨부파일 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BlAttachmentResponse>>> getAttachments(
            @RequestParam String blKind,
            @RequestParam Long blId) {
        List<BlAttachmentResponse> responses = blAttachmentAssembler.toResponseList(
                blAttachmentUseCase.findAttachmentsByBl(blAttachmentAssembler.toBlKind(blKind), blId));
        return ResponseEntity.ok(ApiResponse.of(responses));
    }

    @Operation(summary = "B/L 첨부파일 업로드")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Long>>> uploadAttachment(
            @RequestPart MultipartFile file,
            @RequestParam String blKind,
            @RequestParam Long blId,
            Authentication authentication,
            UriComponentsBuilder uriBuilder) {
        String uploadedBy = authentication.getName();
        UploadBlAttachmentCommand command = blAttachmentAssembler.toUploadCommand(file, blKind, blId, uploadedBy);
        Long id = blAttachmentUseCase.upload(command);
        URI location = uriBuilder.path("/api/bl-attachment/{id}/download").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.BL_ATTACHMENT_UPLOADED.message()));
    }

    @Operation(summary = "B/L 첨부파일 다운로드")
    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadAttachment(@PathVariable Long id) {
        BlAttachmentContent content = blAttachmentUseCase.download(id);
        String mimeType = content.contentType() != null ? content.contentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(content.originalFilename(), StandardCharsets.UTF_8)
                        .build());
        headers.setContentLength(content.fileSize());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(mimeType))
                .body(new InputStreamResource(content.stream()));
    }

    @Operation(summary = "B/L 첨부파일 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(@PathVariable Long id) {
        blAttachmentUseCase.deleteAttachmentById(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.BL_ATTACHMENT_DELETED.message()));
    }
}
