package com.heitaoshu.controller.task;

import com.alibaba.dubbo.config.annotation.Reference;
import com.heitaoshu.service.goods.StockBackService;
import com.heitaoshu.service.order.CategoryReportService;
import com.heitaoshu.service.order.OrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class OrderTask {
    @Reference
    private OrderService orderService;
    @Scheduled(cron = "0 0/2 * * * ?")
    public void orderTimeOut(){
        System.out.println("刷新订单.....");
        orderService.orderTimeOut();
    }

    @Reference
    private CategoryReportService categoryReportService;
    @Scheduled(cron = "0 0 1 * * ?")
    public void setCategoryReport(){
        LocalDate localDate = LocalDate.now().minusDays(1);
        System.out.println("生成" + localDate + "的报表.....");
        System.out.println(categoryReportService.getYesterdayReport(localDate));
        categoryReportService.setYesterdayReport(localDate);
        System.out.println("生成结束");
    }

    /**
     * 库存回滚
     */
    @Reference
    private StockBackService stockBackService;
    @Scheduled(cron = "0 0/1 * * * ?")
    public void doBack(){
        System.out.println("开始回滚sku...");
        stockBackService.doBack();
        System.out.println("回滚结束...");
    }
}
