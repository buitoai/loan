package com.loan.decision.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", unique = true)
    private User customer;

    @Builder.Default
    @Column(name = "total_loans")
    private Integer totalLoans = 0;

    @Builder.Default
    @Column(name = "active_loans")
    private Integer activeLoans = 0;

    @Builder.Default
    @Column(name = "closed_loans")
    private Integer closedLoans = 0;

    @Builder.Default
    @Column(name = "late_payments")
    private Integer latePayments = 0;

    @Builder.Default
    @Column(name = "\"defaults\"")
    private Integer defaults = 0;

    @Builder.Default
    @Column(name = "total_borrowed", precision = 18, scale = 2)
    private BigDecimal totalBorrowed = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "credit_age_months")
    private Integer creditAgeMonths = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
