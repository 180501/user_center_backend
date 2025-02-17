package com.ljj.user_center.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljj.user_center.exception.BusinessException;
import com.ljj.user_center.model.domain.User;
import com.ljj.user_center.model.domain.request.userLoginRequest;
import com.ljj.user_center.model.domain.request.userRegisterRequest;
import com.ljj.user_center.service.UserService;
import com.ljj.user_center.session.LoginSessionId;
import com.ljj.user_center.session.MySessionContext;
import com.ljj.user_center.utils.BaseResponse;
import com.ljj.user_center.utils.ErrorCode;
import com.ljj.user_center.utils.ResultUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.ljj.user_center.content.userConstant.ADMIN_ROLE;
import static com.ljj.user_center.content.userConstant.SESSION_LOGIN;

/**
 * 用户接口
 * @author ljj
 */
@Slf4j  // 可以引入日志，方便调试，进入错误现场
@RestController //这个注解表示这是一个控制器类，所有的方法都将返回json数据，适用于RESTful风格的接口
//controller层涉及的业务逻辑越少越好
@RequestMapping("/user") //这个注解表示映射到/user前缀的url
//@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")//(origins="http://129.28.27.222","http://localhost:3000","http://192.168.35.139:3000", allowCredentials = "true") //允许跨域请求
public class UserController {

    @Autowired
    private UserService userService;
    @PostMapping("/register") //这个注解表示映射到/user/register的url
    public BaseResponse<Long> userRegister(@RequestBody userRegisterRequest userRegisterRequest) {//返回user id
        if (userRegisterRequest == null)
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAllBlank(userAccount, userPassword, checkPassword,planetCode))
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        long l = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        return ResultUtils.success(l);
    }

    /**
     * 用户登录
     * @param userLoginRequest
     * @param request
     * @return User
     */
    @PostMapping("/login") //这个注解表示映射到/user/register的url
        public BaseResponse<User> userLogin(@RequestBody userLoginRequest userLoginRequest, HttpServletRequest request) {//返回user id
        if (userLoginRequest == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAllBlank(userAccount, userPassword))
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        User user1 = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user1);
    }

    @GetMapping("/search")//查询用户
    public BaseResponse<List<User>> searchUsers(String userName,HttpServletRequest request){
        //仅管理员可查询
        if(!isAdmin(request))
            throw new BusinessException(ErrorCode.NO_AUTH);
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(userName)){
            userQueryWrapper.like("username",userName);
        }
        List<User> list = userService.list(userQueryWrapper);//为空则查询所有
        list = list.stream().map(user -> userService.safetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }
    @GetMapping("/recommend")//查询用户
    public BaseResponse<Page<User>> recommendUsers(long pageNum,long pageSize,HttpServletRequest request){
        //页码和页容量是否合适
        if(pageNum <= 0 || pageSize <= 0 || pageSize > 500){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"页码和页容量不合适");
        }
        //仅管理员可查询是不需要的
//        if(!isAdmin(request))
//            throw new BusinessException(ErrorCode.NO_AUTH);
        return ResultUtils.success(userService.recommendUsers(pageNum,pageSize,request));
//        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
//        if(StringUtils.isNotBlank(userName)){
//            userQueryWrapper.like("username",userName);
//        }
//        Page<User> list = userService.page(new Page<>(pageNum,pageSize), userQueryWrapper.select("id","username","avatarURL","userRole","userAccount","gender","phone","email","tags"));//为空则查询所有
//        list = list.stream().map(user -> userService.safetyUser(user)).collect(Collectors.toList());
//        return ResultUtils.success(list);
    }

    @PostMapping("/delete")//删除用户
    public BaseResponse<String> deleteUsers(@RequestBody long userId, HttpServletRequest request){
        if(!isAdmin(request))
            throw new BusinessException(ErrorCode.NO_AUTH);
        if(userId <= 0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(String.valueOf(userService.removeById(userId)));//此为逻辑删除，数据库依然存在
    }
    /**
     *  获取当前用户信息
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        String isContext = "用户使用登录时的Session登录";
        User user = (User) request.getSession().getAttribute(SESSION_LOGIN);
        log.info("当前用户!!!!!!!!!!!!!!!!!!：" + user);
        if (user == null) {
            if(LoginSessionId.sessionId == null)
            throw new BusinessException(ErrorCode.NOT_LOGIN,"wcnmd");
            HttpSession session = MySessionContext.getInstance().getSession(LoginSessionId.sessionId);
            if(session != null){
                isContext = "用户使用Context登录";
                user = (User) session.getAttribute(SESSION_LOGIN);
            }
        }
        //todo 当用户状态为异常时(删除)，不可以查询
        return ResultUtils.success(userService.safetyUser(user),isContext);//这个user本来就已经脱敏管理了
    }

    /**
     *  登出
     * @param request
     * @return int
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if(request == null){
//            return ResultUtils.error(userService.userLogout(request));
              throw new BusinessException(ErrorCode.PARAMS_ERROR,"无请求");
        }
        return ResultUtils.error(ErrorCode.NOT_LOGIN,userService.userLogout(request));
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tags){
        if(CollectionUtils.isEmpty(tags))
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"标签不能为空");
        return ResultUtils.success(userService.searchUserByTags(tags));

    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody User user,HttpServletRequest request){
        if(user == null || user.getId() == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");
        }
        //仅管理员可操作
        User currentLoginUser = userService.getCurrentLoginUser(request);
        if(userService.updateUser(user,currentLoginUser,request)){
            return ResultUtils.success(true);
        }

        else{
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"你输的没问题，但是更新失败");}
    }

    /**
     * 获取最匹配的用户
     *
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getCurrentLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num, user));
    }


    /**
     *是否为管理员
     * @param request
     * @return
     */
    private boolean isAdmin(HttpServletRequest request){
        //仅管理员可操作
        User user = (User) request.getSession().getAttribute(SESSION_LOGIN);
        if(user != null){
            if( user.getUserRole() != ADMIN_ROLE){
                log.info("非管理员用户，无权操作");
                return false;
            }
                return true;
        }
        if(LoginSessionId.sessionId != null){
            HttpSession session = MySessionContext.getInstance().getSession(LoginSessionId.sessionId);
            if(session != null){
                user = (User) session.getAttribute(SESSION_LOGIN);
                if(user.getUserRole() == ADMIN_ROLE){
                    return true;
                }else{
                    return false;
                }
            }
        }
        throw new BusinessException(ErrorCode.NOT_LOGIN,"校验admin权限时用户未登录");
    }


}