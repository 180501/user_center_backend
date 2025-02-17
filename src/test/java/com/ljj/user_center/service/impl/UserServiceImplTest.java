package com.ljj.user_center.service.impl;

import com.ljj.user_center.model.domain.User;
import com.ljj.user_center.service.UserService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;//
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.sun.javafx.font.FontResource.SALT;

@SpringBootTest
public class UserServiceImplTest {
    @Autowired
    private UserService userService;
    //规划线程池
    //核心线程数 (60)：线程池至少会保持60个线程活动。
    //最大线程数 (1000)：线程池最多可以创建1000个线程。
    //线程空闲时间 (10000分钟)：多余的线程在10000分钟内没有任务执行会被终止。
    //任务队列：任务队列的最大容量是10000个任务。
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(60, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    @Test
    void testSearchUserByTags() {
        List<String> list = Arrays.asList("Java","Python");
        System.out.println("标签查询测试开始");
//        userService.printUserTags();
        List<User> result = userService.searchUserByTags(list);
        Assert.assertNotNull(result);
        System.out.println(result.size());
        result.forEach(System.out::println);
    }
    @Test
    public void insert() {
        StopWatch stopWatch = new StopWatch();//计时器
        stopWatch.start("insert fake user");//开始计时
        System.out.println("开始插入数据给我好好地看这");
        String md5Password = DigestUtils.md5DigestAsHex((SALT + "123456").getBytes());
        final long number = 10000l;
        final int futureSize = 10;//异步任务数量
        long j = 0;
        List<CompletableFuture> futureList = new ArrayList<>();//异步任务列表
        int count = (int) (number/futureSize);
        for (long i = 1; i <= futureSize; i++) {
            List<User> users = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();
                user.setUserAccount("ljjnzd");
                user.setUsername("飞起来");
                user.setAvatarUrl("https://q3.itc.cn/q_70/images03/20241129/d2eb9f78bba346ed9db46f15889985ce.jpeg");
                user.setGender(0);
                user.setUserPassword(md5Password);
                user.setPhone("18993597545");
                user.setEmail("ljjnzd@163.com");
                user.setUserStatus(0);
                user.setIsDelete(0);
                user.setCreateTime(new Date());
                user.setUpdateTime(new Date());
                user.setUserRole(0);
                user.setPlanetCode("666");
                user.setTags("[\"李佳俊的小迷妹\"]");
                users.add(user);
                if(j%count == 0){
                    break;//每10000条数据插入一次
                }
            }
            CompletableFuture future = CompletableFuture.runAsync(() -> {
                System.out.println("ThreadID:"+Thread.currentThread().getId());
                userService.saveBatch(users, count);
            }, threadPoolExecutor);//可以用默认的线程池，也可以用自定义的线程池
            futureList.add(future);
        }

        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();//等待所有异步任务完成
//        userService.saveBatch(users, 1000);
        stopWatch.stop();
        System.out.println("插入数据完成，耗时：" + stopWatch.prettyPrint());
    }


}