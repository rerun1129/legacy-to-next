package com.freightos.fms.adapter.out.storage;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.attachment.port.out.StoragePort.StoredObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class S3StorageAdapterTest {

    @Mock private S3Client s3Client;

    private S3StorageProperties properties;
    private S3StorageAdapter adapter;

    @BeforeEach
    void setUp() {
        properties = new S3StorageProperties();
        properties.setBucket("test-bucket");
        properties.setRegion("ap-northeast-2");
        properties.setKeyPrefix("attachments/");
        adapter = new S3StorageAdapter(s3Client, properties);
    }

    // ── store ────────────────────────────────────────────────────────

    @Test
    @DisplayName("store: bucket·prefix+key·contentLength 요청 캡처")
    void store_capturesCorrectRequest() {
        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .willReturn(PutObjectResponse.builder().build());
        InputStream content = new ByteArrayInputStream("data".getBytes());

        adapter.store("HOUSE/1/file.pdf", content, 4L);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        then(s3Client).should().putObject(requestCaptor.capture(), any(RequestBody.class));
        PutObjectRequest captured = requestCaptor.getValue();
        assertThat(captured.bucket()).isEqualTo("test-bucket");
        assertThat(captured.key()).isEqualTo("attachments/HOUSE/1/file.pdf");
    }

    // ── load ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("load: 정상 → S3Client.getObject 결과 반환(기대 바이트 읽기 검증)")
    void load_normal_returnsStream() throws Exception {
        byte[] expectedBytes = "content".getBytes();
        ResponseInputStream<GetObjectResponse> responseStream = new ResponseInputStream<>(
                GetObjectResponse.builder().build(),
                AbortableInputStream.create(new ByteArrayInputStream(expectedBytes)));
        given(s3Client.getObject(any(GetObjectRequest.class))).willReturn(responseStream);

        InputStream result = adapter.load("HOUSE/1/file.pdf");

        assertThat(result.readAllBytes()).isEqualTo(expectedBytes);
        ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.forClass(GetObjectRequest.class);
        then(s3Client).should().getObject(captor.capture());
        assertThat(captor.getValue().key()).isEqualTo("attachments/HOUSE/1/file.pdf");
    }

    @Test
    @DisplayName("load: NoSuchKeyException → ResourceNotFoundException")
    void load_noSuchKey_throwsResourceNotFoundException() {
        given(s3Client.getObject(any(GetObjectRequest.class)))
                .willThrow(NoSuchKeyException.builder().build());

        assertThatThrownBy(() -> adapter.load("HOUSE/1/missing.pdf"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── delete ───────────────────────────────────────────────────────

    @Test
    @DisplayName("delete: 정상 → true 반환")
    void delete_normal_returnsTrue() {
        given(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .willReturn(software.amazon.awssdk.services.s3.model.DeleteObjectResponse.builder().build());

        boolean result = adapter.delete("HOUSE/1/file.pdf");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("delete: SdkClientException → false 반환(예외 전파 금지)")
    void delete_sdkException_returnsFalseWithoutThrowing() {
        given(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .willThrow(SdkClientException.builder().message("network error").build());

        assertThatNoException().isThrownBy(() -> {
            boolean result = adapter.delete("HOUSE/1/file.pdf");
            assertThat(result).isFalse();
        });
    }

    // ── list ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("list: continuationToken 2페이지 전수 수집 + prefix strip + lastModified")
    void list_twoPages_collectsAllAndStripsPrefix() {
        Instant t1 = Instant.parse("2026-01-01T00:00:00Z");
        Instant t2 = Instant.parse("2026-01-02T00:00:00Z");
        Instant t3 = Instant.parse("2026-01-03T00:00:00Z");

        S3Object obj1 = S3Object.builder().key("attachments/HOUSE/1/a.pdf").lastModified(t1).build();
        S3Object obj2 = S3Object.builder().key("attachments/HOUSE/2/b.pdf").lastModified(t2).build();
        S3Object obj3 = S3Object.builder().key("attachments/MASTER/1/c.pdf").lastModified(t3).build();

        ListObjectsV2Response page1 = ListObjectsV2Response.builder()
                .contents(List.of(obj1, obj2))
                .isTruncated(true)
                .nextContinuationToken("token-page2")
                .build();
        ListObjectsV2Response page2 = ListObjectsV2Response.builder()
                .contents(List.of(obj3))
                .isTruncated(false)
                .build();

        given(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .willReturn(page1, page2);

        List<StoredObject> result = adapter.list();

        assertThat(result).hasSize(3);
        // prefix가 제거된 storageKey 형태인지 확인
        assertThat(result).extracting(StoredObject::key)
                .containsExactlyInAnyOrder("HOUSE/1/a.pdf", "HOUSE/2/b.pdf", "MASTER/1/c.pdf");
        // lastModified 보존 확인
        assertThat(result).extracting(StoredObject::lastModified)
                .containsExactlyInAnyOrder(t1, t2, t3);
        // 페이지 2회 호출 확인
        then(s3Client).should(times(2)).listObjectsV2(any(ListObjectsV2Request.class));
    }
}
