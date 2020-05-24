package com.heitaoshu.service.order;
import com.heitaoshu.entity.PageResult;
import com.heitaoshu.pojo.order.Order;
import com.heitaoshu.pojo.order.OrderAndItem;

import java.util.*;

/**
 * order业务逻辑层
 */
public interface OrderService {


    public List<Order> findAll();


    public PageResult<Order> findPage(int page, int size);


    public List<Order> findList(Map<String,Object> searchMap);


    public PageResult<Order> findPage(Map<String,Object> searchMap,int page, int size);


    public Order findById(String id);

    public Map add(Order order);


    public void update(Order order);


    public void delete(String id);

    public void addOrderAndItem(OrderAndItem orderAndItem);

    public void editConsignStatus(Map<String, String> map);

    //订单超时
    public void orderTimeOut();

    //订单合并
    public void merge(String id1,String id2);

    //订单拆分
    public void spilt(String id, Integer num);

    //修改状态
    public void updatePayStatus(String orderId, String transactionId);

}
