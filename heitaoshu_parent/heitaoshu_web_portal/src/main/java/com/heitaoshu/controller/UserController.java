package com.heitaoshu.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.heitaoshu.entity.Result;
import com.heitaoshu.pojo.user.Address;
import com.heitaoshu.pojo.user.User;
import com.heitaoshu.service.user.AddressService;
import com.heitaoshu.service.user.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Reference
    private UserService userService;
    @Reference
    private AddressService addressService;

    @GetMapping("/sendSms")
    public Result sendSms(String phone) {
        userService.sendSms(phone);
        return new Result();
    }

    @PostMapping("/addUser")
    public Result addUser(@RequestBody User user, String code){
        //密码加密
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String newPassword = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(newPassword);

        userService.addUser(user,code);
        return  new Result();
    }
    @GetMapping("getUserName")
    public String getUserName(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }


    @GetMapping("getAddr")
    public List<Address> getAddr(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return addressService.getAddr(username);
    }
}
