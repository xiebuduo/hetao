package com.heitaoshu.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.heitaoshu.entity.PageResult;
import com.heitaoshu.entity.Result;
import com.heitaoshu.pojo.order.Preferential;
import com.heitaoshu.service.order.PreferentialService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/preferential")
public class PreferentialController {

    @Reference
    private PreferentialService preferentialService;

    @GetMapping("/findAll")
    public List<Preferential> findAll(){
        return preferentialService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult<Preferential> findPage(int page, int size){
        return preferentialService.findPage(page, size);
    }

    @PostMapping("/findList")
    public List<Preferential> findList(@RequestBody Map<String,Object> searchMap){
        return preferentialService.findList(searchMap);
    }

    @PostMapping("/findPage")
    public PageResult<Preferential> findPage(@RequestBody Map<String,Object> searchMap,int page, int size){
        return  preferentialService.findPage(searchMap,page,size);
    }

    @GetMapping("/findById")
    public Preferential findById(Integer id){
        return preferentialService.findById(id);
    }


    @PostMapping("/add")
    public Result add(@RequestBody Preferential preferential){
        preferentialService.add(preferential);
        return new Result();
    }

    @PostMapping("/update")
    public Result update(@RequestBody Preferential preferential){
        preferentialService.update(preferential);
        return new Result();
    }

    @GetMapping("/delete")
    public Result delete(Integer id){
        preferentialService.delete(id);
        return new Result();
    }

}