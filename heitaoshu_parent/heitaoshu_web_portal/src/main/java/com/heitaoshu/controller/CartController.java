package com.heitaoshu.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.heitaoshu.entity.Result;
import com.heitaoshu.pojo.order.Order;
import com.heitaoshu.service.order.CartService;
import com.heitaoshu.service.order.OrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference
    private CartService cartService;

    @GetMapping("/getCart")
    public List<Map> getCart() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.refresh(username);
        return cartService.getCart(username);
    }

    @GetMapping("addCart")
    public Result addCart(String skuId, Integer num) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.addCart(username, skuId, num);
        return new Result();
    }

    @GetMapping("changeCheck")
    public Result changeCheck(String skuId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.changeCheck(username, skuId);
        return new Result();
    }

    @GetMapping("changeAllCheck")
    public Result changeAllCheck(Boolean status) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.changeAllCheck(username, status);
        return new Result();
    }

    @PostMapping("delCart")
    public Result delCart() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.delCart(username);
        return new Result();
    }

    @Reference
    private OrderService orderService;
    @PostMapping("saveOrder")
    public Map saveOrder(@RequestBody Order order){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        order.setUsername(username);
        return orderService.add(order);
    }
}
