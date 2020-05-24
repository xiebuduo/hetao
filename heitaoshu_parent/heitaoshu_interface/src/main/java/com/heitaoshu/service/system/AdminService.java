package com.heitaoshu.service.system;
import com.heitaoshu.entity.PageResult;
import com.heitaoshu.pojo.system.Admin;
import com.heitaoshu.pojo.system.AdminRoles;

import java.util.*;

/**
 * admin业务逻辑层
 */
public interface AdminService {


    public List<Admin> findAll();


    public PageResult<Admin> findPage(int page, int size);


    public List<Admin> findList(Map<String,Object> searchMap);


    public PageResult<Admin> findPage(Map<String,Object> searchMap,int page, int size);


    public AdminRoles findById(Integer id);

    public void add(AdminRoles adminRoles);


    public void update(AdminRoles adminRoles);


    public void delete(Integer id);

    //根据用户名查找
    public Admin findByName(String name);

    //修改密码
    public void upPass(String name, String password);
}
