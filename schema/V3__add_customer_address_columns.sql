-- V3: CustomerCode address 필드 추가
-- house_bl: 당사자 4슬롯 (shipper / consignee / notify / doc_partner)
ALTER TABLE house_bl
    ADD COLUMN shipper_address     VARCHAR(500),
    ADD COLUMN consignee_address   VARCHAR(500),
    ADD COLUMN notify_address      VARCHAR(500),
    ADD COLUMN doc_partner_address VARCHAR(500);

-- master_bl: 당사자 3슬롯 (shipper / consignee / notify)
ALTER TABLE master_bl
    ADD COLUMN shipper_address   VARCHAR(500),
    ADD COLUMN consignee_address VARCHAR(500),
    ADD COLUMN notify_address    VARCHAR(500);

-- switch_bl: 당사자 3슬롯 (shipper / consignee / notify)
ALTER TABLE switch_bl
    ADD COLUMN shipper_address   VARCHAR(500),
    ADD COLUMN consignee_address VARCHAR(500),
    ADD COLUMN notify_address    VARCHAR(500);
