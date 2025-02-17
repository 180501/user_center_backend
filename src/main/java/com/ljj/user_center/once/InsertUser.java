package com.ljj.user_center.once;
import java.util.Date;

import com.ljj.user_center.mapper.UserMapper;
import com.ljj.user_center.model.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StopWatch;

import static com.sun.javafx.font.FontResource.SALT;

@Component//给这个类加上注解，使其成为Spring的Bean
public class InsertUser {
    @Autowired
    private UserMapper userMapper;
//@Scheduled(initialDelay = 1000, fixedRate = Long.MAX_VALUE)//运行后1s后执行，间隔无限长再次执行，相当于只执行一次
    public void insert() {
    StopWatch stopWatch = new StopWatch();//计时器
        stopWatch.start("insert");//开始计时
    System.out.println("开始插入数据dsadsadsadasdasdsadsa");
        String md5Password = DigestUtils.md5DigestAsHex((SALT + "1879gh576").getBytes());
        final long number = 10000l;
        for (long i = 1; i <= number; i++) {
            User user = new User();
            user.setUserAccount("ljjnzd");
            user.setUsername("");
            user.setAvatarUrl("");
            user.setGender(0);
            user.setUserPassword(md5Password);
            user.setPhone("");
            user.setEmail("");
            user.setUserStatus(0);
            user.setIsDelete(0);
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());
            user.setUserRole(0);
            user.setPlanetCode("");
            user.setTags("[\"浅草\"]");
            userMapper.insert(user);//这样插入大概一秒4到5个数据，非常慢，建议使用service.saveBatch()批量插入,见test类
        }
        stopWatch.stop();
        System.out.println("插入数据完成，耗时：" + stopWatch.prettyPrint());
    }
}
