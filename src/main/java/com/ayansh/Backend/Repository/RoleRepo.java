package com.ayansh.Backend.Repository;

import com.ayansh.Backend.Model.AppRole;
import com.ayansh.Backend.Model.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepo extends JpaRepository<Roles,Long> {
    Optional<Roles> findByRoleName(AppRole roleName);
}
