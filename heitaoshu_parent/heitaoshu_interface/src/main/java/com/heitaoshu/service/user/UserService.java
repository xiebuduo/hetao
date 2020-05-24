package com.heitaoshu.service.user;
import com.heitaoshu.entity.PageResult;
import com.heitaoshu.pojo.user.User;

import java.util.*;

/**
 * user业务逻辑层
 */
public interface UserService {


    public List<User> findAll();


    public PageResult<User> findPage(int page, int size);


    public List<User> findList(Map<String,Object> searchMap);


    public PageResult<User> findPage(Map<String,Object> searchMap,int page, int size);


    public User findById(String username);

    public void add(User user);


    public void update(User user);


    public void delete(String username);

    //生成验证码
    public void sendSms(String phone);
    //添加用户
    public void addUser(User user,String code);

}