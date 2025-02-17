package com.ljj.user_center.content;
/**
 * 用户用到的所有常量
 */
public interface userConstant
{
    /**
     * 用户登录态存储
     */
    String SESSION_LOGIN = "userloginState";//登录态
    //--------------权限
    //默认权限
    int DEFAULT_ROLE = 0;
    //管理员权限
    int ADMIN_ROLE = 1;
}
