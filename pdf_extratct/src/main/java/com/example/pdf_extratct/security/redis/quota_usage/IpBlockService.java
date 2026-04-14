package com.example.pdf_extratct.security.redis.quota_usage;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class IpBlockService {

    private final StringRedisTemplate redis;

    private static final int ANON_TTL_DAYS =30;
    private static final int BLOCK_TLL_DAYS=1;
    private static final int ANON_LIMIT=2;


    public IpBlockService(StringRedisTemplate redis) {
        this.redis = redis;
    }


    public boolean isBlocked(String ip) {
        String blockKey = "ip:block:" + ip;
        String val = redis.opsForValue().get(blockKey);
        return val != null;
    }
    /**
     * Registra um processamento anônimo e retorna true se permitido,
     * retorna false se atingiu limite e aplicou bloqueio.
     */
    public boolean registerAnonymousUse(String ip, int fileCount) {

        String blockKey = "ip:block:" + ip;
        String counterKey = "anon:count:" + ip;

        // 1️⃣ Já bloqueado? Rejeita
        if (redis.hasKey(blockKey)) {
            return false;
        }

        // 2️⃣ Pega o valor atual (sem incrementar ainda)
        String currentVal = redis.opsForValue().get(counterKey);
        long currentCount = currentVal != null ? Long.parseLong(currentVal) : 0L;

        // 3️⃣ Verifica se o total vai ultrapassar o limite ANTES de incrementar
        if (currentCount + fileCount > ANON_LIMIT) {
            // Aplica bloqueio sem incrementar
            redis.opsForValue().set(blockKey, "1", Duration.ofDays(BLOCK_TLL_DAYS));
            return false;
        }

        // 4️⃣ Incrementa com segurança
        Long newCount = redis.opsForValue().increment(counterKey, fileCount);

        // 5️⃣ Define TTL só na primeira vez
        if (newCount != null && newCount == fileCount) {
            redis.expire(counterKey, Duration.ofDays(ANON_TTL_DAYS));
        }

        return true;
    }


    public boolean registerAnonymousUse(String ip) {
        return registerAnonymousUse(ip, 1);
    }


    public void refundAnonymousUse(String ip, int fileCount){ // Adicionado fileCount
        String counterKey= "anon:count:" + ip; // Corrigido para usar ":"
        Long val=redis.opsForValue().decrement(counterKey, fileCount); // Decrementa pelo número de arquivos

        if (val != null && val <=0){
            redis.delete(counterKey);
        }
    }




}
