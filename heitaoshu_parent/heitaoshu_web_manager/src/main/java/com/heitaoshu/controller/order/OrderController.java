package com.heitaoshu.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.heitaoshu.entity.PageResult;
import com.heitaoshu.entity.Result;
import com.heitaoshu.pojo.order.Order;
import com.heitaoshu.pojo.order.OrderAndItem;
import com.heitaoshu.service.order.CategoryReportService;
import com.heitaoshu.service.order.OrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Reference
    private OrderService orderService;

    @GetMapping("/findAll")
    public List<Order> findAll() {
        return orderService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult<Order> findPage(int page, int size) {
        return orderService.findPage(page, size);
    }

    @PostMapping("/findList")
    public List<Order> findList(@RequestBody Map<String, Object> searchMap) {
        return orderService.findList(searchMap);
    }

    @PostMapping("/findPage")
    public PageResult<Order> findPage(@RequestBody Map<String, Object> searchMap, int page, int size) {
        return orderService.findPage(searchMap, page, size);
    }

    @GetMapping("/findById")
    public Order findById(String id) {
        return orderService.findById(id);
    }


    @PostMapping("/add")
    public Result add(@RequestBody Order order) {
        orderService.add(order);
        return new Result();
    }

    @PostMapping("/update")
    public Result update(@RequestBody Order order) {
        orderService.update(order);
        return new Result();
    }

    @GetMapping("/delete")
    public Result delete(String id) {
        orderService.delete(id);
        return new Result();
    }

    //添加订单
    @PostMapping("/addOrderAndItem")
    public Result addOrderAndItem(@RequestBody OrderAndItem orderAndItem) {
        orderService.addOrderAndItem(orderAndItem);
        return new Result();
    }

    //发货
    @PostMapping("/editConsignStatus")
    public Result editConsignStatus(@RequestBody Map<String, String> map) {
        orderService.editConsignStatus(map);
        return new Result();
    }

    //订单合并
    @PostMapping("/merge")
    public Result merge(String id1,String id2){
        orderService.merge(id1, id2);
        return  new Result();
    }
    //订单拆分
    @PostMapping("/split")
    public Result split(String id1, Integer num){
        orderService.spilt(id1, num);
        return new Result();
    }



}
