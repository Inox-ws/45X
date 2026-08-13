package com.inox.x45.domain;

import com.inox.x45.domain.support.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One of the five fixed roles from Section 4 (FINANCE, LOGISTICS, PRODUCTION, MANAGEMENT, ADMIN). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "app_role", uniqueConstraints = @UniqueConstraint(name = "uk_app_role_name", columnNames = "name"))
public class AppRole extends BaseEntity {

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "description", length = 255)
    private String description;
}
