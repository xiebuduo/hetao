package com.heitaoshu.service.goods;
import com.heitaoshu.entity.PageResult;
import com.heitaoshu.pojo.goods.Category;

import java.util.*;

/**
 * category业务逻辑层
 */
public interface CategoryService {


    public List<Category> findAll();


    public PageResult<Category> findPage(int page, int size);


    public List<Category> findList(Map<String,Object> searchMap);


    public PageResult<Category> findPage(Map<String,Object> searchMap,int page, int size);


    public Category findById(Integer id);

    public void add(Category category);


    public void update(Category category);


    public void delete(Integer id);

    //查找栏目
    public List<Map> categories();

    //将分类导航存到缓存
    public void saveCategoryTreeToRedis();
}
