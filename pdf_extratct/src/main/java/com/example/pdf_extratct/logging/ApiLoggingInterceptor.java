package com.example.pdf_extratct.logging;

import com.example.pdf_extratct.loginpage.user.UserEntity;
import com.example.pdf_extratct.loginpage.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApiLoggingInterceptor implements HandlerInterceptor {

    private final ApiLogRepository apiLogRepository;
    private final UserRepository userRepository; // Para buscar o UserEntity

    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute(START_TIME_ATTRIBUTE, Instant.now().toEpochMilli());
        ApiLogContext.clear(); // Limpa o contexto no início da requisição
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // Não fazemos nada aqui, pois o status code e o tempo final só estarão disponíveis no afterCompletion
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        long endTime = Instant.now().toEpochMilli();
        long responseTimeMs = endTime - startTime;

        ApiLogEntity apiLog = new ApiLogEntity();
        apiLog.setEndpoint(request.getRequestURI());
        apiLog.setMethod(request.getMethod());
        apiLog.setStatusCode(response.getStatus());
        apiLog.setResponseTimeMs(responseTimeMs);
        apiLog.setIpAddress(request.getRemoteAddr());

        // Tentar obter o usuário autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserEntity) {
            UserEntity user = (UserEntity) authentication.getPrincipal();
            // O UserEntity do SecurityContext pode ser um proxy ou não estar totalmente carregado.
            // É mais seguro buscar uma instância gerenciada pelo JPA.
            Optional<UserEntity> managedUser = userRepository.findById(user.getUserId());
            managedUser.ifPresent(apiLog::setUser);
        } else if (authentication != null && authentication.getPrincipal() instanceof String && !authentication.getPrincipal().equals("anonymousUser")) {
            // Caso o principal seja apenas o email ou ID do usuário (se não for um UserEntity completo)
            // Você pode tentar buscar o usuário pelo email ou ID aqui, se for o caso.
            // Por exemplo, se o principal for o email:
            // String email = (String) authentication.getPrincipal();
            // userRepository.findByEmail(email).ifPresent(apiLog::setUser);
        }

        // Recupera os créditos deduzidos do contexto
        Integer creditsDeducted = ApiLogContext.getCreditsDeducted();
        if (creditsDeducted != null) {
            apiLog.setCreditsDeducted(creditsDeducted);
        } else {
            apiLog.setCreditsDeducted(0); // Garante que seja 0 se não foi definido
        }

        apiLogRepository.save(apiLog);
        ApiLogContext.clear(); // Limpa o ThreadLocal após salvar o log
    }
}
