package com.heitaoshu.consumer;

import com.alibaba.fastjson.JSONObject;
import com.heitaoshu.pojo.order.OrderItem;
import com.heitaoshu.service.goods.StockBackService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SkuBackConsumer implements MessageListener {
    @Autowired
    private StockBackService stockBackService;
    public void onMessage(Message message) {
        List<OrderItem> orderItems = JSONObject.parseArray(new String(message.getBody()), OrderItem.class);
        stockBackService.addList(orderItems);
    }
}
