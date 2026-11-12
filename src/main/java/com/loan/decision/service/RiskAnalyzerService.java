package com.loan.decision.service;

import com.loan.decision.model.dto.RiskAnalysisInput;
import com.loan.decision.model.entity.RiskLevel;

public interface RiskAnalyzerService {
    RiskLevel analyzeRisk(RiskAnalysisInput input);
}
