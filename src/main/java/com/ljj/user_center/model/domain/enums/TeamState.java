package com.ljj.user_center.model.domain.enums;

import org.springframework.util.DigestUtils;

import static com.sun.javafx.font.FontResource.SALT;

public enum TeamState {
    PUBLIC(0, "公开"),
    PRIVATE(1, "私密"),
    SECRET(2, "保密"),
    ;
    private int value;
    private String desc;

    TeamState(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }


    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static TeamState valueOf(Integer value) {
        if (value == null) {
            return null;
        }
        for (TeamState state : TeamState.values()) {
            if (state.getValue() == value) {
                return state;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String userPassword = "12345678";
        String md5Password = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        System.out.println(md5Password);
    }
}
