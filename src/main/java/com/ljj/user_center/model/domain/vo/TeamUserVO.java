package com.ljj.user_center.model.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍和用户信息封装类（脱敏）
 *
 *注意这里的id就是队伍id，不是userteam的id
 *
 * @author yupi
 */
@Data
public class TeamUserVO implements Serializable {

    private static final long serialVersionUID = 163478861968488713L;
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建者用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人用户信息,没有其他用户成员信息？只有id列表
     */
    private UserVO createUser;

    /**
     * 已加入的用户数
     */
    private long hasJoinNum;

    /**
     * 是否已加入队伍
     */
    private boolean hasJoin = false;
}