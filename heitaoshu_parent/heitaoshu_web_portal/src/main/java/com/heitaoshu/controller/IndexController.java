package com.heitaoshu.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.heitaoshu.pojo.business.Ad;
import com.heitaoshu.pojo.system.LoginLog;
import com.heitaoshu.service.business.AdService;
import com.heitaoshu.service.goods.CategoryService;
import com.heitaoshu.service.system.LoginLogService;
import com.heitaoshu.util.WebUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/portal")
public class IndexController {
    @Reference
    private AdService adService;
    @Reference
    private CategoryService categoryService;

    @Reference
    private LoginLogService loginLogService;
    @GetMapping("index")
    public String index(Model model, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        //写日志

//        System.out.println("登录成功，我要在这里写日志！");
//        LoginLog loginLog = new LoginLog();
//        loginLog.setLoginName("前端");
//        loginLog.setIp(httpServletRequest.getRemoteAddr());
//        loginLog.setLoginTime(new Date());
//        String agent = httpServletRequest.getHeader("user-agent");
//        loginLog.setBrowserName(WebUtil.getBrowserName(agent));
//        loginLog.setLocation(WebUtil.getCityByIP(loginLog.getIp()));
//        loginLogService.add(loginLog);

        List<Ad> adList = adService.findByPosition("web_index_lb");
        model.addAttribute("lbt",adList);
        //设置栏目
        List<Map> categoryList =  categoryService.categories();
        model.addAttribute("categoryList",categoryList);
        return "index";
    }

}
