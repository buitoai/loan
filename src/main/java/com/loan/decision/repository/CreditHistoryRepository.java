package com.loan.decision.repository;

import com.loan.decision.model.entity.CreditHistory;
import com.loan.decision.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditHistoryRepository extends JpaRepository<CreditHistory, Long> {

    Optional<CreditHistory> findByCustomer(User customer);
}
