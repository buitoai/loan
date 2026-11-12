package com.loan.decision.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskDistribution {

    private long lowCount;
    private long mediumCount;
    private long highCount;
}
