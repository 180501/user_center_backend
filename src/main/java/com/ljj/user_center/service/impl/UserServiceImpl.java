package com.ljj.user_center.service.impl;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ljj.user_center.exception.BusinessException;
import com.ljj.user_center.model.domain.User;
import com.ljj.user_center.service.UserService;
import com.ljj.user_center.mapper.UserMapper;
import com.ljj.user_center.session.LoginSessionId;
import com.ljj.user_center.session.MySessionContext;
import com.ljj.user_center.utils.AlgorithmUtils;
import com.ljj.user_center.utils.ErrorCode;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.tuple.ImmutablePair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import static com.ljj.user_center.content.userConstant.ADMIN_ROLE;
import static com.ljj.user_center.content.userConstant.SESSION_LOGIN;

/**
* @author DELL
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2024-11-28 16:35:39
*/
@Slf4j  // 可以引入日志，方便调试，进入错误现场
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,User>
    implements UserService {
    @Autowired//这个注解是spring提供的，可以自动注入
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private UserMapper userMapper;
    /**
     * 密码加密盐,混淆
     */
    public final String SALT = "123456";
    /**
     * session中存储登录状态的key
     */

    /**
     * @param
     * @return userid
     * @author:ljj 用户注册
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String platCode) {
        if (StringUtils.isAllBlank(userAccount, userPassword, checkPassword, platCode)) {
            //todo 修改为自定义异常
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空"); //全为空
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号过短");//账号不能小于四位
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过长");//密码长度小于8位
        }

        if (userAccount.matches(".*[\\pP\\pS\\s].*")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号含有特殊字符");//校验账户是否含有特殊字符
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");   //两次密码不一致
        }
        //最后查看是否重复，以防资源浪费
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        if (this.count(queryWrapper) > 0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号已存在");   //用户已存在
        long count = this.count(new QueryWrapper<User>().eq("planetCode", platCode));
//        this.listObjs();
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "特定编号已存在");   //特定编号已存在
        }

        User user = new User();
        //对密码进行加密处理
        String md5Password = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        user.setUserAccount(userAccount);
        user.setUserPassword(md5Password);
        user.setPlanetCode(platCode);
        boolean save = this.save(user);

        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");   //注册失败
        }
        return user.getId();

    }

    /**
     * @param userAccount
     * @param userPassword
     * @return USER对象
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if (StringUtils.isAllBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "参数为空"); //全为空
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号过短");//账号不能小于四位
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短");//密码长度小于8位
        }

        if (userAccount.matches(".*[\\pP\\pS\\s].*")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号含有特殊字符");//校验账户是否含有特殊字符
        }
        //最后查看是否重复，以防资源浪费
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        //对密码进行加密处理
        String md5Password = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        queryWrapper.eq("userPassword", md5Password);
        User user1 = userMapper.selectOne(queryWrapper);
        if (user1 == null) {
            log.info("user not exist,can not match password");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");   //用户不存在
        }
        //用户脱敏,防止敏感信息泄露
        User safetyUser = safetyUser(user1);
        //记录用户的登录态,session可以看成是一个属性队列，可以存放用户的各种信息,attribute是session的属性(map)
        request.getSession().setAttribute(SESSION_LOGIN, safetyUser);
        int maxInactiveInterval = request.getSession().getMaxInactiveInterval();
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!最大的存活时间" + maxInactiveInterval);
        log.info("request的最大存活时间为：{}秒", maxInactiveInterval);
//        request.getSession().setMaxInactiveInterval(30*60); //设置session的最大存活时间为30分钟
        log.info("用户 {} 登录成功，会话ID为：{}", user1.getId(), request.getSession().getId());
        LoginSessionId.sessionId = request.getSession().getId();
        MySessionContext.getInstance().AddSession(request.getSession());
        return (User) request.getSession().getAttribute(SESSION_LOGIN);
    }

    /**
     * 用户重构
     *
     * @param user1
     * @return
     */
    public User safetyUser(User user1) {
        if (user1 == null)
            return null;
        User safetyUser = new User();
        safetyUser.setId(user1.getId());
        safetyUser.setUserAccount(user1.getUserAccount());
        safetyUser.setUsername(user1.getUsername());
        safetyUser.setAvatarUrl(user1.getAvatarUrl());
        safetyUser.setGender(user1.getGender());
        safetyUser.setPhone(user1.getPhone());
        safetyUser.setEmail(user1.getEmail());
        safetyUser.setUserStatus(user1.getUserStatus());
        safetyUser.setUserRole(user1.getUserRole());
        safetyUser.setCreateTime(new Date());
        safetyUser.setPlanetCode(user1.getPlanetCode());
        safetyUser.setTags(user1.getTags());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     * @return boolean
     * 0：成功，-1：失败,1：用户未登录
     */
    @Override
    public String userLogout(HttpServletRequest request) {
        //获取request请求，删除attribute属性里的session字段
        User user = (User) request.getSession().getAttribute(SESSION_LOGIN);
        if(user == null && LoginSessionId.sessionId == null)
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
       if (LoginSessionId.sessionId != null) {
            MySessionContext.getInstance().DelSession(LoginSessionId.sessionId);
            LoginSessionId.sessionId = null;
        }
        if (user != null) {
            request.getSession().removeAttribute(SESSION_LOGIN);

        }
        return "用户登出成功,context清除";
    }
    //finish

    /**
     * 根据标签搜索用户,内存写法
     * 暂时不用SQL查询
     *
     * @param tagNameLists
     * @return users条件后的
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNameLists) {
        if (tagNameLists.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签为空");
        }
        List<String> lowerCaseTagNameLists = tagNameLists.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());//转为小写
        //拼接and查询
//        long startTime = System.currentTimeMillis();
//        return userList.stream().map(this::safetyUser).collect(Collectors.toList());
        //内存查询方法
        List<User> users = userMapper.selectList(new QueryWrapper<>());
        userMapper.selectCount(null);//查询所有用户的用户数量
        Gson gson = new Gson();
        return users.stream().filter(user -> {//users.parallelStream()是并行流，可以提高查询效率，但会长期占有线程池
            String tags = user.getTags();
//                if(tags == null){
//                    return false;   tagList = Optional.ofNullable(tagList).orElse(new HashSet<>());
//                }
            Set<String> tagList = gson.fromJson(tags, new TypeToken<Set<String>>() {
            }.getType());
            tagList = Optional.ofNullable(tagList).orElse(new HashSet<>());//防止tag为空
            for (String tag : lowerCaseTagNameLists) {
                if (!tagList.contains(tag)) {
                    return false;
                }
            }
            return true;
        }).map(this::safetyUser).collect(Collectors.toList());
//        log.info("内存查询用户标签耗时：{}ms!!!!!!!!!!!!!!!!!!",System.currentTimeMillis()-startTime);
    }
//    String regex = "(\"java\",\"python\",\"tomcat\")";

    /**
     * 根据SQL查询用户标签，暂时不用
     *
     * @param tagNameLists
     * @return
     */
    @Deprecated//不推荐使用
    @Override
    public List<User> searchUserByTagsBySQL(List<String> tagNameLists) {
        if (tagNameLists.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签为空");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        List<String> lowerCaseTagNameLists = tagNameLists.stream()
//                .map(String::toLowerCase)
//                .collect(Collectors.toList());//转为小写
        //拼接and查询
        long startTime = System.currentTimeMillis();

        for (String tag : tagNameLists) {//忽略大小写
            queryWrapper = queryWrapper.like("tags", tag);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        log.info("SQL查询用户标签耗时：{}ms!!!!!!!!!!!!!!!!!!", System.currentTimeMillis() - startTime);
        return userList.stream().map(this::safetyUser).collect(Collectors.toList());
//此乃SQL语句查询
    }

    /**
     * 输出用户表每个用户的 tags 列内容
     */
    @Override
    public void printUserTags() {
        // 查询所有用户
        List<User> users = userMapper.selectList(new QueryWrapper<>());
        System.out.println("wcnmd");
        // 输出每个用户的 tags 列内容
        for (User user : users) {
            System.out.println("User ID: " + user.getId() + ", Tags: " + user.getTags());
        }
    }

    @Override
    public Boolean updateUser(User user, User loginUser, HttpServletRequest request) {
        if (user.getId() <= 0 || loginUser == null || loginUser.getId() == null)
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        if (loginUser.getId() != user.getId() && loginUser.getUserRole() != ADMIN_ROLE)
            throw new BusinessException(ErrorCode.NOT_LOGIN, "不符合SESSION记录");
        User oldUser = userMapper.selectById(loginUser.getId());
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在");
        }
        userMapper.updateById(user);
        request.getSession().setAttribute(SESSION_LOGIN, safetyUser(userMapper.selectById(oldUser.getId())));
        return true;
    }

    @Override
    public User getCurrentLoginUser(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(SESSION_LOGIN);
//        log.info("当前登录用户!!!!!!!!!!!!!!!!!!：" + user);
        if (user == null) {
            if (LoginSessionId.sessionId == null)
                throw new BusinessException(ErrorCode.NOT_LOGIN, "wcnmd");
            HttpSession session = MySessionContext.getInstance().getSession(LoginSessionId.sessionId);
            if (session != null) {
                user = (User) session.getAttribute(SESSION_LOGIN);
                log.info("这是通过session存储来登录的！！！！！！！！！！！,并非request");
            }
        }
        return user;//这个user本来就已经脱敏管理了
    }

    @Override
    public Page<User> recommendUsers(long pageNum, long pageSize, HttpServletRequest request) {
        //controller已经判断页码和页容量
        //获取当前登录用户

        User loginUser = getCurrentLoginUser(request);
        String userRedisKey = String.format("yupao:recommend:users:%s", loginUser.getId());
        //从redis中获取推荐用户列表,没有则创建，有则获取
        ValueOperations<String, Object> sObValueOperations = redisTemplate.opsForValue();
        Page<User> userPage = null;
        if (sObValueOperations.get(userRedisKey) != null) {
            userPage = (Page<User>) sObValueOperations.get(userRedisKey);
            return userPage;
        }
        //从数据库中获取推荐用户列表
        userPage = this.page(new Page<>(pageNum, pageSize), new QueryWrapper<User>().select("id", "username", "avatarURL", "userRole", "userAccount", "gender", "phone", "email", "tags"));//为空则查询所有
        if (userPage.getRecords().isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "无法获取推荐用户列表");
        }
        //将推荐用户列表存入redis
        try {
            sObValueOperations.set(userRedisKey, userPage, 10, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("redis存储推荐用户列表失败", e);
        }
        return userPage;
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        User loginUser = getCurrentLoginUser(request);
        if (loginUser != null && loginUser.getUserRole() == ADMIN_ROLE) {
            return true;
            //todo 这里可以加个权限校验，判断是否有权限访问某个接口
        }
        return false;
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.select("id","tags");
        objectQueryWrapper.isNotNull("tags");
        //不选择当前登录的用户，使用pair而不用treemap，因为treemap是根据key排序的，key必须是可比较的类型而不是对象，但其实也可以比
        objectQueryWrapper.ne("id", loginUser.getId());//objectQueryWrapper.ne是不等于，可以排除掉自己
        List<User> userList = this.list(objectQueryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());//对登录用户的tags进行字符串列表化
        System.out.println(tagList);
        // 用户列表的下表 => 相似度
        List<Pair<User, Long>> userPair = new ArrayList<>();//定义对Map的排序规则
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();//已经提前过滤标签为空值的user
            //对用户的tags进行字符串列表化
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            //计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            userPair.add(new ImmutablePair<>(user, distance));//添加的
        }
        // 排序
        Stream<Pair<User, Long>> limitUserList = userPair.stream().sorted(Comparator.comparing(Pair::getValue)).limit(num);
            // 取出前 num 个元素的ID
        List<Long> userIdListResult = limitUserList.map(Pair -> Pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> objectQueryWrapper1 = new QueryWrapper<>();
        objectQueryWrapper1.in("id", userIdListResult);//in是无顺序的写法，不能这么写
        List<User> userListResult = this.list(objectQueryWrapper1).stream().map(this::safetyUser).collect(Collectors.toList());
        //转换为键位ID值为user的Map,再次重新排序
        Map<Long, User> userMap = userListResult.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        List<User> resultList = new ArrayList<>();
        for(Long id : userIdListResult){
            resultList.add(userMap.get(id));
        }
        return resultList;
    }
}
















class example{
    void add(){}
    void delete(){
        this.add();
    }
    static void determine(){
        System.out.println("你好");
    }
    static void surve(){
        //静态方法内部只能调用静态属性和静态方法
        determine();
    }

    public static void main(String[] args) {
        example.surve();
    }
}




