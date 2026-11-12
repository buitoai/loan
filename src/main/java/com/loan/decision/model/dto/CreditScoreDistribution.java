package com.loan.decision.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditScoreDistribution {

    private long range300to499;
    private long range500to599;
    private long range600to699;
    private long range700to749;
    private long range750to850;
}
