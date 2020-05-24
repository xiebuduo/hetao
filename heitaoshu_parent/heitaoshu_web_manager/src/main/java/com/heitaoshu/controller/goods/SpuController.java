package com.heitaoshu.controller.goods;

import com.alibaba.dubbo.config.annotation.Reference;
import com.heitaoshu.entity.PageResult;
import com.heitaoshu.entity.Result;
import com.heitaoshu.pojo.goods.Goods;
import com.heitaoshu.pojo.goods.Spu;
import com.heitaoshu.service.goods.SpuService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/spu")
public class SpuController {

    @Reference
    private SpuService spuService;

    @GetMapping("/findAll")
    public List<Spu> findAll(){
        return spuService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult<Spu> findPage(int page, int size){
        return spuService.findPage(page, size);
    }

    @PostMapping("/findList")
    public List<Spu> findList(@RequestBody Map<String,Object> searchMap){
        return spuService.findList(searchMap);
    }

    @PostMapping("/findPage")
    public PageResult<Spu> findPage(@RequestBody Map<String,Object> searchMap,int page, int size){
        return  spuService.findPage(searchMap,page,size);
    }

    @GetMapping("/findById")
    public Spu findById(String id){
        return spuService.findById(id);
    }


    @PostMapping("/add")
    public Result add(@RequestBody Spu spu){
        spuService.add(spu);
        return new Result();
    }

    @PostMapping("/update")
    public Result update(@RequestBody Spu spu){
        spuService.update(spu);
        return new Result();
    }

    @GetMapping("/delete")
    public Result delete(String id){
        spuService.delete(id);
        return new Result();
    }

    //逻辑删除复原
    @GetMapping("/resetDelete")
    public Result resetDelete(String id){
        spuService.resetDelete(id);
        return new Result();
    }

    @PostMapping("/saveGoods")
    public Result saveGoods(@RequestBody Goods goods){
        spuService.saveGoods(goods);
        return new Result();
    }

    @GetMapping("/findGoods")
    public Goods findGoods(String id){
        return spuService.findGoods(id);
    }

    //审核
    @PostMapping("/audit")
    public Result Audit(@RequestBody Map<String, String> map){
        spuService.audit(map.get("id"), map.get("Status"), map.get("message"));
        return new Result();
    }
    //下架
    @GetMapping("/pull")
    public Result pull(String id){
        spuService.pull(id);
        return new Result();
    }
    //上架
    @GetMapping("/put")
    public Result put(String id){
        spuService.put(id);
        return  new Result();
    }

    //批量上架
    @GetMapping("/putMany")
    public Result putMany(String[] ids){
    int count = spuService.putMany(ids);
    return new Result(0, "成功上架了" + count +"个商品！");
    }

}
