package com.inox.x45.repository;

import com.inox.x45.domain.UserRoleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleAssignmentRepository extends JpaRepository<UserRoleAssignment, Long> {
    List<UserRoleAssignment> findByUserId(Long userId);
    List<UserRoleAssignment> findByRoleId(Long roleId);
    void deleteByUserId(Long userId);
}
