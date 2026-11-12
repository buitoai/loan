package com.loan.decision.repository;

import com.loan.decision.model.entity.LoanApplication;
import com.loan.decision.model.entity.LoanStatus;
import com.loan.decision.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    // Find all applications belonging to a specific customer
    List<LoanApplication> findByCustomer(User customer);

    // Find all applications belonging to a specific customer (paginated)
    Page<LoanApplication> findByCustomer(User customer, Pageable pageable);

    // Find all applications with a given status
    List<LoanApplication> findByStatus(LoanStatus status);

    // Find all applications with a given id number (exact match)
    List<LoanApplication> findByIdNumber(String idNumber);

    // Search by applicant name (case-insensitive, partial match)
    List<LoanApplication> findByApplicantNameContainingIgnoreCase(String applicantName);

    // Search by id number (partial match)
    List<LoanApplication> findByIdNumberContaining(String idNumber);

    // Check if any application with the given id number is in one of the specified statuses
    boolean existsByIdNumberAndStatusIn(String idNumber, List<LoanStatus> statuses);

    /**
     * Paginated query with optional filters:
     *  - status: filter by loan status (null = all statuses)
     *  - dateFrom / dateTo: filter by submittedAt range (null = no bound)
     *  - amountMin / amountMax: filter by loanAmount range (null = no bound)
     */
    @Query("SELECT a FROM LoanApplication a WHERE " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:dateFrom IS NULL OR a.submittedAt >= :dateFrom) AND " +
           "(:dateTo IS NULL OR a.submittedAt <= :dateTo) AND " +
           "(:amountMin IS NULL OR a.loanAmount >= :amountMin) AND " +
           "(:amountMax IS NULL OR a.loanAmount <= :amountMax)")
    Page<LoanApplication> findWithFilters(
            @Param("status") LoanStatus status,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("amountMin") BigDecimal amountMin,
            @Param("amountMax") BigDecimal amountMax,
            Pageable pageable);
}
