package com.heitaoshu.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.heitaoshu.pojo.order.Order;
import com.heitaoshu.service.order.OrderService;
import com.heitaoshu.service.order.WeixinPayService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference
    private OrderService orderService;
    @Reference
    private WeixinPayService weixinPayService;

    @GetMapping("/createNative")
    public Map createNative(String orderId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Order order = orderService.findById(orderId);
        if (order != null) {
            if ("0".equals(order.getPayStatus()) && "0".equals(order.getOrderStatus()) && username.equals(order.getUsername())) {
                return weixinPayService.createNative(orderId, order.getPayMoney(), "http://hetao.easy.echosite.cn/pay/notify.xxx");
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @GetMapping("/notify")
    public Map notifyLogic(HttpServletRequest request) {
        System.out.println("支付成功回调。。。。");
        InputStream inStream;
        try {
            inStream = request.getInputStream();
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1){
                outSteam.write(buffer, 0, len);
            }
            outSteam.close();
            inStream.close();
            String result = new String(outSteam.toByteArray(), "utf-8");
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap();
    }
}
