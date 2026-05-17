package com.freightos.admin.adapter.out.persistence.notice;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<NoticeJpaEntity, Long>, NoticeRepositoryCustom {
}
