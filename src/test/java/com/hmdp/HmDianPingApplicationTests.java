package com.hmdp;

import cn.hutool.core.lang.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

@SpringBootTest
class HmDianPingApplicationTests {

    @Test
    public void testUUID() {
        // bf12bdef-0209-482a-9e18-ec27b74bf50c
        // 4ff579285d8941b999e51c3956cd96b7
        // 895c
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        System.out.println("UUID: "+uuid);
    }


    @Resource
    public StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedis(){
        stringRedisTemplate.opsForValue().set("test", "123");
        stringRedisTemplate.opsForValue().get("test");
        System.out.println("111");
    }



}
