package com.freightos.admin.adapter.out.persistence.user;

import com.freightos.admin.domain.user.entity.Permission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(schema = "admin", name = "admin_user_permission")
@IdClass(UserPermissionJpaEntity.UserPermissionId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionJpaEntity {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false, length = 30, updatable = false)
    private Permission permission;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class UserPermissionId implements Serializable {
        private Long userId;
        private Permission permission;
    }
}
