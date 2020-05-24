package com.heitaoshu.controller.system;

import com.alibaba.dubbo.config.annotation.Reference;
import com.heitaoshu.entity.PageResult;
import com.heitaoshu.entity.Result;
import com.heitaoshu.pojo.system.Admin;
import com.heitaoshu.pojo.system.AdminRole;
import com.heitaoshu.pojo.system.AdminRoles;
import com.heitaoshu.service.system.AdminService;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Reference
    private AdminService adminService;

    @GetMapping("/findAll")
    public List<Admin> findAll(){
        return adminService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult<Admin> findPage(int page, int size){
        return adminService.findPage(page, size);
    }

    @PostMapping("/findList")
    public List<Admin> findList(@RequestBody Map<String,Object> searchMap){
        return adminService.findList(searchMap);
    }

    @PostMapping("/findPage")
    public PageResult<Admin> findPage(@RequestBody Map<String,Object> searchMap,int page, int size){
        return  adminService.findPage(searchMap,page,size);
    }

    @GetMapping("/findById")
    public AdminRoles findById(Integer id){
        return adminService.findById(id);
    }


    @PostMapping("/add")
    public Result add(@RequestBody AdminRoles adminRoles){
        String gensalt = BCrypt.gensalt();
        Admin admin = adminRoles.getAdmin();
        System.out.println(adminRoles.getAdmin());
        admin.setPassword(BCrypt.hashpw(admin.getPassword(),gensalt));
        //数组转集合
        adminRoles.setAdmin(admin);
        adminService.add(adminRoles);
        return new Result();
    }

    @PostMapping("/update")
    public Result update(@RequestBody AdminRoles adminRoles){
        String gensalt = BCrypt.gensalt();
        Admin admin = adminRoles.getAdmin();
        admin.setPassword(BCrypt.hashpw(admin.getPassword(),gensalt));
        adminService.update(adminRoles);
        return new Result();
    }

    @GetMapping("/delete")
    public Result delete(Integer id){
        adminService.delete(id);
        return new Result();
    }

}
