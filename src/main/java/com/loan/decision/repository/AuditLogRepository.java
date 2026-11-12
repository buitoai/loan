package com.loan.decision.repository;

import com.loan.decision.model.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Find all audit logs for a specific user
    List<AuditLog> findByUser_UserId(Long userId);

    // Find all audit logs for a specific loan application
    List<AuditLog> findByApplication_ApplicationId(Long applicationId);

    // Find all audit logs by action type
    List<AuditLog> findByAction(String action);

    // Find all audit logs within a time range
    List<AuditLog> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    // Paginated: find audit logs by user
    Page<AuditLog> findByUser_UserId(Long userId, Pageable pageable);

    // Paginated: find audit logs by application
    Page<AuditLog> findByApplication_ApplicationId(Long applicationId, Pageable pageable);

    /**
     * Combined admin search with optional filters:
     *  - username: filter by user's username (null = all users)
     *  - action: filter by action type (null = all actions)
     *  - dateFrom / dateTo: filter by createdAt range (null = no bound)
     *
     * Supports requirement 11.6: admin can search audit logs by time range, action type, and username.
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:username IS NULL OR a.user.username = :username) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:dateFrom IS NULL OR a.createdAt >= :dateFrom) AND " +
           "(:dateTo IS NULL OR a.createdAt <= :dateTo)")
    Page<AuditLog> findWithFilters(
            @Param("username") String username,
            @Param("action") String action,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable);
}
