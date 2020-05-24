package com.heitaoshu.controller.login;

import com.alibaba.dubbo.config.annotation.Reference;
import com.heitaoshu.entity.Result;
import com.heitaoshu.pojo.system.Admin;
import com.heitaoshu.service.system.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")

public class LoginController{
    /**
     * 获取当前用户名
     * @return
     */
    @GetMapping("/name")
    public Map showName(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = new HashMap();
        map.put("name",name);
        return map;
    }

    /**
     * 修改当前用户密码
     */
    @Reference
    private AdminService adminService;
    @Autowired
    private HttpServletRequest request;
    @PostMapping("/upPass")
    public Result upPass(@RequestParam("oldPass") String oldPass,@RequestParam("newPass") String newPass){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Admin admin = adminService.findByName(name);
        if(BCrypt.checkpw(oldPass,admin.getPassword())){
            if(newPass != null && !"".equals(newPass)){
                newPass = BCrypt.hashpw(newPass,BCrypt.gensalt());
                adminService.upPass(name,newPass);
                request.getSession().invalidate();
                return new Result(0,"成功！");
            }
        }
        else{
            return new Result(1,"原密码错误");
        }
        return new Result(2,"");

    }

}