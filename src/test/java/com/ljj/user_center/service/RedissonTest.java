package com.ljj.user_center.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljj.user_center.exception.BusinessException;
import com.ljj.user_center.model.domain.User;
import com.ljj.user_center.utils.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
public class RedissonTest {
    private List<Long> userList = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void testRedisson() {
        //list map set 等数据结构的操作仿制redisson的数据结构
        //list存在于JVM中，但Rlist存在于Redis中，可以实现分布式的list操作
        RedissonClient redisson = Redisson.create();
        RList<Object> myList = redissonClient.getList("mylist");
        myList.add("ljj");
        System.out.println("mylist:" + myList.get(0));
        myList.remove(0);

        // map
        Map<String, Object> map = new HashMap<>();
        map.put("yupi", 10);
        map.get("yupi");

        RMap<String, Object> map1 = redissonClient.getMap("test-map");

        // set

        // stack

    }

    @Test
    public void testRedisson2() {
        RLock lock = redissonClient.getLock("yupao:recommend:users:precachejob");//锁的名字肯定是唯一的
        try {
            if(lock.tryLock(0,-1, TimeUnit.SECONDS)){//尝试获取锁，等待时间为0，过期时间10秒，上锁成功则执行
                System.out.println("获取锁成功:"+lock.getName()+"\t"+ Thread.currentThread().getId());
                Thread.sleep(10000000);//延长线程运行时间来检查看门狗的延期机制
                //从redis中获取推荐用户列表,没有则创建，有则获取
            }
        } catch (InterruptedException e) {
            log.error("获取锁失败",e);
            throw new RuntimeException(e);
        }finally {//finally中释放锁，防止死锁
            //只能释放自己的锁
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
                System.out.println("释放锁成功:"+lock.getName()+"\t"+ Thread.currentThread().getId());
            }
        }
    }
}