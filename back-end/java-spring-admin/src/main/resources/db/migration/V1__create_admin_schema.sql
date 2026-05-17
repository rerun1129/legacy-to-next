-- Admin module schema bootstrap.
-- Creates the dedicated 'admin' PostgreSQL schema used by the java-spring-admin module.
-- Flyway는 후속 Goal(G7)에서 활성화 예정. 본 plan(MVP) 로컬은 application-local.yml의
-- ddl-auto=create-drop + db/init/01-create-schema.sql 로 처리하지만, 운영 마이그레이션
-- 활성화 시 첫 마이그레이션으로 본 파일을 사용한다.
CREATE SCHEMA IF NOT EXISTS admin;
