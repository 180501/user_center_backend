package com.ljj.user_center;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@MapperScan("com.ljj.user_center.mapper")//将mapper下的文件注入到spring容器中
@EnableScheduling//开启定时任务
public class UserCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserCenterApplication.class, args);
    }

}
