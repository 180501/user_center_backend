package com.ljj.user_center.utils;

import lombok.Data;

@Data
public class PageRequest implements java.io.Serializable {//Serializable接口是Java提供的序列化接口，用于在网络传输或存储过程中对Java对象进行序列化和反序列化。
    private static final long serialVersionUID = 100003L;
    protected int pageNum = 1;
    protected int pageSize = 10;
}
