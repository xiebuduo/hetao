package com.heitaoshu.service.order;

import java.util.Map;

public interface WeixinPayService {
    public Map createNative(String orderId,Integer money,String notifyUrl);

    /**
     * 微信支付回调
     * @param xml
     */
    public void notifyLogic(String xml);


    /**
     * 查询支付结果
     * @param orderId
     * @return
     */
    public Map queryPayStatus(String orderId);


}
