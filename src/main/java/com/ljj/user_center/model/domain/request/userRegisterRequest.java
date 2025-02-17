package com.ljj.user_center.model.domain.request;

import lombok.Data;

/**用户注册请求参数实体类
 * @Author: ljj
 */
@Data
public class userRegisterRequest implements java.io.Serializable {//Serializable接口是Java提供的序列化接口，用于在网络传输或存储过程中对Java对象进行序列化和反序列化，序列化为字节数组，反序列化为原对象。
    private static final long serialVersionUID = 100001L;// serialVersionUID是long类型，用于标识类的版本号，不同版本的类，serialVersionUID值不同。
    //定义前端传入的参数
    private String userAccount, userPassword, checkPassword,planetCode;
}
