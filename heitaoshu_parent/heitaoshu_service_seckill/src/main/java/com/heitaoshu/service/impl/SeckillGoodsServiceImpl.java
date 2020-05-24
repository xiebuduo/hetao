package com.heitaoshu.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.heitaoshu.pojo.seckill.SeckillGoods;
import com.heitaoshu.service.seckill.SeckillGoodsService;
import com.heitaoshu.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    private RedisTemplate redisTemplate;

    /****
     * 根据商品ID查询商品详情
     * @param time:商品秒杀时区
     * @param id:商品ID
     * @return
     */
    @Override
    public SeckillGoods one(String time, Long id) {
        return (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_"+time).get(id);
    }

    /***
     * 根据时间区间查询秒杀商品列表
     * @param time
     * @return
     */
    @Override
    public List<SeckillGoods> list(String time) {
        //组装key
        String key = "SeckillGoods_"+time;
        return redisTemplate.boundHashOps(key).values();
    }
}
