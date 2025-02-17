package com.ljj.user_center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.ljj.user_center.exception.BusinessException;
import com.ljj.user_center.mapper.TeamMapper;
import com.ljj.user_center.model.domain.Team;
import com.ljj.user_center.model.domain.User;
import com.ljj.user_center.model.domain.dto.TeamQuery;
import com.ljj.user_center.model.domain.enums.TeamState;
import com.ljj.user_center.model.domain.request.TeamJoinRequest;
import com.ljj.user_center.model.domain.request.TeamQuitRequest;
import com.ljj.user_center.model.domain.request.TeamUpdateRequest;
import com.ljj.user_center.model.domain.vo.TeamUserVO;
import com.ljj.user_center.model.domain.vo.UserVO;
import com.ljj.user_center.service.TeamService;
import com.ljj.user_center.service.UserService;
import com.ljj.user_center.service.UserTeamService;
import com.ljj.user_center.utils.ErrorCode;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ljj.user_center.model.domain.UserTeam;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
* @author DELL
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2025-01-15 11:04:03
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private UserTeamService userTeamService;
    @Autowired
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)// 事务注解，若一个操作执行失败，则抛出异常，事务回滚
    public long addTeam(Team team, User loginUser) {
        final long userId = loginUser.getId();
//        1. 请求参数是否为空？
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍参数不能为空");
        }
//        2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN,"请先登录");
        }
//        3. 校验信息
//                - 队伍人数 > 1 且 <= 20
        if(team.getMaxNum() < 1 || team.getMaxNum() > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数必须大于1且小于等于20");
        }
//                - 队伍标题 <= 20
        if(StringUtils.isBlank(team.getName()) || team.getName().length() > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍标题不能为空且长度不能超过20");
        }
//                - 描述 <= 512
        if(StringUtils.isNotBlank(team.getDescription()) && team.getDescription().length() > 512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述长度不能超过512");
        }
//
//                - status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamState teamState = TeamState.valueOf(status);
        if(teamState == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态不正确");
        }
//        - 如果 status 是加密状态，一定要有密码，且密码 <= 32
        if(TeamState.SECRET.equals(teamState)) {
            if (StringUtils.isBlank(team.getPassword()) || team.getPassword().length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密状态下，密码不能为空且长度不能超过32");
            }
        }
//                - 超时时间 > 当前时间
        if(new Date().after(team.getExpireTime())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍超时时间不能小于当前时间");
        }
//                - 校验用户最多创建 5 个队伍
        //TODO: 可能同时创建非常多个队伍，需要加锁限制或者在方法名前加@synchronized来禁止并发
        long count = this.count(new QueryWrapper<Team>().eq("userId", userId));
            if(count >= 5){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"非VIP用户的加入和创建总和不能超过5个");
            }
        //        4. 插入队伍信息到队伍表
        team.setUserId(userId);
        boolean save = this.save(team);
        Long id = team.getId();
        if(!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"队伍信息保存失败");
        }
//        4. 插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(id);
        userTeam.setUserId(userId);
        userTeam.setJoinTime(new Date());
        boolean saveUserTeam = userTeamService.save(userTeam);

        if(!saveUserTeam){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"用户-队伍关系保存失败");
        }
        return id;
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if(teamQuery != null){
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            //因为上面是拿的鱼皮的vo,所以这里需要添加
            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in("id", idList);//in查询,查询id在idList中的数据
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);//模糊查询
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);//模糊查询
            }
            Integer maxNum = teamQuery.getMaxNum();
            // 查询最大人数相等的
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            Long userId = teamQuery.getUserId();
            // 根据创建人来查询
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            // 根据状态来查询,为空则默认查询公开队伍
            Integer status = teamQuery.getStatus();
            TeamState statusEnum = TeamState.valueOf(status);
            boolean hasStatus = statusEnum != null;
            if (statusEnum == null) {
                statusEnum = TeamState.PUBLIC;
            }
            // 只有管理员才能查看私有队伍
            if (!isAdmin && statusEnum.equals(TeamState.PRIVATE)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            if(hasStatus)
            queryWrapper.eq("status", statusEnum.getValue());
        }

        // 不展示已过期的队伍,gt是greater than 的意思
        // expireTime is null or expireTime > now()
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        //进行全条件查询
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();//没有满足条件的队伍
        }

        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        // 关联查询创建人的用户信息--java代码形式，creatuser信息的建立
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            // 脱敏用户信息
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }//对每个队伍统计加入人数
        for (TeamUserVO teamUserVO : teamUserVOList) {
            teamUserVO.setHasJoinNum(countTeamUserByTeamId(teamUserVO.getId()));
        }
        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest team, User currentLoginUser, boolean isAdmin) {
        if(team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍参数不能为空");
        }
        //判断team的id是否为空
        Long id = team.getId();
        if(id == null || id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍id不能为空");
        }
        //判断当前team是否在数据库中存在
        Team teamInDb = this.getById(id);
        if(teamInDb == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        //判断当前登录用户是否有权限修改当前队伍,只有创建者和管理员可与
        if(!teamInDb.getUserId().equals(currentLoginUser.getId())  && !isAdmin){
            throw new BusinessException(ErrorCode.NO_AUTH,"没有权限修改队伍");
        }
        //TODO 如果用户传来的值和旧值一样，则不更新

         //如果队伍状态改为加密，必须要有密码
        if(TeamState.SECRET.equals(TeamState.valueOf(team.getStatus()))) {
            //判断密码是否为空
            if (StringUtils.isBlank(team.getPassword()) || team.getPassword().length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密队伍状态下，密码不能为空且长度不能超过32");
            }
        }
        Team team1 = new Team();
        BeanUtils.copyProperties(team, team1);
        return this.updateById(team1);
    }

    /**
     * 加入队伍的方法
     * @param teamJoinRequest
     * @param currentLoginUser
     * @return
     */
    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User currentLoginUser) {
//        2. 队伍必须存在，只能加入未满、未过期的队伍
        Long teamId = teamJoinRequest.getTeamId();
        if(teamId == null || teamId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍id不能为空");
        }
        //判断当前team是否在数据库中存在
        Team team = this.getById(teamId);
        if(team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        Date expireTime = team.getExpireTime();
        if(expireTime != null && team.getExpireTime().before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍已过期");
        }
        //        3. 不能加入自己的队伍，不能重复加入已加入的队伍（幂等性）
        if(team.getUserId() == currentLoginUser.getId()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"不能加入自己的队伍");
        }
        //        4. 禁止加入私有的队伍
        TeamState teamState = TeamState.valueOf(team.getStatus());
        if(TeamState.PRIVATE.equals(teamState)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"不能加入私有的队伍");
        }

