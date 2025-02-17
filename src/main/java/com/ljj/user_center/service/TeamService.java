package com.ljj.user_center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ljj.user_center.model.domain.Team;
import com.ljj.user_center.model.domain.User;
import com.ljj.user_center.model.domain.dto.TeamQuery;
import com.ljj.user_center.model.domain.request.TeamJoinRequest;
import com.ljj.user_center.model.domain.request.TeamQuitRequest;
import com.ljj.user_center.model.domain.request.TeamUpdateRequest;
import com.ljj.user_center.model.domain.vo.TeamUserVO;

import java.util.List;


/**
* @author DELL
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2025-01-15 11:04:03
*/
public interface TeamService extends IService<Team> {

    long addTeam(Team team, User loginUser);

    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean admin);

    boolean updateTeam(TeamUpdateRequest team, User currentLoginUser, boolean admin);

    /**
     * 用户加入队伍
     *
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User currentLoginUser);

    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);


    Boolean deleteTeam(Long teamId, User loginUser);
}
