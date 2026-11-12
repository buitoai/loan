package com.loan.decision.service;

import com.loan.decision.model.dto.RiskAnalysisInput;
import com.loan.decision.model.entity.RiskLevel;
import com.loan.decision.util.RiskCalculator;
import org.springframework.stereotype.Service;

@Service
public class RiskAnalyzerServiceImpl implements RiskAnalyzerService {

    @Override
    public RiskLevel analyzeRisk(RiskAnalysisInput input) {
        return RiskCalculator.calculateRisk(input);
    }
}
