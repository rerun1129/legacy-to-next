package com.freightos.fms.adapter.out.persistence.masterbl.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class MasterBlJpaEntityTest {

    // ── replaceDesc ─────────────────────────────────────────────────

    @Test
    @DisplayName("replaceDesc: 기존 desc가 있으면 기존 desc의 masterBl back-ref를 null로 만든다")
    void replaceDesc_existingDesc_nullsOldBackRef() {
        MasterBlJpaEntity entity = new MasterBlJpaEntity();
        MasterBlDescJpaEntity oldDesc = new MasterBlDescJpaEntity();
        oldDesc.setMasterBl(entity);
        entity.replaceDesc(oldDesc);

        MasterBlDescJpaEntity newDesc = new MasterBlDescJpaEntity();
        entity.replaceDesc(newDesc);

        assertThat(oldDesc.getMasterBl()).isNull();
        assertThat(entity.getDesc()).isEqualTo(newDesc);
    }

    @Test
    @DisplayName("replaceDesc: null 입력 시 기존 desc의 back-ref를 null로 만들고 desc도 null 처리")
    void replaceDesc_nullInput_clearsExistingDesc() {
        MasterBlJpaEntity entity = new MasterBlJpaEntity();
        MasterBlDescJpaEntity existingDesc = new MasterBlDescJpaEntity();
        existingDesc.setMasterBl(entity);
        entity.replaceDesc(existingDesc);

        entity.replaceDesc(null);

        assertThat(existingDesc.getMasterBl()).isNull();
        assertThat(entity.getDesc()).isNull();
    }

    @Test
    @DisplayName("replaceDesc: 기존 desc가 없을 때 null 입력해도 예외 없이 처리된다")
    void replaceDesc_noExistingDesc_nullInput_noException() {
        MasterBlJpaEntity entity = new MasterBlJpaEntity();

        assertThatCode(() -> entity.replaceDesc(null)).doesNotThrowAnyException();
        assertThat(entity.getDesc()).isNull();
    }
}
