package com.example.pdf_extratct.security.redis.quota_usage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class IpBlockServiceTest {

    @MockBean
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Autowired
    private IpBlockService ipBlockService;

    @BeforeEach
    void setUp() {
        // Garante que o mock do Redis sempre retorne o mock de ValueOperations
        lenient().when(redis.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("isBlocked deve retornar true se a chave de bloqueio existir no Redis")
    void shouldReturnTrueIfIpIsBlocked() {
        String ip = "1.2.3.4";
        when(valueOperations.get("ip:block:" + ip)).thenReturn("1");

        assertTrue(ipBlockService.isBlocked(ip));
    }

    @Test
    @DisplayName("registerAnonymousUse deve retornar false se o IP já estiver bloqueado")
    void shouldReturnFalseIfAlreadyBlocked() {
        String ip = "1.2.3.4";
        when(redis.hasKey("ip:block:" + ip)).thenReturn(true);

        assertFalse(ipBlockService.registerAnonymousUse(ip));
        verify(valueOperations, never()).increment(anyString(), anyLong());
    }

    @Test
    @DisplayName("registerAnonymousUse deve bloquear IP e retornar false se o limite for atingido")
    void shouldBlockAndReturnFalseWhenLimitExceeded() {
        String ip = "1.2.3.4";
        String counterKey = "anon:count:" + ip;
        String blockKey = "ip:block:" + ip;

        when(redis.hasKey(blockKey)).thenReturn(false);
        // Simula que o incremento retornou 3 (maior que o limite definido na classe)
        when(valueOperations.increment(counterKey, 1)).thenReturn(3L);

        assertFalse(ipBlockService.registerAnonymousUse(ip));

        verify(valueOperations).decrement(counterKey, 1);
        verify(valueOperations).set(eq(blockKey), eq("1"), any(Duration.class));
    }

    @Test
    @DisplayName("refundAnonymousUse deve decrementar o contador no Redis")
    void shouldDecrementCounterOnRefund() {
        String ip = "1.2.3.4";
        String counterKey = "anon:count:" + ip;

        when(valueOperations.decrement(counterKey, 1)).thenReturn(0L);

        ipBlockService.refundAnonymousUse(ip, 1);

        verify(redis).delete(counterKey);
    }
}
