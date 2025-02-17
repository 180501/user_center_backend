create table user
(
    id           bigint auto_increment comment '用户'
        primary key,
    userAccount  varchar(255)                 null comment '登录账号',
    username     varchar(255)                 null comment '用户名称',
    avatarUrl    varchar(1024)                null comment '用户头像
',
    gender       tinyint                      null comment '性别',
    userPassword varchar(512)                 not null comment '账号密码',
    phone        varchar(255)                 null comment '手机号
',
    email        varchar(512)                 null comment '邮箱',
    UserStatus   int      default 0           null comment '0表示正常，1表示异常',
    isDelete     tinyint  default 0           null comment '是否删除,0表示否',
    createTime   datetime default (curtime()) null comment '创建时间',
    updateTime   datetime default (curtime()) null comment '更新时间',
    userRole     int      default 0           not null comment '0普通，1管理员',
    planetCode   varchar(512)                 null comment '特定编号'
)
    comment '用户表';

