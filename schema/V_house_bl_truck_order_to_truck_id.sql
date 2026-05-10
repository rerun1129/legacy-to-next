-- house_bl_truck_order: 부모(house_bl) FK → ext(house_bl_truck) FK 재배치
-- TRUCK JobDiv 전용 자식이므로 ext가 소유. ON DELETE CASCADE로 ext 삭제 시 자동 정리.
-- ER 재구조화 Phase 1 Step 1.3

-- 1) ext FK 컬럼 추가 (백필 전 임시 NULL)
ALTER TABLE fms.house_bl_truck_order
    ADD COLUMN IF NOT EXISTS house_bl_truck_id BIGINT;

-- 2) 기존 데이터 백필 — house_bl_id로 ext PK 조회
UPDATE fms.house_bl_truck_order tor
   SET house_bl_truck_id = (
       SELECT t.house_bl_truck_id
         FROM fms.house_bl_truck t
        WHERE t.house_bl_id = tor.house_bl_id
   )
 WHERE tor.house_bl_truck_id IS NULL;

-- 3) NOT NULL 적용 (백필 후)
ALTER TABLE fms.house_bl_truck_order
    ALTER COLUMN house_bl_truck_id SET NOT NULL;

-- 4) 신규 FK + ON DELETE CASCADE (ext 삭제 시 자식 자동 정리)
ALTER TABLE fms.house_bl_truck_order
    ADD CONSTRAINT fk_house_bl_truck_order_truck
        FOREIGN KEY (house_bl_truck_id)
        REFERENCES fms.house_bl_truck(house_bl_truck_id) ON DELETE CASCADE;

-- 5) 조회 성능 인덱스
CREATE INDEX IF NOT EXISTS idx_house_bl_truck_order_truck_id
    ON fms.house_bl_truck_order(house_bl_truck_id);

-- 6) 기존 house_bl_id FK 제거 + 컬럼 제거
ALTER TABLE fms.house_bl_truck_order
    DROP CONSTRAINT IF EXISTS house_bl_truck_order_house_bl_id_fkey;
ALTER TABLE fms.house_bl_truck_order
    DROP COLUMN IF EXISTS house_bl_id;

COMMENT ON COLUMN fms.house_bl_truck_order.house_bl_truck_id IS 'House B/L TRUCK ext 참조 FK (cascade — ext 삭제 시 자식 자동 정리)';
