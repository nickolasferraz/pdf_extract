package com.example.pdf_extratct.loginpage.jobs.creation;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JobCreationFactory {

    private final Map<JobCreationType, JobCreationStrategy> strategies;

    public JobCreationFactory(List<JobCreationStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        JobCreationStrategy::getType,
                        Function.identity()
                ));
    }

    public JobCreationStrategy getStrategy(JobCreationType type) {
        JobCreationStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy não encontrada para criação de job: " + type);
        }
        return strategy;
    }
}
