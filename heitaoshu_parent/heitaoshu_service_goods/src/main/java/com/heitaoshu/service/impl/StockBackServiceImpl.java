package com.heitaoshu.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.heitaoshu.dao.SkuMapper;
import com.heitaoshu.dao.StockBackMapper;
import com.heitaoshu.pojo.goods.StockBack;
import com.heitaoshu.pojo.order.OrderItem;
import com.heitaoshu.service.goods.StockBackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service(interfaceClass = StockBackService.class)
public class StockBackServiceImpl implements StockBackService {

    @Autowired
    private StockBackMapper stockBackMapper;

    @Transactional
    public void addList(List<OrderItem> orderItems) {
        for(OrderItem orderItem : orderItems){
            StockBack stockBack = new StockBack();
            stockBack.setOrderId(orderItem.getOrderId());
            stockBack.setSkuId(orderItem.getSkuId());

            //是否存在
            List<StockBack> stockBacks = stockBackMapper.select(stockBack);

            stockBack.setStatus("0");
            stockBack.setNum(orderItem.getNum());
            stockBack.setCreateTime(new Date());
            if (! (stockBacks.size() > 0)){
                stockBackMapper.insert(stockBack);
            }
        }
    }
    /**
     * 执行回滚
     */
    @Autowired
    private SkuMapper skuMapper;
    @Transactional
    public void doBack() {
        StockBack stockBack = new StockBack();
        stockBack.setStatus("0");
        List<StockBack> stockBackList = stockBackMapper.select(stockBack);
        for(StockBack stockBack1 : stockBackList){
            skuMapper.upStockAndSaleNum(stockBack1.getSkuId(),- stockBack1.getNum());
            stockBack1.setStatus("1");
            stockBack1.setBackTime(new Date());
            stockBackMapper.updateByPrimaryKeySelective(stockBack1);
        }
    }
}
