package com.heitaoshu.service.order;

import java.util.List;
import java.util.Map;

public interface CartService {
    //查询购物车
    public List<Map> getCart(String username);
    //添加购物车
    public void addCart(String username,String skuId, Integer num);
    //改变一个选中状态
    public void changeCheck(String username, String id);
    //改变所有状态
    public void changeAllCheck(String username,Boolean status);
    //删除选中的购物车
    public void delCart(String username);
    //刷新购物车
    public void refresh(String username);
}
