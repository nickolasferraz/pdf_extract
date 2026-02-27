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

        // 1️⃣ Se já estiver bloqueado, nem tenta incrementar
        if (redis.hasKey(blockKey)) {
            return false;
        }

        String counterKey = "anon:count:" + ip;

        Long currentCount = redis.opsForValue().increment(counterKey, fileCount);

        // 2️⃣ Se for o primeiro uso, define TTL
        if (currentCount != null && currentCount == fileCount) {
            redis.expire(counterKey, Duration.ofDays(ANON_TTL_DAYS));
        }

        // 3️⃣ Se ultrapassou limite
        if (currentCount != null && currentCount > ANON_LIMIT) {

            // desfaz o incremento
            redis.opsForValue().decrement(counterKey, fileCount);

            // aplica bloqueio temporário
            redis.opsForValue().set(blockKey, "1", Duration.ofDays(BLOCK_TLL_DAYS));

            return false;
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
