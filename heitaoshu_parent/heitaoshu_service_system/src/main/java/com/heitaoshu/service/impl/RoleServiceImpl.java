package com.heitaoshu.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.heitaoshu.dao.RoleMapper;
import com.heitaoshu.dao.RoleResourceMapper;
import com.heitaoshu.entity.PageResult;
import com.heitaoshu.pojo.system.Role;
import com.heitaoshu.pojo.system.RoleResource;
import com.heitaoshu.pojo.system.RoleResources;
import com.heitaoshu.service.system.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

@Service(interfaceClass = RoleService.class)
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private RoleResourceMapper roleResourceMapper;


    @Override
    public List<Role> findAll() {
        return roleMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Role> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Role> roless = (Page<Role>) roleMapper.selectAll();
        return new PageResult<Role>(roless.getTotal(),roless.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Role> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        List<Role> roles = roleMapper.selectByExample(example);
        return roles;
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Role> findPage(Map<String, Object> searchMap, int page, int size) {
        Example example = createExample(searchMap);
        PageHelper.startPage(page,size);
        Page<Role> roless = (Page<Role>) roleMapper.selectByExample(example);
        return new PageResult<Role>(roless.getTotal(),roless.getResult());
    }

    /**
     * 根据Id查询
     *
     * @param id
     * @return
     */
    public RoleResources findById(Integer id) {
        RoleResources roleResources = new RoleResources();
        RoleResource roleResource = new RoleResource();
        Role role = roleMapper.selectByPrimaryKey(id);
        roleResources.setRole(role);
        roleResources.setRoleResources(findRoleResource(role));
        return roleResources;
    }

    /**
     * 新增
     */
    @Transactional
    public void add(RoleResources roleResources) {
        //插入用户表
        roleMapper.insert(roleResources.getRole());
        //插入权限表
        for(RoleResource roleResource1 : roleResources.getRoleResources()){
            roleResourceMapper.insert(roleResource1);
        }
    }

    /**
     * 修改
     */
    @Transactional
    public void update(RoleResources roleResources) {
        //修改用户表
        roleMapper.updateByPrimaryKeySelective(roleResources.getRole());
        //修改权限表

        //删除对应权限
        Example example = new Example(RoleResource.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("roleId",roleResources.getRole().getId());
        roleResourceMapper.deleteByExample(example);

        //添加权限
        for (RoleResource roleResource : roleResources.getRoleResources()) {
            roleResourceMapper.insert(roleResource);
        }
    }

    /**
     *  删除
     * @param id
     */
    @Transactional
    public void delete(Integer id) {
        roleMapper.deleteByPrimaryKey(id);
        //删除对应权限表
        Example example = new Example(RoleResource.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("roleId",id);
        roleResourceMapper.deleteByExample(example);
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Role.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 角色名称
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
            }

            // ID
            if(searchMap.get("id")!=null ){
                criteria.andEqualTo("id",searchMap.get("id"));
            }

        }
        return example;
    }

    /**
     * 根据role查找用户权限表
     * @param role
     * @return
     */
    private List<RoleResource> findRoleResource(Role role){
        Example example = new Example(RoleResource.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("roleId",role.getId());
        return roleResourceMapper.selectByExample(example);
    }

}
