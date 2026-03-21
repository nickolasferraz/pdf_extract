package com.example.pdf_extratct.loginpage.user.strategy;

import com.example.pdf_extratct.loginpage.auth.AuthProvider;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class UserCreationStrategyFactory {

    private final Map<AuthProvider, UserCreationStrategy> strategies;

    public UserCreationStrategyFactory(List<UserCreationStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        UserCreationStrategy::getProviderName,
                        Function.identity()
                ));
    }

    public UserCreationStrategy getStrategy(AuthProvider provider) {
        UserCreationStrategy strategy = strategies.get(provider);
        if (strategy == null) {
            throw new IllegalArgumentException("Provider não suportado: " + provider);
        }
        return strategy;
    }
}
