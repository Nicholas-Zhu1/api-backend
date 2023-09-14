package com.zhu.userapi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;


@SpringBootTest
class RedisTest {


    @Autowired
    private RedisTemplate redisTemplate;
    @Test
    void contextLoads() {
        ValueOperations<String,Object> valueOperations= redisTemplate.opsForValue();
        valueOperations.set("zhu","dong",10000, TimeUnit.MILLISECONDS);
        valueOperations.set("zhu1","dong");
        valueOperations.set("zhu2","dong");
    }

}

