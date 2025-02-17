package com.ljj.user_center.model.domain.request;

import lombok.Data;

@Data
public class userLoginRequest implements java.io.Serializable {//Serializable接口是Java提供的序列化接口，用于在网络传输或存储过程中对Java对象进行序列化和反序列化。
    private static final long serialVersionUID = 100002L;// serialVersionUID是long类型，用于标识类的版本号，不同版本的类，serialVersionUID值不同。
    //定义前端传入的参数
    private String userAccount, userPassword;
}
