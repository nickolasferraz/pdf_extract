package com.example.pdf_extratct.logging;

import com.example.pdf_extratct.loginpage.user.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ApiLoggingInterceptor implements HandlerInterceptor {

    private final ApiLogRepository apiLogRepository;
    // ❌ REMOVIDO: private final UserRepository userRepository;
    // Não precisa mais buscar UserEntity gerenciado pelo JPA para o log

    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute(START_TIME_ATTRIBUTE, Instant.now().toEpochMilli());
        ApiLogContext.clear();
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        long endTime = Instant.now().toEpochMilli();
        long responseTimeMs = endTime - startTime;

        // ✅ MUDOU: ApiLogEntity → ApiLogDocument
        ApiLogDocument apiLog = new ApiLogDocument();
        apiLog.setEndpoint(request.getRequestURI());
        apiLog.setMethod(request.getMethod());
        apiLog.setStatusCode(response.getStatus());
        apiLog.setResponseTimeMs(responseTimeMs);
        apiLog.setIpAddress(request.getRemoteAddr());

        // Tentar obter o usuário autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserEntity) {
            UserEntity user = (UserEntity) authentication.getPrincipal();
            // ✅ MUDOU: salva apenas userId e email (sem referência JPA)
            apiLog.setUserId(user.getUserId());
            apiLog.setUserEmail(user.getEmail());
        }

        // Recupera os créditos deduzidos do contexto
        Integer creditsDeducted = ApiLogContext.getCreditsDeducted();
        apiLog.setCreditsDeducted(creditsDeducted != null ? creditsDeducted : 0);

        apiLogRepository.save(apiLog);
        ApiLogContext.clear();
    }
}