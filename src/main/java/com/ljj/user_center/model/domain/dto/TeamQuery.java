package com.ljj.user_center.model.domain.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.ljj.user_center.utils.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * @Author ljj
 *    TeamQuery
 *    2019/11/25 14:59
 *    Team分页查询封装类
 */
@EqualsAndHashCode(callSuper = true)//生成的hashCode和equals方法都继承自父类
@Data
public class TeamQuery extends PageRequest {
    /**
     * id
     */
    private Long id;

    /**
     * id 列表，包含的用户ID列表
     */
    private List<Long> idList;

    /**
     * 搜索关键词（同时对队伍名称和描述搜索）
     */
    private String searchText;

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
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

}
