package com.heitaoshu.controller.login;

import net.sf.json.JSONObject;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationFailureHandlerImpl implements AuthenticationFailureHandler {
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        System.out.println("登录时失败");
        Map<String, String> map = new HashMap<>();
        map.put("code", "1");
        map.put("msg", "登录失败");
        if("Cannot pass null or empty values to constructor".equals(e.getMessage()) || "UserDetailsService returned null, which is an interface contract violation".equals(e.getMessage()) || "Bad credentials".equals(e.getMessage())){
            map.put("data", "请输入正确的用户名或密码！");
        } else if("Cannot pass a null GrantedAuthority collection".equals(e.getMessage())){
            map.put("data", "该用户没有任何权限限！请联系管理员");
        } else if("GrantedAuthority list cannot contain any null elements".equals(e.getMessage())){
            map.put("data", "权限集合异常！请联系开发人员！");
        } else if("username cannot be null".equals(e.getMessage())){
            map.put("data","请输入用户名！");
        } else if("password cannot be null".equals(e.getMessage())) {
            map.put("data", "请输入密码");
        } else if("Maximum sessions of".contains(e.getMessage())){
            map.put("data","您已在其他地方登录！");
        }
        else{
            map.put("data",e.getMessage()+"异常！请联系开发人员！");
        }

        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletRequest.getSession().setAttribute("errormsg", map);
        PrintWriter out = httpServletResponse.getWriter();
        JSONObject jsonObject = JSONObject.fromObject(map);
        out.write(jsonObject.toString());
        out.flush();
        out.close();
    }
}
