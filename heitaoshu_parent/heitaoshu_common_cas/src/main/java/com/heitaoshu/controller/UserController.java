package com.heitaoshu.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("login")
public class UserController {
    @GetMapping("username")
    public Map userName(){
        Map map = new HashMap();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if(!"".equals(username)){
            map.put("username","");
        }
        map.put("username",username);
        return map;
    }
}
