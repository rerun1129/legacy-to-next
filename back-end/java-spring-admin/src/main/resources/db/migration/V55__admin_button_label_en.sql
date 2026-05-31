-- V55: admin.button 에 label_en(영문 표시명) 컬럼 추가 + 기존 행 백필

ALTER TABLE admin.button ADD COLUMN label_en VARCHAR(200);
COMMENT ON COLUMN admin.button.label_en IS '버튼 표시명 (영어, 옵셔널)';

-- 한국어 label → 영문 label_en 백필
UPDATE admin.button
SET label_en = CASE label
    WHEN '신규'  THEN 'New'
    WHEN '수정'  THEN 'Save'
    WHEN '삭제'  THEN 'Delete'
END
WHERE label IN ('신규', '수정', '삭제')
  AND label_en IS NULL;

-- 이미 영문으로 입력된 label 행은 label_en = label 로 채움
UPDATE admin.button
SET label_en = label
WHERE label NOT IN ('신규', '수정', '삭제')
  AND label_en IS NULL;
