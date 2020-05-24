package com.heitaoshu.controller.login;

import com.alibaba.dubbo.config.annotation.Reference;
import com.heitaoshu.pojo.system.LoginLog;
import com.heitaoshu.service.system.LoginLogService;
import com.heitaoshu.util.WebUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {
    @Reference
    private LoginLogService loginLogService;
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        System.out.println("登录成功，我要在这里写日志！");
        LoginLog loginLog = new LoginLog();
        loginLog.setLoginName(authentication.getName());
        loginLog.setIp(httpServletRequest.getRemoteAddr());
        loginLog.setLoginTime(new Date());
        String agent = httpServletRequest.getHeader("user-agent");
        loginLog.setBrowserName(WebUtil.getBrowserName(agent));
        loginLog.setLocation(WebUtil.getCityByIP(loginLog.getIp()));
        loginLogService.add(loginLog);
        httpServletResponse.setContentType("application/json;charset=utf-8");
        httpServletResponse.getWriter().write("登录成功！");
        httpServletRequest.getRequestDispatcher("/main.html").forward(httpServletRequest, httpServletResponse);
    }
}
