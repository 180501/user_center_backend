package com.ljj.user_center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljj.user_center.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author DELL
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2024-11-28 16:35:39
*/
public interface UserService extends IService<User> {
    //初次编写用户注册业务层
    /**
     * 返回值要求为用户id
     */
    long userRegister(String userAccount,String userPassword,String checkPassword,String planetCode);
    //初次编写用户登录业务层
    /**
     * 返回值要求为用户信息
     * HttpServletRequest是用来获取客户端的请求信息的，比如ip地址，浏览器类型等。
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param user1
     * @return
     */
    User safetyUser(User user1);

    /**
     * 用户注销
     *
     * @param request
     * @return int
     */
    String userLogout(HttpServletRequest request   );
    List<User> searchUserByTags(List<String> tagNameLists);

    List<User> searchUserByTagsBySQL(List<String> tagNameLists);

    void printUserTags();
    //更新用户信息
    Boolean updateUser(User user,User loginUser,HttpServletRequest request);
    //获取当前登录用户
    User getCurrentLoginUser(HttpServletRequest request);

    Page<User> recommendUsers(long pageNum, long pageSize, HttpServletRequest request);

    boolean isAdmin(HttpServletRequest request);

    List<User> matchUsers(long num, User user);
}
