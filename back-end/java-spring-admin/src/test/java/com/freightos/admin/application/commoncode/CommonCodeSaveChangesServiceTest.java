package com.freightos.admin.application.commoncode;

import com.freightos.admin.application.commoncode.command.SaveCommonCodeChangesCommand;
import com.freightos.admin.application.commoncode.port.out.CommonCodeCachePort;
import com.freightos.admin.application.commoncode.port.out.CommonCodePort;
import com.freightos.admin.application.commoncode.projection.CommonCodeSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.commoncode.entity.CommonCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CommonCodeSaveChangesServiceTest {

    @Mock
    private CommonCodePort commonCodePort;

    @Mock
    private CommonCodeCachePort commonCodeCachePort;

    @InjectMocks
    private CommonCodeService service;

    // --- fixture helpers ---

    private static CommonCode existingCode(Long id) {
        CommonCode code = CommonCode.create("Bound", "EXP", "Export", "수출", 0, true);
        code.assignIdentity(id, null, null, null, null);
        return code;
    }

    // --- getCommonCodesByGroup ---

    @Test
    void getCommonCodesByGroup_groupNotFound_throwsNotFound() {
        given(commonCodePort.existsGroupByGroupCode("UNKNOWN")).willReturn(false);

        assertThatThrownBy(() -> service.getCommonCodesByGroup("UNKNOWN"))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void getCommonCodesByGroup_found_returnsSummaries() {
        CommonCodeSummary summary = new CommonCodeSummary(1L, "Bound", "EXP", "Export", "수출", 0, true);
        given(commonCodePort.existsGroupByGroupCode("Bound")).willReturn(true);
        given(commonCodePort.findCodeSummariesByGroupCode("Bound")).willReturn(List.of(summary));

        List<CommonCodeSummary> result = service.getCommonCodesByGroup("Bound");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().code()).isEqualTo("EXP");
    }

    // --- saveCommonCodeChanges: groupNotFound ---

    @Test
    void saveChanges_groupNotFound_throwsNotFound() {
        given(commonCodePort.existsGroupByGroupCode("NONE")).willReturn(false);
        SaveCommonCodeChangesCommand command = new SaveCommonCodeChangesCommand("NONE", List.of(), List.of());

        assertThatThrownBy(() -> service.saveCommonCodeChanges(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        then(commonCodePort).should(never()).saveCommonCode(any());
    }

    // --- saveCommonCodeChanges: create duplicate ---

    @Test
    void saveChanges_createDuplicate_throwsConflict() {
        given(commonCodePort.existsGroupByGroupCode("Bound")).willReturn(true);
        given(commonCodePort.existsByGroupCodeAndCode("Bound", "EXP")).willReturn(true);

        SaveCommonCodeChangesCommand.CreateCommonCodeItem item =
                new SaveCommonCodeChangesCommand.CreateCommonCodeItem("EXP", "Export", "수출", 0, true);
        SaveCommonCodeChangesCommand command =
                new SaveCommonCodeChangesCommand("Bound", List.of(item), List.of());

        assertThatThrownBy(() -> service.saveCommonCodeChanges(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus())
                        .isEqualTo(HttpStatus.CONFLICT));

        then(commonCodePort).should(never()).saveCommonCode(any());
    }

    // --- saveCommonCodeChanges: create OK ---

    @Test
    void saveChanges_createNew_callsSaveAndWriteThrough() {
        given(commonCodePort.existsGroupByGroupCode("Bound")).willReturn(true);
        given(commonCodePort.existsByGroupCodeAndCode("Bound", "NEW")).willReturn(false);
        given(commonCodePort.findActiveCodeSummariesByGroupCodeOrdered("Bound")).willReturn(List.of());

        SaveCommonCodeChangesCommand.CreateCommonCodeItem item =
                new SaveCommonCodeChangesCommand.CreateCommonCodeItem("NEW", "New", null, 5, true);
        SaveCommonCodeChangesCommand command =
                new SaveCommonCodeChangesCommand("Bound", List.of(item), List.of());

        SaveChangesResult result = service.saveCommonCodeChanges(command);

        assertThat(result.createdCount()).isEqualTo(1);
        assertThat(result.updatedCount()).isZero();
        then(commonCodePort).should().saveCommonCode(any());
        then(commonCodeCachePort).should().putGroupCodes(anyString(), any());
    }

    // --- saveCommonCodeChanges: update notFound ---

    @Test
    void saveChanges_updateNotFound_throwsNotFound() {
        given(commonCodePort.existsGroupByGroupCode("Bound")).willReturn(true);
        given(commonCodePort.findCommonCodeById(999L)).willReturn(Optional.empty());

        SaveCommonCodeChangesCommand.UpdateCommonCodeItem item =
                new SaveCommonCodeChangesCommand.UpdateCommonCodeItem(999L, "Export", "수출", 0, true);
        SaveCommonCodeChangesCommand command =
                new SaveCommonCodeChangesCommand("Bound", List.of(), List.of(item));

        assertThatThrownBy(() -> service.saveCommonCodeChanges(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // --- saveCommonCodeChanges: update OK ---

    @Test
    void saveChanges_updateExisting_callsUpdateByIdAndWriteThrough() {
        CommonCode existing = existingCode(1L);
        given(commonCodePort.existsGroupByGroupCode("Bound")).willReturn(true);
        given(commonCodePort.findCommonCodeById(1L)).willReturn(Optional.of(existing));
        given(commonCodePort.findActiveCodeSummariesByGroupCodeOrdered("Bound")).willReturn(List.of());

        SaveCommonCodeChangesCommand.UpdateCommonCodeItem item =
                new SaveCommonCodeChangesCommand.UpdateCommonCodeItem(1L, "Updated Export", "수정 수출", 0, true);
        SaveCommonCodeChangesCommand command =
                new SaveCommonCodeChangesCommand("Bound", List.of(), List.of(item));

        SaveChangesResult result = service.saveCommonCodeChanges(command);

        assertThat(result.updatedCount()).isEqualTo(1);
        then(commonCodePort).should().updateCommonCodeById(1L, existing);
        then(commonCodeCachePort).should().putGroupCodes(anyString(), any());
    }

    // --- saveCommonCodeChanges: empty → write-through still called ---

    @Test
    void saveChanges_emptyAll_returnsZeroAndTriggersWriteThrough() {
        given(commonCodePort.existsGroupByGroupCode("Bound")).willReturn(true);
        given(commonCodePort.findActiveCodeSummariesByGroupCodeOrdered("Bound")).willReturn(List.of());

        SaveChangesResult result = service.saveCommonCodeChanges(
                new SaveCommonCodeChangesCommand("Bound", List.of(), List.of()));

        assertThat(result.createdCount()).isZero();
        assertThat(result.updatedCount()).isZero();
        then(commonCodeCachePort).should().putGroupCodes(anyString(), any());
    }
}
