-- V31: customer.name NOT NULL 해제 (운영 데이터에 name NULL 존재)

ALTER TABLE admin.customer ALTER COLUMN name DROP NOT NULL;
