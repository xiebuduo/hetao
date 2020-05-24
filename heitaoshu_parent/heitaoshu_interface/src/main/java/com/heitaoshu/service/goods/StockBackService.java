package com.heitaoshu.service.goods;

import com.heitaoshu.pojo.order.OrderItem;

import java.util.List;

public interface StockBackService {
    /**
     * 添加回滚数据到数据库
     */
    public void addList(List<OrderItem> orderItems);

    /**
     * 回滚
     */
    public void doBack();
}
