package com.ljj.user_center.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户表
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 用户
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 登录账号
     */
    private String userAccount;

    /**
     * 用户名称
     */
    private String username;

    /**
     * 用户头像

     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 账号密码
     */
    private String userPassword;

    /**
     * 手机号

     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 0表示正常，1表示异常
     */
    private Integer userStatus;

    /**
     * 是否删除,0表示否
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 0普通，1管理员
     */
    private Integer userRole;

    /**
     * 特定编号
     */
    private String planetCode;

    /**
     * 标签数组，以json存储
     */
    private String tags;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}