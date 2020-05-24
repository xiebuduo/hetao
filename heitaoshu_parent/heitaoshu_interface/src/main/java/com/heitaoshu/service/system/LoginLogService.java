package com.heitaoshu.service.system;
import com.heitaoshu.entity.PageResult;
import com.heitaoshu.pojo.system.LoginLog;

import java.util.*;

/**
 * loginLog业务逻辑层
 */
public interface LoginLogService {


    public List<LoginLog> findAll();


    public PageResult<LoginLog> findPage(int page, int size);


    public List<LoginLog> findList(Map<String,Object> searchMap);


    public PageResult<LoginLog> findPage(Map<String,Object> searchMap,int page, int size);


    public LoginLog findById(Integer id);

    public void add(LoginLog loginLog);


    public void update(LoginLog loginLog);


    public void delete(Integer id);

}
