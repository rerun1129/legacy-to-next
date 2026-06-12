package com.freightos.fms.adapter.out.storage;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.attachment.port.out.StoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileSystemStorageAdapterTest {

    @TempDir
    Path tempDir;

    private LocalFileSystemStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        LocalStorageProperties props = new LocalStorageProperties();
        props.setBasePath(tempDir.toString());
        adapter = new LocalFileSystemStorageAdapter(props);
    }

    // ── store + load 라운드트립 ─────────────────────────────────

    @Test
    @DisplayName("store → load 라운드트립: 저장한 내용을 그대로 읽어온다")
    void storeAndLoad_roundTrip() throws IOException {
        byte[] content = "hello-file-content".getBytes();
        adapter.store("HOUSE/1/uuid-test", new ByteArrayInputStream(content), content.length);

        InputStream loaded = adapter.load("HOUSE/1/uuid-test");
        byte[] read = loaded.readAllBytes();

        assertThat(read).isEqualTo(content);
    }

    @Test
    @DisplayName("load: 존재하지 않는 key → ResourceNotFoundException")
    void load_notExists_throwsResourceNotFoundException() {
        assertThatThrownBy(() -> adapter.load("HOUSE/1/non-existent"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── delete ────────────────────────────────────────────────────

    @Test
    @DisplayName("delete: 존재하는 파일 삭제 → true 반환")
    void delete_existingFile_returnsTrue() throws IOException {
        byte[] content = "delete-me".getBytes();
        adapter.store("HOUSE/1/to-delete", new ByteArrayInputStream(content), content.length);

        boolean deleted = adapter.delete("HOUSE/1/to-delete");

        assertThat(deleted).isTrue();
    }

    @Test
    @DisplayName("delete: 존재하지 않는 파일 삭제 → false 반환, 예외 미발생")
    void delete_notExists_returnsFalse() {
        boolean deleted = adapter.delete("HOUSE/1/ghost");
        assertThat(deleted).isFalse();
    }

    // ── 경로 탈출 방어 ─────────────────────────────────────────

    @Test
    @DisplayName("store: '../' 경로 탈출 시도 → IllegalArgumentException")
    void store_pathTraversal_throwsIllegalArgument() {
        assertThatThrownBy(() -> adapter.store(
                "../outside.txt",
                new ByteArrayInputStream("evil".getBytes()),
                4L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("허용되지 않은");
    }

    @Test
    @DisplayName("load: '../' 경로 탈출 시도 → IllegalArgumentException")
    void load_pathTraversal_throwsIllegalArgument() {
        assertThatThrownBy(() -> adapter.load("../../etc/passwd"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("허용되지 않은");
    }

    // ── list ─────────────────────────────────────────────────────

    @Test
    @DisplayName("list: 저장된 파일들이 StoredObject 목록으로 반환된다")
    void list_returnsStoredObjects() {
        adapter.store("HOUSE/1/file1", new ByteArrayInputStream("a".getBytes()), 1L);
        adapter.store("MASTER/2/file2", new ByteArrayInputStream("b".getBytes()), 1L);

        List<StoragePort.StoredObject> objects = adapter.list();

        assertThat(objects).hasSize(2);
        assertThat(objects).extracting(StoragePort.StoredObject::key)
                .containsExactlyInAnyOrder("HOUSE/1/file1", "MASTER/2/file2");
    }

    @Test
    @DisplayName("list: basePath 미존재 시 빈 목록 반환")
    void list_emptyBasePath_returnsEmpty() {
        LocalStorageProperties props = new LocalStorageProperties();
        props.setBasePath(tempDir.resolve("nonexistent").toString());
        LocalFileSystemStorageAdapter emptyAdapter = new LocalFileSystemStorageAdapter(props);

        assertThat(emptyAdapter.list()).isEmpty();
    }
}
