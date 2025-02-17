package com.ljj.user_center.config;

import lombok.Data;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.api.RedissonRxClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.redisson.Redisson;
import org.redisson.api.RedissonReactiveClient;
import java.io.File;
import java.io.IOException;


/**
 * @Author ljj
 * @Date 2019/11/25 10:52
 * Redisson配置类
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "spring.redis")//读取配置文件中的redis配置,这样改配置文件就可以动态的修改redis配置
public class RedissonConfig {
    private String host;

    private String port;//从配置中读取，属性名与配置文件中的redis.port相同

    @Bean
    public RedissonClient redissonClient() {
        //1.创建配置
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);//
        //2.设置地址，单个集群
        config.useSingleServer().setAddress(redisAddress).setDatabase(1);//与缓存隔开使用第1个库
//        config.useClusterServers()这个是集群模式(分布式)，需要指定多个redis节点地址，但因此次只用一个redis节点，所以注释掉了
//                // use "rediss://" for SSL connection
//                .addNodeAddress("redis://127.0.0.1:7181");，
        //3.创建RedissonClient实例
        // Sync and Async API支持异步操作，Reactive API支持响应式编程
        RedissonClient redisson = Redisson.create(config);
        // Reactive API
//        RedissonReactiveClient redissonReactive = redisson.reactive();
        // RxJava3 API
//        RedissonRxClient redissonRx = redisson.rxJava();
// or read config from file
//        config = Config.fromYAML(new File("config-file.yaml"));
        return redisson;
    }


}
