package com.ljj.user_center.service;
import java.util.Date;

import com.ljj.user_center.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**用户服务测试
 * @author：李佳俊
 *
 *
 */
@SpringBootTest
class UserServiceTest {
    @Autowired
    private UserService userService;
    @Test
    void testInsertUser(){
        User user = new User();
        user.setUserAccount("123546");
        user.setUsername("ljj");
        user.setAvatarUrl("43srtctyktyd");
        user.setGender(0);
        user.setUserPassword("vhbbm");
        user.setPhone("2345");
        user.setEmail("34萨4");
        user.setUserStatus(0);
        user.setIsDelete(0);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        boolean save = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(save);
    }

    @Test
    void userRegister() {
      //输入用户名长度不足4位
        String userAccount = "ljj";
        String password = "1879gh576";
        String checkPassword = "1879gh576";
        String planetCode = "1";
        Assertions.assertEquals(-1,userService.userRegister(userAccount,password,checkPassword,planetCode));
      //输入密码长度不足8位
          userAccount = "ljjnb";
          password = "123";
          checkPassword = "123";
          Assertions.assertEquals(-1,userService.userRegister(userAccount,password,checkPassword,planetCode));
          //其中一个为空
        userAccount = "";
        password = "1879gh576";
        checkPassword = "1879gh576";
        Assertions.assertEquals(-1,userService.userRegister(userAccount,password,checkPassword,planetCode));
        //两次密码不一致
        userAccount = "ljjnb";
        checkPassword = "1879gh576sad";
        Assertions.assertEquals(-1,userService.userRegister(userAccount,password,checkPassword,planetCode));
        //存在特殊字符
        userAccount = "ljj !";
        Assertions.assertEquals(-1,userService.userRegister(userAccount,password,checkPassword,planetCode));
        //表中已存在账号
        userAccount = "123456";
        Assertions.assertEquals(-1,userService.userRegister(userAccount,password,checkPassword,planetCode));
        //成功注册样例
        userAccount = "ljjnb3";
        checkPassword = "1879gh576";

        long l = userService.userRegister(userAccount, password, checkPassword, planetCode);
        Assertions.assertTrue(l > 0);
    }
}