//        5. 如果加入的队伍是加密的，必须密码匹配才可以
        if(TeamState.SECRET.equals(teamState)){
            if(!team.getPassword().equals(teamJoinRequest.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
            }
        }
        //建立分布式锁，一个队伍一次只能有一个人加入
        RLock lock = redissonClient.getLock("yupao:join:team"+teamId);
        try {
            while(true){
                if(lock.tryLock()){//尝试获取锁
                    System.out.println("获取锁成功:"+lock.getName()+"\t"+ Thread.currentThread().getId());
                    //        1. 用户最多加入 5 个队伍
                    long count = userTeamService.count(new QueryWrapper<UserTeam>().eq("userId", currentLoginUser.getId()));
                    if(count >= 5){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR,"非VIP用户的加入和创建总和不能超过5个");
                    }
                    //判断当前登录用户是否有权限加入当前队伍,只有创建者和管理员可与
                    long teamCount = countTeamUserByTeamId(teamId);
                    if(teamCount >= team.getMaxNum()){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数已满");
                    }
                    if(userTeamService.count(new QueryWrapper<UserTeam>().eq("teamId", teamId).eq("userId", currentLoginUser.getId())) > 0){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR,"不能重复加入已加入的队伍");
                    }
//        6. 新增队伍 - 用户关联信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setTeamId(teamId);
                    userTeam.setUserId(currentLoginUser.getId());
                    userTeam.setJoinTime(new Date());
                    boolean save = userTeamService.save(userTeam);
                    if(!save){
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR,"用户-队伍关系保存失败");
                    }
//        7. 队伍人数+1
                    return save;
                }
            }
        }catch (Exception e) {
            throw e;
        }finally {
            if(lock.isHeldByCurrentThread()){
                lock.unlock();//释放锁
                System.out.println("释放锁成功:"+lock.getName()+"\t"+ Thread.currentThread().getId());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍退出参数不能为空");
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeam(teamId);//判断队伍是否存在的封装方法
        long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setTeamId(teamId);
        queryUserTeam.setUserId(userId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);//`队伍当前人数`
        //队伍只剩下一个人，解散
        if (teamHasJoinNum == 1) {
            //删除队伍
            this.removeById(teamId);
        } else {
            //队伍至少还剩下两人
            //是队长
            if (team.getUserId() == userId) {
                //把队伍转移给最早加入的用户
                //1.查询已加入队伍的所有用户和加入时间
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍人数不足2人,不能转移队长");
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                //更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍队长失败");
                }
            }
        }
        //移除关系
        return userTeamService.remove(queryWrapper);//进行删除操作，删除对应的一行数据user_team
    }

    @Override
    @Transactional(rollbackFor = Exception.class)//事务注解，若一个操作执行失败，则抛出异常，事务回滚
    public Boolean deleteTeam(Long teamId, User loginUser) {
        Team team = getTeam(teamId);
        if (!team.getUserId().equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH,"无访问权限");
        }
        // 移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除队伍关联信息失败");
        }
        return this.removeById(teamId);
    }

    private Team getTeam(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍id不能为空");
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }

    /**
     * 利用关系表的teamid获取某队伍当前人数
     *
     * @param teamId
     * @return
     */
    private long countTeamUserByTeamId(long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }
}







