package com.heitaoshu.service.goods;
import com.heitaoshu.entity.PageResult;
import com.heitaoshu.pojo.goods.Sku;
import com.heitaoshu.pojo.order.OrderItem;

import java.util.*;

/**
 * sku业务逻辑层
 */
public interface SkuService {


    public List<Sku> findAll();


    public PageResult<Sku> findPage(int page, int size);


    public List<Sku> findList(Map<String,Object> searchMap);


    public PageResult<Sku> findPage(Map<String,Object> searchMap,int page, int size);


    public Sku findById(String id);

    public void add(Sku sku);


    public void update(Sku sku);


    public void delete(String id);

    public void saveAllPriceToRedis();

    public Integer findPriceById(String id);

    public void saveSkuByIPToRedis(String id,Integer price);

    public void deleteSkuFromRedisById(String id);

    //减少库存
    public boolean reduceNum(List<OrderItem> orderItems);
}
