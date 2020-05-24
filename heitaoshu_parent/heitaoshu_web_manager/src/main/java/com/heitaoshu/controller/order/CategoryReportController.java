package com.heitaoshu.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.heitaoshu.pojo.order.CategoryReport;
import com.heitaoshu.service.order.CategoryReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categoryReport")
public class CategoryReportController {
    @Reference
    private CategoryReportService categoryReportService;
    @GetMapping("/yesterday")
    public List<CategoryReport> yesterdayReport(){
        LocalDate localDate = LocalDate.now().minusDays(1);
        return categoryReportService.getYesterdayReport(localDate);
    }

    //获得指定范围
    @GetMapping("/getCategory1Report")
    public List<Map> getCategory1Report(String date1, String date2){
        return categoryReportService.getCategory1Report(date1, date2);
    }
}
