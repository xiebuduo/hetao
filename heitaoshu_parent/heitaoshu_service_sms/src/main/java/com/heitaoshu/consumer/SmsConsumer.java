package com.heitaoshu.consumer;

import com.alibaba.fastjson.JSONObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class SmsConsumer implements MessageListener {
    @Value("${param}")
    private String param;
    @Value("${smsCode}")
    private String smsCode;
    @Autowired
    private smsUtil smsUtil;
    public void onMessage(Message message) {
        String smsBody = new String(message.getBody());

        System.out.println(smsBody);

        Map<String, String> smsMap = JSONObject.parseObject(smsBody,Map.class);

        String phone = smsMap.get("phone");
        String code = smsMap.get("code");
        param = param.replace("[value]",code);
        //发送验证码
        smsUtil.sendSms(phone,smsCode,param);
        System.out.println("手机号:" + phone + ",验证码:" + code);

    }
}
