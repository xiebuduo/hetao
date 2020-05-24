package com.heitaoshu.timer;

import com.heitaoshu.dao.SeckillGoodsMapper;
import com.heitaoshu.pojo.seckill.SeckillGoods;
import com.heitaoshu.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Set;


@Component
public class SeckillGoodsTask {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /*****
     * 30秒执行一次
     * 0/30:表示从0秒开始执行，每过30秒再次执行
     */
    @Scheduled(cron = "0/15 * * * * ?")
    public void loadGoods(){
        //1.查询所有时间区间
        List<Date> dateMenus = DateUtil.getDateMenus();
        
        //2.循环时间区间，查询每个时间区间的秒杀商品
        for (Date startTime : dateMenus) {
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            //2.1   商品必须审核通过
            criteria.andEqualTo("status","1");

            //2.2   库存>0
            criteria.andGreaterThan("stockCount",0);

            //2.3   秒杀开始时间>=当前循环的时间区间的开始时间
            criteria.andGreaterThanOrEqualTo("startTime",startTime);

            //2.3   秒杀结束时间<当前循环的时间区间的开始时间+2小时
            criteria.andLessThan("endTime",DateUtil.addDateHour(startTime,2));

            //2.4 过滤Redis中已经存在的该区间的秒杀商品
            Set keys = redisTemplate.boundHashOps("SeckillGoods_" + DateUtil.date2Str(startTime)).keys();
            if(keys!=null && keys.size()>0){
                //select * from table where id not in(keys)
                criteria.andNotIn("id",keys);
            }

            //2.4 执行查询
            List<SeckillGoods> seckillGoods =seckillGoodsMapper.selectByExample(example);

            //3.将秒杀商品存入到Redis缓存
            for (SeckillGoods seckillGood : seckillGoods) {
                redisTemplate.boundHashOps("SeckillGoods_"+DateUtil.date2Str(startTime)).put(seckillGood.getId(),seckillGood);
            }
        }
    }

}
