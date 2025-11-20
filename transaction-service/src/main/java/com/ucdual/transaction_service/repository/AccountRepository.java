package com.ucdual.transaction_service.repository;

import com.ucdual.transaction_service.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
