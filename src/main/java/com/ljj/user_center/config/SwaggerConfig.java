package com.ljj.user_center.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author: shayu
 * @date: 2022/11/20
 * @ClassName: yupao-backend01
 * @Description: 自定义 Swagger 接口文档的配置
 */
@Configuration // 配置类
@EnableSwagger2 // 开启 swagger2 的自动配置
@Profile("dev") // 开发环境开启 swagger
public class SwaggerConfig {
    @Bean(value = "defaultApi2")
    public Docket docket() {
        // 创建一个 swagger 的 bean 实例
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo()) // 设置基本信息
                // 配置接口信息
                .select() // 设置扫描接口
                // 配置如何扫描接口
                .apis(RequestHandlerSelectors//这玩意是选择器，用来选择那些接口
                                //.any() // 扫描全部的接口，默认
                                //.none() // 全部不扫描
                                .basePackage("com.ljj.user_center.controller") // 扫描指定包下的接口，最为常用
                        //.withClassAnnotation(RestController.class) // 扫描带有指定注解的类下所有接口
                        //.withMethodAnnotation(PostMapping.class) // 扫描带有只当注解的方法接口

                )
                .paths(PathSelectors
                                .any() // 满足条件的路径，该断言总为true
                        //.none() // 不满足条件的路径，该断言总为false（可用于生成环境屏蔽 swagger）
                        //.ant("/user/**") // 满足字符串表达式路径
                        //.regex("") // 符合正则的路径
                )
                .build();
    }
    
    // 基本信息设置
    private ApiInfo apiInfo() {
        Contact contact = new Contact(
                "ljj", // 作者姓名
                "shayuyu.cn", // 作者网址
                "shayu-yusha@qq.com"); // 作者邮箱
        return new ApiInfoBuilder()
                .title("鱼泡伙伴匹配系统-接口文档") // 标题
                .description("众里寻他千百度，慕然回首那人却在灯火阑珊处") // 描述
                .termsOfServiceUrl("https://www.baidu.com") // 跳转连接
                .version("1.0") // 版本
                .license("Swagger-的使用(详细教程)")
                .licenseUrl("https://blog.csdn.net/xhmico/article/details/125353535")
                .contact(contact)
                .build();
    }

}
