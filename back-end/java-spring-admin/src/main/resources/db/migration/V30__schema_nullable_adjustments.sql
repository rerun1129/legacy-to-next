-- V30: 데이터 적재 과정에서 확인된 스키마 조정
-- carrier_type/port_type/name 등 NOT NULL 해제 + edi_code 크기 확장

-- ========== 1. Carrier ==========
ALTER TABLE admin.carrier ALTER COLUMN carrier_type DROP NOT NULL;
ALTER TABLE admin.carrier ALTER COLUMN edi_code TYPE VARCHAR(10);

-- ========== 2. Port ==========
ALTER TABLE admin.port ALTER COLUMN port_type DROP NOT NULL;
ALTER TABLE admin.port ALTER COLUMN name DROP NOT NULL;

-- ========== 3. ExchangeRate ==========
ALTER TABLE admin.exchange_rate ALTER COLUMN name DROP NOT NULL;

-- ========== 4. Customer ==========
ALTER TABLE admin.customer ALTER COLUMN customer_type DROP NOT NULL;
