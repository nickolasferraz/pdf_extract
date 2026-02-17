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

    public boolean registerAnonymousUse(String ip){
        String counterkey ="anon:count:" + ip;

        Long count= redis.opsForValue().increment(counterkey);

        if (count !=  null && count == 1L){
            redis.expire(counterkey, Duration.ofDays(ANON_TTL_DAYS));
        }

        if (count != null && count >ANON_LIMIT){
            String blockKey= "ip:block:" + ip;
            redis.opsForValue().set(blockKey,"1",Duration.ofDays(BLOCK_TLL_DAYS));
            return false;
        }

        return true;

    }

    public void refundAnonymousUse(String ip){
        String counterKey= "anon:count"+ip;
        Long val=redis.opsForValue().decrement(counterKey);

        if (val != null && val <=0){
            redis.delete(counterKey);
        }
    }




}
