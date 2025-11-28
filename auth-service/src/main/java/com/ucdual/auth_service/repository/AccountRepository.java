package com.ucdual.auth_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ucdual.auth_service.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
}