package com.loan.decision.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditScoreResult {

    /** Credit score in range 300–850 */
    private int score;

    private double paymentHistoryScore;
    private double currentDebtScore;
    private double creditAgeScore;
    private double creditMixScore;
    private double newCreditScore;
    private LocalDateTime calculatedAt;
}
