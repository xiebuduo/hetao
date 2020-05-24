package com.heitaoshu.service.goods;

import com.heitaoshu.entity.PageResult;
import com.heitaoshu.pojo.goods.Brand;
import com.heitaoshu.pojo.goods.SpuLog;

import java.util.List;
import java.util.Map;

public interface SpuLogService {
    public List<Brand> findAll();


    public PageResult<Brand> findPage(int page, int size);


    public List<Brand> findList(Map<String,Object> searchMap);


    public PageResult<Brand> findPage(Map<String,Object> searchMap,int page, int size);


    public Brand findById(Integer id);

    public void add(SpuLog spuLog);


    public void update(SpuLog spuLog);


    public void delete(Integer id);
}
