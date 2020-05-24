package com.heitaoshu.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.heitaoshu.service.goods.SkuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/sku")
public class SkuController {
    @Reference
    private SkuService skuService;
    @GetMapping("/price")
    public Integer price(String id){
        Integer i = skuService.findPriceById(id);
        System.out.println(i);
        return skuService.findPriceById(id);
    }
}
