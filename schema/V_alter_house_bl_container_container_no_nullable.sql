-- house_bl_container.container_no NOT NULL 제약 해제 (NULLABLE로 변경)
-- Reason: 컨테이너 번호 미할당 상태의 등록을 허용하기 위함.
ALTER TABLE fms.house_bl_container
    ALTER COLUMN container_no DROP NOT NULL;
