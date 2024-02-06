package org.SecondImage.reggie.common;

import cn.hutool.core.lang.UUID;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

public class DistributedLock implements ILock {

    private String name;
    private RedisTemplate redisTemplate;
    private static final String lockKEY = "lock:";
    private static final String lockID = UUID.randomUUID().toString(true) + "_"; //hutool的UUID

    public DistributedLock(String name, RedisTemplate redisTemplate) {
        this.name = name;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean trylock(long timeout) {
        String id = lockID + Thread.currentThread().getId();

        Boolean res = redisTemplate.opsForValue().setIfAbsent(lockKEY + name, id, timeout, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(res);
    }

    @Override
    public void unlock() {
        String lockId = (String) redisTemplate.opsForValue().get(lockKEY + name);
        String id = lockID + Thread.currentThread().getId();
        if (id.equals(lockId)) {
            // 2.删除锁
            redisTemplate.delete(lockKEY + name);
        }
    }
}
