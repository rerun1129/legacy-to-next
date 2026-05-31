-- CodeNameQueryRepositoryTest 전용 시드 데이터
-- admin 스키마는 application-test.yml create_namespaces=true + ddl-auto:create-drop 으로 자동 생성됨
-- 엔티티 실제 컬럼: customer_code, name, deleted_at (active/created_at/updated_at 없음)
-- H2 호환: NOW() 대신 CURRENT_TIMESTAMP 사용
-- admin_user 컬럼: AdminUserRefJpaEntity 선언 기준 (username/user_eng_name/email/deleted_at)

INSERT INTO admin.customer (customer_code, name)
VALUES ('CUST-A', '고객 A');

-- deleted_at 채움 → fetchCustomerNames 에서 제외 대상
INSERT INTO admin.customer (customer_code, name, deleted_at)
VALUES ('CUST-B', '고객 B (삭제됨)', CURRENT_TIMESTAMP);

INSERT INTO admin.port (port_code, name)
VALUES ('KRPUS', '부산항');

-- deleted_at 채움 → fetchPortNames 에서 제외 대상
INSERT INTO admin.port (port_code, name, deleted_at)
VALUES ('KRSEL', '서울항 (삭제됨)', CURRENT_TIMESTAMP);

INSERT INTO admin.carrier (carrier_code, name)
VALUES ('HMMD', 'HMM');

-- deleted_at 채움 → fetchCarrierNames 에서 제외 대상
INSERT INTO admin.carrier (carrier_code, name, deleted_at)
VALUES ('GONE', '삭제된 선사', CURRENT_TIMESTAMP);

-- admin_user: AdminUserRefJpaEntity에 선언된 컬럼(username/user_eng_name/email/deleted_at)만 삽입
-- H2 ddl-auto:create-drop 은 @Entity 선언 컬럼 기준으로 테이블을 생성하므로 미선언 컬럼(password_hash/role/active) 삽입 금지

INSERT INTO admin.admin_user (username, user_eng_name, email)
VALUES ('john.doe', 'John Doe', 'john.doe@example.com');

-- user_eng_name NULL → email fallback
INSERT INTO admin.admin_user (username, user_eng_name, email)
VALUES ('jane.smith', NULL, 'jane.smith@example.com');

-- deleted_at 채움 → fetchUserNames 에서 제외 대상
INSERT INTO admin.admin_user (username, user_eng_name, email, deleted_at)
VALUES ('ghost.user', 'Ghost', 'ghost@example.com', CURRENT_TIMESTAMP);

-- HsCodeRefJpaEntity 컬럼 기준: hs_code, name, deleted_at
INSERT INTO admin.hs_code (hs_code, name)
VALUES ('8471.30', '휴대용 자동자료처리기계');

-- deleted_at 채움 → fetchHsCodeNames 에서 제외 대상
INSERT INTO admin.hs_code (hs_code, name, deleted_at)
VALUES ('9999.99', '삭제된 HS코드', CURRENT_TIMESTAMP);

-- TeamRefJpaEntity 컬럼 기준: team_id, team_code, name, active (sort_order 미선언 → 삽입 금지)
-- active=true 활성 팀 2행
INSERT INTO admin.team (team_code, name, active)
VALUES ('TEAM-A', '영업팀', TRUE);

INSERT INTO admin.team (team_code, name, active)
VALUES ('TEAM-B', '운영팀', TRUE);

-- active=false 비활성 팀 → fetchTeamNames 에서 제외 대상
INSERT INTO admin.team (team_code, name, active)
VALUES ('TEAM-Z', '해산된 팀', FALSE);
