package com.heitaoshu.controller.system;

import com.alibaba.dubbo.config.annotation.Reference;
import com.heitaoshu.entity.PageResult;
import com.heitaoshu.entity.Result;
import com.heitaoshu.pojo.system.Role;
import com.heitaoshu.pojo.system.RoleResources;
import com.heitaoshu.service.system.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/role")
public class RoleController {

    @Reference
    private RoleService roleService;

    @GetMapping("findAll")
    public List<Role> findAll(){
        return roleService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult<Role> findPage(int page, int size){
        return roleService.findPage(page, size);
    }

    @PostMapping("/findList")
    public List<Role> findList(@RequestBody Map<String,Object> searchMap){
        return roleService.findList(searchMap);
    }

    @PostMapping("/findPage")
    public PageResult<Role> findPage(@RequestBody Map<String,Object> searchMap,int page, int size){
        return  roleService.findPage(searchMap,page,size);
    }

    @GetMapping("/findById")
    public RoleResources findById(Integer id){
        return roleService.findById(id);
    }


    @PostMapping("/add")
    public Result add(@RequestBody RoleResources roleResources){
        roleService.add(roleResources);
        return new Result();
    }

    @PostMapping("/update")
    public Result update(@RequestBody RoleResources roleResources){
        roleService.update(roleResources);
        return new Result();
    }

    @GetMapping("/delete")
    public Result delete(Integer id){
        roleService.delete(id);
        return new Result();
    }

}
