package com.heitaoshu.service.business;
import com.heitaoshu.entity.PageResult;
import com.heitaoshu.pojo.business.Ad;
import com.heitaoshu.pojo.goods.Category;

import java.util.*;

/**
 * ad业务逻辑层
 */
public interface AdService {


    public List<Ad> findAll();


    public PageResult<Ad> findPage(int page, int size);


    public List<Ad> findList(Map<String,Object> searchMap);


    public PageResult<Ad> findPage(Map<String,Object> searchMap,int page, int size);


    public Ad findById(Integer id);

    public void add(Ad ad);


    public void update(Ad ad);


    public void delete(Integer id);

    public List<Ad> findByPosition(String position);


    //广告缓存
    public void saveAdToRedisByPosition(String position);

    public void saveAllAdToRedis();
}
