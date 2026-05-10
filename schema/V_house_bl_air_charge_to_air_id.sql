-- house_bl_air_charge: 부모(house_bl) FK → ext(house_bl_air) FK 재배치
-- AIR JobDiv 전용 자식이므로 ext가 소유. ON DELETE CASCADE로 ext 삭제 시 자동 정리.
-- ER 재구조화 Phase 1 Step 1.2

-- 1) ext FK 컬럼 추가 (백필 전 임시 NULL)
ALTER TABLE fms.house_bl_air_charge
    ADD COLUMN IF NOT EXISTS house_bl_air_id BIGINT;

-- 2) 기존 데이터 백필 — house_bl_id로 ext PK 조회
UPDATE fms.house_bl_air_charge ac
   SET house_bl_air_id = (
       SELECT a.house_bl_air_id
         FROM fms.house_bl_air a
        WHERE a.house_bl_id = ac.house_bl_id
   )
 WHERE ac.house_bl_air_id IS NULL;

-- 3) NOT NULL 적용 (백필 후)
ALTER TABLE fms.house_bl_air_charge
    ALTER COLUMN house_bl_air_id SET NOT NULL;

-- 4) 신규 FK + ON DELETE CASCADE (ext 삭제 시 자식 자동 정리)
ALTER TABLE fms.house_bl_air_charge
    ADD CONSTRAINT fk_house_bl_air_charge_air
        FOREIGN KEY (house_bl_air_id)
        REFERENCES fms.house_bl_air(house_bl_air_id) ON DELETE CASCADE;

-- 5) 조회 성능 인덱스
CREATE INDEX IF NOT EXISTS idx_house_bl_air_charge_air_id
    ON fms.house_bl_air_charge(house_bl_air_id);

-- 6) 기존 house_bl_id FK 제거 + 컬럼 제거
ALTER TABLE fms.house_bl_air_charge
    DROP CONSTRAINT IF EXISTS house_bl_air_charge_house_bl_id_fkey;
ALTER TABLE fms.house_bl_air_charge
    DROP COLUMN IF EXISTS house_bl_id;

COMMENT ON COLUMN fms.house_bl_air_charge.house_bl_air_id IS 'House B/L AIR ext 참조 FK (cascade — ext 삭제 시 자식 자동 정리)';
