package com.example.pdf_extratct.loginpage.jobs.factory;

import com.example.pdf_extratct.loginpage.jobs.JobStatus;
import com.example.pdf_extratct.loginpage.jobs.strategy.JobStatusStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JobStatusStrategyFactory {

    private final Map<JobStatus, JobStatusStrategy> strategies;

    public JobStatusStrategyFactory(List<JobStatusStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        JobStatusStrategy::getTargetStatus,
                        Function.identity()
                ));
    }

    public JobStatusStrategy getStrategy(JobStatus status) {
        JobStatusStrategy strategy = strategies.get(status);
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy não encontrada para status: " + status);
        }
        return strategy;
    }
}
