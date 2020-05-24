package com.heitaoshu.service.impl;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.heitaoshu.dao.OrderConfigMapper;
import com.heitaoshu.dao.OrderItemMapper;
import com.heitaoshu.dao.OrderLogMapper;
import com.heitaoshu.dao.OrderMapper;
import com.heitaoshu.entity.PageResult;
import com.heitaoshu.pojo.order.*;
import com.heitaoshu.service.goods.SkuService;
import com.heitaoshu.service.order.CartService;
import com.heitaoshu.service.order.OrderService;
import com.heitaoshu.util.IdWorker;
import org.aspectj.weaver.ast.Or;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service(interfaceClass = OrderService.class)
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 返回全部记录
     * @return
     */
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Order> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Order> orders = (Page<Order>) orderMapper.selectAll();
        return new PageResult<Order>(orders.getTotal(),orders.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Order> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return orderMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Order> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Order> orders = (Page<Order>) orderMapper.selectByExample(example);
        return new PageResult<Order>(orders.getTotal(),orders.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public Order findById(String id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     *
     * @param order
     */
    @Autowired
    private CartService cartService;
    @Reference
    private SkuService skuService;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Transactional
    public Map add(Order order) {
        //获取购物车选中的信息
        List<Map> cartList = cartService.getCart(order.getUsername());
        List<OrderItem> orderItems = cartList.stream().filter(map -> ((boolean)map.get("check"))).map(map -> (OrderItem)map.get("item")).collect(Collectors.toList());
        if(! skuService.reduceNum(orderItems)){
            throw new RuntimeException("订单生成失败");
        }
        try{
            //添加订单主表
            order.setId(idWorker.nextId()+"");
            order.setTotalNum(orderItems.stream().mapToInt(OrderItem::getNum).sum());
            order.setTotalMoney(orderItems.stream().mapToInt(OrderItem::getMoney).sum());
            order.setPayMoney(orderItems.stream().mapToInt(OrderItem::getPayMoney).sum());
            order.setPreMoney(order.getTotalMoney() - order.getPayMoney());

            //创建日期
            order.setCreateTime(new Date());
            order.setUpdateTime(new Date());
            order.setOrderStatus("0");
            order.setPayStatus("0");
            order.setConsignStatus("0");

            orderMapper.insert(order);

            int a = 1/0;
            //订单明细表
            for(OrderItem orderItem : orderItems){
                orderItem.setId(idWorker.nextId() + "");
                orderItem.setOrderId(order.getId());
                orderItemMapper.insert(orderItem);
            }
        } catch (Exception e){
            //更新订单主表id
            List<OrderItem> newOrderItem = new ArrayList<>();
            for(OrderItem orderItem : orderItems){
                orderItem.setOrderId(idWorker.nextId() + "");
                newOrderItem.add(orderItem);
            }
            rabbitTemplate.convertAndSend("","queue_skuback", JSON.toJSONString(orderItems));
            throw new RuntimeException("订单生成失败！");
        }


        //清除选中的购物车
        cartService.delCart(order.getUsername());
        //返回订单号和支付金额
        Map map = new HashMap();
        map.put("orderId",order.getId());
        map.put("payMoney",order.getPayMoney());
        return map;
    }

    /**
     * 修改
     * @param order
     */
    public void update(Order order) {
        orderMapper.updateByPrimaryKeySelective(order);
    }

    /**
     *  删除
     * @param id
     */
    public void delete(String id) {
        orderMapper.deleteByPrimaryKey(id);
    }


    /**
     * 添加订单和明细
     */
    @Transactional
    public void addOrderAndItem(OrderAndItem orderAndItem) {
        //当前时间
        Date date = new Date();

        Order order = orderAndItem.getOrder();
        OrderItem orderItem = orderAndItem.getOrderItem();

        order.setId(idWorker.nextId()+"");
        order.setCreateTime(date);
        order.setUpdateTime(date);
        order.setOrderStatus("0");
        order.setPayStatus("0");
        order.setConsignStatus("0");
        order.setIsDelete("0");
        //价格计算
        order.setTotalMoney(orderItem.getNum()*orderItem.getPrice());
        order.setPayMoney(order.getTotalMoney() - order.getPreMoney());

        orderMapper.insert(order);

        //订单明细
        orderItem.setOrderId(order.getId());
        orderItem.setMoney(order.getTotalMoney());
        orderItem.setPayMoney(order.getPayMoney());
        orderItem.setIsReturn("0");
        orderItemMapper.insert(orderItem);
    }


    /**
     * 订单发货
     */

    @Autowired
    private OrderLogMapper orderLogMapper;
    @Transactional
    public void editConsignStatus(Map<String, String> map) {
        if(map.get("ids") != null){
            Date date = new Date();
            Order order = new Order();
            order.setUpdateTime(date);
            order.setConsignStatus("1");
            order.setOrderStatus("1");

            Example example = new Example(Order.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andIn("id", Arrays.asList(map.get("ids")));
            criteria.andEqualTo("payStatus","1");
            criteria.andEqualTo("orderStatus","0");
            criteria.andEqualTo("isDelete","0");
            orderMapper.updateByExample(order,example);

            //快递信息


            //日志记录
            OrderLog orderLog = new OrderLog();
            orderLog.setId((idWorker.nextId()) + "");
            orderLog.setOperater("谢鹏飞");
            orderLog.setOrderId(order.getId());
            orderLog.setOperateTime(date);
            orderLog.setOrderStatus(order.getOrderStatus());
            orderLog.setConsignStatus(order.getConsignStatus());
            orderLog.setRemarks(map.get("remarks"));
            orderLogMapper.insert(orderLog);
        }
    }

    /**
     * 订单超时
     */

    @Autowired
    private OrderConfigMapper orderConfigMapper;

    @Override
    public void orderTimeOut() {
        OrderConfig orderConfig = orderConfigMapper.selectByPrimaryKey("1");
        Integer orderTimeout = orderConfig.getOrderTimeout();
        LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(orderTimeout);

        //查找超时的订单
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andLessThan("createTime",localDateTime);
        criteria.andEqualTo("orderStatus","0");
        criteria.andEqualTo("isDelete","0");

        List<Order> orderList = orderMapper.selectByExample(example);
        for(Order order:orderList){
            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId()+"");
            orderLog.setOperater("system");
            orderLog.setOperateTime(new Date());
            orderLog.setOrderStatus("4");
            orderLog.setPayStatus(order.getPayStatus());
            orderLog.setConsignStatus(order.getConsignStatus());
            orderLog.setOrderId(order.getId());
            orderLogMapper.insert(orderLog);
            //更改订单状态
            order.setOrderStatus("4");
            order.setCloseTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }
    }

    /**
     * 订单合并
     */

    @Transactional
    public void merge(String id1, String id2) {
        Order order1 = orderMapper.selectByPrimaryKey(id1);
        Order order2 = orderMapper.selectByPrimaryKey(id2);

        order1.setTotalNum(order1.getTotalNum() + order2.getTotalNum());
        order1.setTotalMoney(order1.getTotalMoney() + order2.getTotalMoney());
        order1.setPreMoney(order1.getPreMoney() + order2.getPreMoney());
        order1.setPayMoney(order1.getPayMoney() + order2.getPayMoney());
        order1.setUpdateTime(new Date());

        orderMapper.updateByPrimaryKeySelective(order1);

        //修改从订单详细到主订单
        Example example = new Example(OrderItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderId",id2);
        OrderItem orderItem = orderItemMapper.selectOneByExample(example);
        orderItem.setOrderId(order1.getId());
        orderItemMapper.updateByPrimaryKeySelective(orderItem);

        //删除从表
        orderMapper.deleteByPrimaryKey(id2);
    }

    /**
     * 订单拆分
     */
    @Transactional
    public void spilt(String id, Integer num) {

        Date date = new Date();
        //获得订单和详细
        Order order1 = orderMapper.selectByPrimaryKey(id);
        if(order1.getTotalNum() <= num || num < 1){
            throw new RuntimeException("超出订单数量");
        }
        //获得单价
        Integer price = order1.getTotalMoney() / order1.getTotalNum();
                //减主表
        order1.setUpdateTime(date);
        order1.setTotalNum(order1.getTotalNum() - num);
        order1.setTotalMoney(order1.getTotalNum() * price);
        order1.setPayMoney(order1.getTotalMoney() - order1.getPreMoney());
        orderMapper.updateByPrimaryKeySelective(order1);

        //从表
        Order order2 = order1;
        order2.setId(idWorker.nextId()+"");
        order2.setUpdateTime(date);
        order2.setCreateTime(date);
        order2.setTotalNum(num);
        order2.setTotalMoney(num * price);
        order2.setPreMoney(0);
        order2.setPayMoney(order2.getPayMoney());
        orderMapper.updateByPrimaryKeySelective(order2);
    }


    @Override
    @Transactional
    public void updatePayStatus(String orderId, String transactionId) {
        System.out.println("调用修改订单状态");
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(order!=null && "0".equals( order.getPayStatus() )){
            //修改订单状态等信息
            order.setPayStatus("1");//支付状态
            order.setOrderStatus("1");//订单状态
            order.setUpdateTime(new Date());//修改日期
            order.setPayTime(new Date());//支付日期
            order.setTransactionId(transactionId);//交易流水号
            orderMapper.updateByPrimaryKeySelective(order);//修改

            //记录订单日志
            OrderLog orderLog=new OrderLog();
            orderLog.setId( idWorker.nextId()+"" );
            orderLog.setOperater("system");//系统
            orderLog.setOperateTime(new Date());//操作时间
            orderLog.setOrderStatus("1");//订单状态
            orderLog.setPayStatus("1");//支付状态
            orderLog.setRemarks("支付流水号："+transactionId);//备注
            orderLog.setOrderId(orderId);
            orderLogMapper.insert(orderLog);

        }


    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 订单id
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andLike("id","%"+searchMap.get("id")+"%");
            }
            // 支付类型，1、在线支付、0 货到付款
            if(searchMap.get("payType")!=null && !"".equals(searchMap.get("payType"))){
                criteria.andLike("payType","%"+searchMap.get("payType")+"%");
            }
            // 物流名称
            if(searchMap.get("shippingName")!=null && !"".equals(searchMap.get("shippingName"))){
                criteria.andLike("shippingName","%"+searchMap.get("shippingName")+"%");
            }
            // 物流单号
            if(searchMap.get("shippingCode")!=null && !"".equals(searchMap.get("shippingCode"))){
                criteria.andLike("shippingCode","%"+searchMap.get("shippingCode")+"%");
            }
            // 用户名称
            if(searchMap.get("username")!=null && !"".equals(searchMap.get("username"))){
                criteria.andLike("username","%"+searchMap.get("username")+"%");
            }
            // 买家留言
            if(searchMap.get("buyerMessage")!=null && !"".equals(searchMap.get("buyerMessage"))){
                criteria.andLike("buyerMessage","%"+searchMap.get("buyerMessage")+"%");
            }
            // 是否评价
            if(searchMap.get("buyerRate")!=null && !"".equals(searchMap.get("buyerRate"))){
                criteria.andLike("buyerRate","%"+searchMap.get("buyerRate")+"%");
            }
            // 收货人
            if(searchMap.get("receiverContact")!=null && !"".equals(searchMap.get("receiverContact"))){
                criteria.andLike("receiverContact","%"+searchMap.get("receiverContact")+"%");
            }
            // 收货人手机
            if(searchMap.get("receiverMobile")!=null && !"".equals(searchMap.get("receiverMobile"))){
                criteria.andLike("receiverMobile","%"+searchMap.get("receiverMobile")+"%");
            }
            // 收货人地址
            if(searchMap.get("receiverAddress")!=null && !"".equals(searchMap.get("receiverAddress"))){
                criteria.andLike("receiverAddress","%"+searchMap.get("receiverAddress")+"%");
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if(searchMap.get("sourceType")!=null && !"".equals(searchMap.get("sourceType"))){
                criteria.andLike("sourceType","%"+searchMap.get("sourceType")+"%");
            }
            // 交易流水号
            if(searchMap.get("transactionId")!=null && !"".equals(searchMap.get("transactionId"))){
                criteria.andLike("transactionId","%"+searchMap.get("transactionId")+"%");
            }
            // 订单状态
            if(searchMap.get("orderStatus")!=null && !"".equals(searchMap.get("orderStatus"))){
                criteria.andLike("orderStatus","%"+searchMap.get("orderStatus")+"%");
            }
            // 支付状态
            if(searchMap.get("payStatus")!=null && !"".equals(searchMap.get("payStatus"))){
                criteria.andLike("payStatus","%"+searchMap.get("payStatus")+"%");
            }
            // 发货状态
            if(searchMap.get("consignStatus")!=null && !"".equals(searchMap.get("consignStatus"))){
                criteria.andLike("consignStatus","%"+searchMap.get("consignStatus")+"%");
            }
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andLike("isDelete","%"+searchMap.get("isDelete")+"%");
            }

            // 数量合计
            if(searchMap.get("totalNum")!=null ){
                criteria.andEqualTo("totalNum",searchMap.get("totalNum"));
            }
            // 金额合计
            if(searchMap.get("totalMoney")!=null ){
                criteria.andEqualTo("totalMoney",searchMap.get("totalMoney"));
            }
            // 优惠金额
            if(searchMap.get("preMoney")!=null ){
                criteria.andEqualTo("preMoney",searchMap.get("preMoney"));
            }
            // 邮费
            if(searchMap.get("postFee")!=null ){
                criteria.andEqualTo("postFee",searchMap.get("postFee"));
            }
            // 实付金额
            if(searchMap.get("payMoney")!=null ){
                criteria.andEqualTo("payMoney",searchMap.get("payMoney"));
            }

        }
        return example;
    }

}
