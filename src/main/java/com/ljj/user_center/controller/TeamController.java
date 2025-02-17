package com.ljj.user_center.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljj.user_center.exception.BusinessException;
import com.ljj.user_center.model.domain.Team;
import com.ljj.user_center.model.domain.User;
import com.ljj.user_center.model.domain.UserTeam;
import com.ljj.user_center.model.domain.dto.TeamQuery;
import com.ljj.user_center.model.domain.request.TeamAddRequest;
import com.ljj.user_center.model.domain.request.TeamJoinRequest;
import com.ljj.user_center.model.domain.request.TeamQuitRequest;
import com.ljj.user_center.model.domain.request.TeamUpdateRequest;
import com.ljj.user_center.model.domain.vo.TeamUserVO;
import com.ljj.user_center.service.TeamService;
import com.ljj.user_center.service.UserService;
import com.ljj.user_center.service.UserTeamService;
import com.ljj.user_center.utils.BaseResponse;
import com.ljj.user_center.utils.ErrorCode;
import com.ljj.user_center.utils.ResultUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/team")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")//(origins="http://129.28.27.222","http://localhost:3000","http://192.168.35.139:3000", allowCredentials = "true") //允许跨域请求
public class TeamController {
    @Autowired
    private TeamService teamService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserTeamService userTeamService;


    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamFilter, HttpServletRequest request) {
        if (teamFilter == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "team is null");
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamFilter, team);
        long teamId = teamService.addTeam(team, userService.getCurrentLoginUser(request));
        if (teamId > 0) {
            return ResultUtils.success(teamId);
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "save team error");
        }
    }

    @DeleteMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestParam long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id is null");
        }
        User loginUser = userService.getCurrentLoginUser(request);
        Boolean team = teamService.deleteTeam(id, loginUser);
        if (team) {
            return ResultUtils.success(true);
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
    }

    //    @RolesAllowed({"1"})
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest team, HttpServletRequest request) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "team is null");
        }
        //判断是否为管理员
        User currentLoginUser = userService.getCurrentLoginUser(request);
        boolean admin = userService.isAdmin(request);
        boolean save = teamService.updateTeam(team, currentLoginUser, admin);
        ;
        if (save) {
            return ResultUtils.success(true);
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "update team error");
        }
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(@RequestParam(value = "id", required = true) long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id is null");
        }
        Team team = teamService.getById(id);
        if (team != null) {
            return ResultUtils.success(team);
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据库查询失败");
        }
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> getTeamList(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "teamQuery is null");
        }
//        Team team = new Team();
//        BeanUtils.copyProperties(team, teamQuery);//将query对象中的属性复制到teamQuery对象中，以便使用wrapper查询
        boolean admin = userService.isAdmin(request);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, admin);
        //判断是否为当前用户已加入的队伍
        teamList = judgeTeamIsJoined(teamList, userService.getCurrentLoginUser(request).getId());
        return ResultUtils.success(teamList);
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> getTeamListByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "teamQuery is null");
        }
        Team team = new Team();
        BeanUtils.copyProperties(team, teamQuery);//将query对象中的属性复制到teamQuery对象中，以便使用wrapper查询
        List<Team> teamList = teamService.list(new QueryWrapper<Team>(team));
        Page<Team> teamPage = teamService.page(new Page<Team>(teamQuery.getPageNum(), teamQuery.getPageSize()), new QueryWrapper<Team>(team));
        return ResultUtils.success(teamPage);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "teamJoinRequest is null");
        }
        boolean save = teamService.joinTeam(teamJoinRequest, userService.getCurrentLoginUser(request));
        if (save) {
            return ResultUtils.success(true);
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "加入队伍失败");
        }
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取我创建的队伍
     *
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        // 管理员可以看到所有队伍
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(teamList);
    }

    /**
     * 获取我加入的队伍
     *
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentLoginUser(request);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        // 取出不重复的队伍 id
        // teamId userId
        //1 2
        //2 3
        //2 4
        //1 5
        //1 ->2,5
        //2 ->3,4
        Map<Long, List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(listMap.keySet());//收集键值，也就是队伍ID
        teamQuery.setIdList(idList);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        //判断是否为当前用户已加入的队伍
        teamList = judgeTeamIsJoined(teamList, loginUser.getId());
        return ResultUtils.success(teamList);
    }

    /**
     * 对目前展示的队伍判断是否是当前用户已加入的
     */
    public List<TeamUserVO> judgeTeamIsJoined(List<TeamUserVO> teamUserVO, Long userId) {
        //获取当前队伍的id列表
        List<Long> teamIdList = teamUserVO.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        //从userteam中获取与当前用户相关的队伍
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.in("teamId", teamIdList);//从teamIdList中筛选出当前用户已加入的队伍
        //获取当前用户已加入的队伍id列表
        List<Long> joinedTeamIdList = userTeamService.list(queryWrapper).stream().map(UserTeam::getTeamId).collect(Collectors.toList());
        //   遍历teamUserVO，判断是否是当前用户已加入的队伍
        for (TeamUserVO teamUser : teamUserVO) {
            if (joinedTeamIdList.contains(teamUser.getId())) {
                teamUser.setHasJoin(true);
            }
        }
        return teamUserVO;
    }
}