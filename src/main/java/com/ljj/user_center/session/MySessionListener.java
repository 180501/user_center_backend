package com.ljj.user_center.session;

import com.ljj.user_center.session.MySessionContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class MySessionListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // 当会话被创建时，将其添加到 MySessionContext
        MySessionContext.getInstance().AddSession(se.getSession());
        System.out.println("Session created: " + se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        // 当会话被销毁时，从 MySessionContext 中删除会话
        MySessionContext.getInstance().DelSession(se.getSession().getId());
        System.out.println("Session destroyed: " + se.getSession().getId());
    }
}
