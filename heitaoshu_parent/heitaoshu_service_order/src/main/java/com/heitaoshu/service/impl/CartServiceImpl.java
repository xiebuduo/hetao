package com.heitaoshu.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.heitaoshu.pojo.goods.Sku;
import com.heitaoshu.pojo.order.OrderItem;
import com.heitaoshu.service.order.CartService;
import com.heitaoshu.service.goods.CategoryService;
import com.heitaoshu.service.goods.SkuService;
import com.heitaoshu.service.order.PreferentialService;
import com.heitaoshu.util.CacheKey;
import com.heitaoshu.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Reference
    private SkuService skuService;
    @Autowired
    private PreferentialService preferentialService;
    @Autowired
    private IdWorker idWorker;
    @Reference
    private CategoryService categoryService;

    public List<Map> getCart(String username) {
        //从缓存中查找
        List<Map> cartList = (List<Map>) redisTemplate.boundHashOps(CacheKey.CART_LIST).get(username);
        return cartList;
    }

    public void addCart(String username, String skuId, Integer num) {
        //检查格式
        Sku sku = skuService.findById(skuId);
        if(sku == null){
            throw new RuntimeException("不存在该商品");
        }
        //商品是否存在
//        redisTemplate.boundHashOps(CacheKey.CART_LIST).delete(username);
        boolean flag = false;
        //该用户购物车是否存在
        OrderItem orderItem = new OrderItem();
        List<Map> cartList = (List<Map>) redisTemplate.boundHashOps(CacheKey.CART_LIST).get(username);
        if(cartList != null){
            for (Map map : cartList){
                if (skuId.equals(((OrderItem)map.get("item")).getSkuId())){
                    orderItem =  (OrderItem)map.get("item");
                    orderItem.setNum(orderItem.getNum() + num);
                    //如果数量小于0则删除该商品
                    if(orderItem.getNum() <= 0){
                        cartList.remove(map);
                        redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,cartList);
                        return;
                    }

                    orderItem.setWeight(sku.getWeight() * orderItem.getNum());
                    orderItem.setMoney(sku.getPrice() * orderItem.getNum());
                    //计算优惠
                    int preMoney = preferentialService.findPreMoneyByCategoryId(orderItem.getCategoryId3(),orderItem.getMoney());
                    orderItem.setPayMoney(orderItem.getMoney() - preMoney);
                    //不删就有异常
                    cartList.remove(map);


                    map.put("item",orderItem);
                    map.put("check",map.get("check"));
                    cartList.add(map);
                    redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,cartList);


                    //存在则退出循环，标志为true
                    flag = true;
                    break;
                }
            }
        }

        //不存在则添加
        if (cartList == null || flag ==false) {
            if (num <= 0) {
                throw new RuntimeException("商品数量必须大于0");
            }
            Map orderMap = new HashMap();

            orderItem.setOrderId(String.valueOf(idWorker.nextId()));
            orderItem.setSkuId(skuId);
            orderItem.setSpuId(sku.getSpuId());
            orderItem.setName(sku.getName());
            orderItem.setImage(sku.getImage());
            orderItem.setNum(num);
            orderItem.setPrice(sku.getPrice());
            orderItem.setMoney(sku.getPrice() * num);
            //计算优惠
            int preMoney = preferentialService.findPreMoneyByCategoryId(orderItem.getCategoryId3(),orderItem.getMoney());
            orderItem.setPayMoney(orderItem.getMoney() - preMoney);
            orderItem.setWeight(sku.getWeight() * num);
            orderItem.setCategoryId3(sku.getCategoryId());
            //二级栏目ID
            Integer category2 = (Integer) redisTemplate.boundHashOps(CacheKey.CATEGORY).get(sku.getCategoryId());
            if (category2 == null) {
                category2 = categoryService.findById(sku.getCategoryId()).getParentId();
                redisTemplate.boundHashOps(CacheKey.CATEGORY).put(sku.getCategoryId(), category2);
            }
            orderItem.setCategoryId2(category2);

            //一级栏目ID
            Integer category1 = (Integer) redisTemplate.boundHashOps(CacheKey.CATEGORY).get(category2);
            if (category1 == null) {
                category1 = categoryService.findById(category2).getParentId();
                redisTemplate.boundHashOps(CacheKey.CATEGORY).put(category2, category1);
            }
            orderItem.setCategoryId1(category1);
            orderMap.put("item",orderItem);
            orderMap.put("check",false);
            cartList.add(orderMap);
            //添加到缓存
            redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,cartList);

        }
    }

    /**
     * 改变一个商品选中状态
     * @param username
     * @param id
     */
    public void changeCheck(String username, String id) {
        List<Map> cartList= (List<Map>) redisTemplate.boundHashOps(CacheKey.CART_LIST).get(username);
        for (Map map : cartList){
            if(((OrderItem)map.get("item")).getSkuId().equals(id)){
                map.put("check", !(Boolean)map.get("check"));
                cartList.remove(map);
                cartList.add(map);
                redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,cartList);
                return;
            }
        }
    }

    /**
     * 改变所有商品状态
     * @param username
     */
    public void changeAllCheck(String username,Boolean status) {
        List<Map> cartList = (List<Map>)redisTemplate.boundHashOps(CacheKey.CART_LIST).get(username);
        List<Map> newCart = new ArrayList<>();
        for(Map map : cartList){
            map.put("check",status);
            newCart.add(map);
        }
        redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,newCart);
        return;
    }

    public void delCart(String username) {
        List<Map> cartList = getCart(username).stream().filter(cart->(boolean)cart.get("check") == false).collect(Collectors.toList());
        redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username, cartList);
        return;
    }

    /**
     * 刷新购物车
     */
    public void refresh(String username) {
        List<Map> cartList = getCart(username);
        List<Map> newCart = new ArrayList<>();
        for (Map cart : cartList) {
            OrderItem orderItem = (OrderItem) cart.get("item");
            Sku sku = skuService.findById(orderItem.getSkuId());
            orderItem.setPrice(sku.getPrice());
            orderItem.setMoney(sku.getPrice() * orderItem.getNum());
            //优惠
            int preMoney = preferentialService.findPreMoneyByCategoryId(sku.getCategoryId(),orderItem.getMoney());
            orderItem.setPayMoney(orderItem.getMoney() - preMoney);
            cart.put("item",orderItem);
            newCart.add(cart);
        }
        //更新缓存
        redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,newCart);
    }
}
