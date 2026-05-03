package com.ljj.user_center.controller;

import com.ljj.user_center.model.domain.User;
import com.ljj.user_center.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/new")
public class NewController {
    @Autowired
    private UserService userService;
    @PostMapping("/add")
    public String add(User user, HttpServletRequest request) {
        User currentLoginUser = userService.getCurrentLoginUser(request);
        return "add";
    }


}
