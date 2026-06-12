package com.freightos.fms.adapter.in.web.attachment;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.common.security.GatewayProperties;
import com.freightos.fms.application.attachment.command.UploadBlAttachmentCommand;
import com.freightos.fms.application.attachment.port.in.BlAttachmentUseCase;
import com.freightos.fms.application.attachment.projection.BlAttachmentContent;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.attachment.entity.BlAttachment;
import com.freightos.fms.domain.attachment.enums.AttachmentBlKind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(BlAttachmentController.class)
@ActiveProfiles("test")
class BlAttachmentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BlAttachmentUseCase blAttachmentUseCase;

    @MockitoBean
    private BlAttachmentAssembler blAttachmentAssembler;

    @MockitoBean
    @SuppressWarnings("unused")
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    @SuppressWarnings("unused")
    private GatewayProperties gatewayProperties;

    // ── GET /api/bl-attachment?blKind=HOUSE&blId=1 ─────────────────

    @Test
    @DisplayName("GET /api/bl-attachment: blKind=HOUSE, blId=1 → 200 + 목록 반환")
    void getAttachments_happyPath_returns200() throws Exception {
        BlAttachment attachment = new BlAttachment(1L, AttachmentBlKind.HOUSE, 1L, "test.pdf",
                "HOUSE/1/uuid", "application/pdf", 1024L, "user1", LocalDateTime.now());
        given(blAttachmentAssembler.toBlKind("HOUSE")).willReturn(AttachmentBlKind.HOUSE);
        given(blAttachmentUseCase.findAttachmentsByBl(AttachmentBlKind.HOUSE, 1L))
                .willReturn(List.of(attachment));
        given(blAttachmentAssembler.toResponseList(any())).willReturn(List.of());

        mockMvc.perform(get("/api/bl-attachment")
                        .param("blKind", "HOUSE")
                        .param("blId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        then(blAttachmentUseCase).should().findAttachmentsByBl(AttachmentBlKind.HOUSE, 1L);
    }

    @Test
    @DisplayName("GET /api/bl-attachment: blKind 유효하지 않은 값 → 400")
    void getAttachments_invalidBlKind_returns400() throws Exception {
        given(blAttachmentAssembler.toBlKind("INVALID"))
                .willThrow(new IllegalArgumentException("유효하지 않은 blKind: INVALID"));

        mockMvc.perform(get("/api/bl-attachment")
                        .param("blKind", "INVALID")
                        .param("blId", "1"))
                .andExpect(status().isBadRequest());
    }

    // ── POST /api/bl-attachment (multipart) ───────────────────────

    @Test
    @DisplayName("POST /api/bl-attachment: multipart 업로드 happy path → 201 + id + Location")
    void uploadAttachment_happyPath_returns201() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf-content".getBytes());
        given(blAttachmentAssembler.toUploadCommand(any(), any(), any(), any()))
                .willReturn(new UploadBlAttachmentCommand(
                        AttachmentBlKind.HOUSE, 1L, "test.pdf", "application/pdf",
                        11L, new ByteArrayInputStream("pdf-content".getBytes()), "tester"));
        given(blAttachmentUseCase.upload(any())).willReturn(42L);

        mockMvc.perform(multipart("/api/bl-attachment")
                        .file(file)
                        .param("blKind", "HOUSE")
                        .param("blId", "1")
                        .principal(new UsernamePasswordAuthenticationToken("tester", "", List.of())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(42))
                .andExpect(jsonPath("$.message").value(MessageCode.BL_ATTACHMENT_UPLOADED.message()))
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.endsWith("/api/bl-attachment/42/download")));
    }

    @Test
    @DisplayName("POST /api/bl-attachment: UseCase가 빈 파일 IllegalArgumentException → 400")
    void uploadAttachment_emptyFile_returns400() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.txt", MediaType.TEXT_PLAIN_VALUE, new byte[0]);
        given(blAttachmentAssembler.toUploadCommand(any(), any(), any(), any()))
                .willReturn(new UploadBlAttachmentCommand(
                        AttachmentBlKind.HOUSE, 1L, "empty.txt", "text/plain",
                        0L, new ByteArrayInputStream(new byte[0]), "tester"));
        given(blAttachmentUseCase.upload(any()))
                .willThrow(new IllegalArgumentException("업로드 파일 크기가 0입니다."));

        mockMvc.perform(multipart("/api/bl-attachment")
                        .file(emptyFile)
                        .param("blKind", "HOUSE")
                        .param("blId", "1")
                        .principal(new UsernamePasswordAuthenticationToken("tester", "", List.of())))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/bl-attachment/{id}/download ─────────────────────

    @Test
    @DisplayName("GET /api/bl-attachment/1/download: 다운로드 헤더 검증 → Content-Disposition+Content-Length")
    void downloadAttachment_happyPath_returnsFileStream() throws Exception {
        byte[] fileBytes = "file-content".getBytes();
        BlAttachmentContent content = new BlAttachmentContent(
                "test.pdf", "application/pdf", fileBytes.length, new ByteArrayInputStream(fileBytes));
        given(blAttachmentUseCase.download(1L)).willReturn(content);

        mockMvc.perform(get("/api/bl-attachment/1/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("test.pdf")))
                .andExpect(header().longValue("Content-Length", fileBytes.length))
                .andExpect(content().contentType("application/pdf"));
    }

    @Test
    @DisplayName("GET /api/bl-attachment/999/download: 미존재 → 404 ProblemDetail")
    void downloadAttachment_notFound_returns404() throws Exception {
        given(blAttachmentUseCase.download(999L))
                .willThrow(new ResourceNotFoundException(MessageCode.BL_ATTACHMENT_NOT_FOUND));

        mockMvc.perform(get("/api/bl-attachment/999/download"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value(MessageCode.BL_ATTACHMENT_NOT_FOUND.message()));
    }

    // ── DELETE /api/bl-attachment/{id} ───────────────────────────

    @Test
    @DisplayName("DELETE /api/bl-attachment/1: 200 + BL_ATTACHMENT_DELETED 메시지")
    void deleteAttachment_happyPath_returns200() throws Exception {
        mockMvc.perform(delete("/api/bl-attachment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(MessageCode.BL_ATTACHMENT_DELETED.message()));

        then(blAttachmentUseCase).should().deleteAttachmentById(1L);
    }

    @Test
    @DisplayName("DELETE /api/bl-attachment/999: 미존재 → 404")
    void deleteAttachment_notFound_returns404() throws Exception {
        willThrow(new ResourceNotFoundException(MessageCode.BL_ATTACHMENT_NOT_FOUND))
                .given(blAttachmentUseCase).deleteAttachmentById(999L);

        mockMvc.perform(delete("/api/bl-attachment/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
