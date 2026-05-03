package com.ljj.user_center.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljj.user_center.exception.BusinessException;
import com.ljj.user_center.mapper.UserMapper;
import com.ljj.user_center.model.domain.User;
import com.ljj.user_center.service.UserService;
import com.ljj.user_center.utils.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.message.ThreadInformation;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
预热缓存，使用定时任务，每天凌晨2点执行一次，将推荐用户列表缓存到redis中，提高用户访问速度


 */
@Slf4j
@Component
public class PreCacheJob {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;
    @Autowired
    private RedissonClient  redissonClient;
    //重点用户列表,不能写死
    private List<Long> userList = Arrays.asList(1L,2L,3L,4L,5L,6L,7L,8L,9L,10L);
    //表示在每月的1日的凌晨2点调整任务
    @Scheduled(cron = "0 0 2 * * ?")//每天凌晨2点执行一次
    public void preCache() {
        //使用redisson的分布式锁，具有看门狗机制
        RLock lock = redissonClient.getLock("yupao:recommend:users:precachejob");//锁的名字肯定是唯一的
        try {
            if(lock.tryLock(0,-1, TimeUnit.SECONDS)){//尝试获取锁，等待时间为0，过期时间-1，启动看门狗的延期机制
                System.out.println("获取锁成功:"+lock.getName()+"\t"+ Thread.currentThread().getId());
                Page<User> userPage = null;
                //从redis中获取推荐用户列表,没有则创建，有则获取
                ValueOperations<String, Object> sObValueOperations = redisTemplate.opsForValue();//操作redis中的值

                //获取当前登录用户
                for (Long userId : userList) {
//                    redissonClient.getMap()
                    String userRedisKey = String.format("yupao:recommend:users:%s", userId);
                    //从数据库中获取推荐用户列表
                    userPage = userService.page(new Page<>(1,400), new QueryWrapper<User>().select("id","username","avatarURL","userRole","userAccount","gender","phone","email","tags"));//为空则查询所有
                    if(userPage.getRecords().isEmpty()){
                        throw new BusinessException(ErrorCode.NULL_ERROR,"无法获取推荐用户列表");
                    }
                    try{
//                        sObValueOperations.get(userRedisKey);
                        RMap<Object, Object> map = redissonClient.getMap(userRedisKey);

                        sObValueOperations.set(userRedisKey,userPage,10, TimeUnit.SECONDS);//设置缓存，10秒过期时间
                    }catch (Exception e){
                        log.error("redis存储推荐用户列表失败",e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("获取锁失败",e);
            throw new RuntimeException(e);
        }finally {//finally中释放锁，防止死锁
            //只能释放自己的锁
            if(lock.isHeldByCurrentThread()){//当前线程持有锁才释放
                lock.unlock();
                System.out.println("释放锁成功:"+lock.getName()+"\t"+ Thread.currentThread().getId());
            }
        }
    }
}
