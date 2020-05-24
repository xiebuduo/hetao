package com.heitaoshu.service.system;
import com.heitaoshu.entity.PageResult;
import com.heitaoshu.pojo.system.Role;
import com.heitaoshu.pojo.system.RoleResources;

import java.util.*;

/**
 * role业务逻辑层
 */
public interface RoleService {

    public List<Role> findAll();

    public PageResult<Role> findPage(int page, int size);


    public List<Role> findList(Map<String,Object> searchMap);


    public PageResult<Role> findPage(Map<String,Object> searchMap,int page, int size);


    public RoleResources findById(Integer id);

    public void add(RoleResources roleResources);

    public void update(RoleResources roleResources);

    public void delete(Integer id);

}
