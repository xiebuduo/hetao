package com.heitaoshu.service.goods;

import com.heitaoshu.entity.PageResult;
import com.heitaoshu.pojo.goods.Audit;
import com.heitaoshu.pojo.goods.Brand;

import java.util.List;
import java.util.Map;

public interface AuditService {
    public List<Brand> findAll();


    public PageResult<Brand> findPage(int page, int size);


    public List<Brand> findList(Map<String,Object> searchMap);


    public PageResult<Brand> findPage(Map<String,Object> searchMap,int page, int size);


    public Brand findById(Integer id);

    public void add(Audit audit);


    public void update(Audit audit);


    public void delete(Integer id);
}
