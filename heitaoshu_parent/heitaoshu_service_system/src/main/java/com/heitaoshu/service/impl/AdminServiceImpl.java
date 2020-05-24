package com.heitaoshu.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.heitaoshu.dao.AdminMapper;
import com.heitaoshu.dao.AdminRoleMapper;
import com.heitaoshu.entity.PageResult;
import com.heitaoshu.pojo.system.Admin;
import com.heitaoshu.pojo.system.AdminRole;
import com.heitaoshu.pojo.system.AdminRoles;
import com.heitaoshu.service.system.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = AdminService.class)
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private AdminRoleMapper adminRoleMapper;

    /**
     * 返回全部记录
     * @return
     */
    public List<Admin> findAll() {
        return adminMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Admin> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Admin> admins = (Page<Admin>) adminMapper.selectAll();
        return new PageResult<Admin>(admins.getTotal(),admins.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Admin> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return adminMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Admin> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Admin> admins = (Page<Admin>) adminMapper.selectByExample(example);
        return new PageResult<Admin>(admins.getTotal(),admins.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public AdminRoles findById(Integer id)
    {
        AdminRoles adminRoles = new AdminRoles();
        Admin admin = adminMapper.selectByPrimaryKey(id);
        adminRoles.setAdmin(admin);

        //查找roleId
        Example example = new Example(AdminRole.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("adminId",admin.getId());
        List<AdminRole> adminRoless = adminRoleMapper.selectByExample(example);
        List<Integer> roleIds = new ArrayList<>();
        //获取roleId
        for(AdminRole adminRole: adminRoless){
            roleIds.add(adminRole.getRoleId());
        }
        adminRoles.setRoleIds(roleIds);
        return adminRoles;
    }

    /**
     * 新增
     *
     * @param admin
     */
    @Transactional
    public void add(AdminRoles adminRoles) {
        //添加管理员
        adminMapper.insert(adminRoles.getAdmin());
        Integer adminId = adminRoles.getAdmin().getId();
        AdminRole adminRole = new AdminRole();
        adminRole.setAdminId(adminId);
        //添加该管理员角色
        for (Integer integer : adminRoles.getRoleIds()) {
            adminRole.setRoleId(integer);
            adminRoleMapper.insert(adminRole);
        }
    }

    /**
     * 修改
     * @param
     */
    @Transactional
    public void update(AdminRoles adminRoles) {
        Admin admin = adminRoles.getAdmin();
        adminMapper.updateByPrimaryKeySelective(admin);

        //删除该用户所有角色
        Example example = new Example(AdminRole.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("adminId",admin.getId());
        adminRoleMapper.deleteByExample(example);

        //添加角色
        List<Integer> roleIds = adminRoles.getRoleIds();
        AdminRole adminRole = new AdminRole();
        adminRole.setAdminId(admin.getId());
        for(Integer roleId : roleIds){
            adminRole.setRoleId(roleId);
            adminRoleMapper.insert(adminRole);
        }
    }

    /**
     *  删除
     * @param id
     */
    @Transactional
    public void delete(Integer id) {
        adminMapper.deleteByPrimaryKey(id);
        //删除用户角色表
        Example example = new Example(AdminRole.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("adminId",id);
        adminRoleMapper.deleteByExample(example);
    }

    /**
     * 根据用户名查找
     */
    @Override
    public Admin findByName(String name) {
        Example example = new Example(Admin.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("loginName",name);
        return adminMapper.selectOneByExample(example);
    }

    /**
     * 修改密码
     */
    @Override
    public void upPass(String name,String password) {
        Admin admin = new Admin();
        admin.setLoginName(name);
        admin.setPassword(password);

        Example example = new Example(Admin.class);
        Example.Criteria criteria= example.createCriteria();
        criteria.andEqualTo("loginName",name);
        adminMapper.updateByExampleSelective(admin,example);
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Admin.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 用户名
            if(searchMap.get("loginName")!=null && !"".equals(searchMap.get("loginName"))){
//                criteria.andLike("loginName","%"+searchMap.get("loginName")+"%");
                criteria.andEqualTo("loginName",searchMap.get("loginName"));
            }
            // 密码
            if(searchMap.get("password")!=null && !"".equals(searchMap.get("password"))){
                criteria.andEqualTo("password",searchMap.get("password"));
            }
            // 状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andEqualTo("status",searchMap.get("status"));
            }

            // id
            if(searchMap.get("id")!=null ){
                criteria.andEqualTo("id",searchMap.get("id"));
            }

        }
        return example;
    }

}
