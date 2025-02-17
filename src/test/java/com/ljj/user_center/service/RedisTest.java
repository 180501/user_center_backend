package com.ljj.user_center.service;
import java.util.Date;

import com.ljj.user_center.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
public class RedisTest {
    @Autowired
    private RedisTemplate redisTemplate;//redisTemplate可以操作redis数据库比如
    @Autowired
    private StringRedisTemplate stringRedisTemplate;//stringRedisTemplate可以操作redis数据库中的字符串类型数据

    @Test
    public void testRedis() {
//        RedisUtils.set("test", "testValue");//redis可以建立有时限的值，比如10秒，1分钟，1小时，1天，1月，1年，也可以设置过期时间，过期后redis会自动删除该值
        //增
        User user = new User();
        user.setId(0L);
        user.setUserAccount("ljjzdhd");
        user.setUsername("ljjzdhd");
        ValueOperations valueOperations = redisTemplate.opsForValue();//valueOperations可以操作redis数据库中的值
        ListOperations listOperations = redisTemplate.opsForList();//listOperations可以操作redis数据库中的列表
//        valueOperations.set("ljj", "testValue");
//        valueOperations.set("ljjnzd", 18);
//        valueOperations.set("ljjnzzdd", 18.5);
//        valueOperations.set("ljjzdhd", user);
//        valueOperations.set("ljjzdfcd", "testValue");
//        //查
//        Object ljj = valueOperations.get("ljj");
//        Assertions.assertEquals("testValue",ljj);
//        ljj = valueOperations.get("ljjnzd");
//        Assertions.assertEquals(18, ljj);
//        ljj = valueOperations.get("ljjnzzdd");
//        Assertions.assertEquals(18.5, ljj);
//        ljj = valueOperations.get("ljjzdhd");
//        System.out.println("你的user:" + ljj);
//        删
        redisTemplate.delete("ljj");
        redisTemplate.delete("ljjnzd");
        redisTemplate.delete("ljjnzzdd");
        redisTemplate.delete("ljjzdhd");
        redisTemplate.delete("ljjzdfcd");
//        改
//        valueOperations.set("ljj", "testValue1");

    }
}