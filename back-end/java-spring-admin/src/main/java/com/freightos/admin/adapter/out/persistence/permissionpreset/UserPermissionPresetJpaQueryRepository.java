package com.freightos.admin.adapter.out.persistence.permissionpreset;

import com.freightos.admin.application.permissionpreset.projection.UserPermissionPresetRow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * user_permission_preset 과 permission_preset 을 조인하여
 * UserPermissionPresetRow 를 반환하는 JPQL 쿼리 리포지토리.
 */
@Repository
public class UserPermissionPresetJpaQueryRepository {

    @PersistenceContext
    private EntityManager em;

    public List<UserPermissionPresetRow> findRowsByUserId(Long userId) {
        String jpql = """
                SELECT new com.freightos.admin.application.permissionpreset.projection.UserPermissionPresetRow(
                    u.id, u.userId, u.presetId, p.code, p.name, p.active
                )
                FROM UserPermissionPresetJpaEntity u
                JOIN PermissionPresetJpaEntity p ON p.id = u.presetId
                WHERE u.userId = :userId
                ORDER BY u.id ASC
                """;
        return em.createQuery(jpql, UserPermissionPresetRow.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
