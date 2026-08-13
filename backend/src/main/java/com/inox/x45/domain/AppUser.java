package com.inox.x45.domain;

import com.inox.x45.domain.support.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "app_user", uniqueConstraints = @UniqueConstraint(name = "uk_app_user_email", columnNames = "email"))
public class AppUser extends BaseEntity {

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    /**
     * Null for locally-seeded users until they sign in via Entra ID at least
     * once. Uniqueness (when set) is enforced by a filtered index in the
     * migration, not a table-level unique constraint - see V1__init_schema.sql.
     */
    @Column(name = "entra_object_id", length = 100)
    private String entraObjectId;

    /** Local-dev auth fallback only (Section 3). Never set when Entra ID is the auth source. */
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
