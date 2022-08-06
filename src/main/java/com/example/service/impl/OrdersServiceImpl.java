package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.BaseContext;
import com.example.dao.OrdersDao;
import com.example.domain.*;
import com.example.exception.BusinessException;
import com.example.service.*;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersDao, Orders> implements OrdersService {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderDetailService orderDetailService;

    @Override
    @Transactional
    public void saveByOrder(Orders orders) {
        //获取当前用户的id
        Long currentId = BaseContext.getCurrentId();
        //查询当前购物车信息
        LambdaQueryWrapper<ShoppingCart> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(currentId!=null,ShoppingCart::getUserId,currentId);
        List<ShoppingCart> list = shoppingCartService.list(wrapper);
        if(list==null){
            throw new BusinessException("购物车为空");
        }
        //获取用户数据
        User user = userService.getById(currentId);
        //获取地址信息
        Long addressBookId = orders.getAddressBookId();
        AddressBook address = addressBookService.getById(addressBookId);
        if(address==null){
            throw new BusinessException("地址有误，不能下单");
        }
        //设置订单号
        long orderId = IdWorker.getId();
        //原子整形，防止多线程情况下出来错误结果
        AtomicInteger amount = new AtomicInteger(0);

        List<OrderDetail> orderDetails=new ArrayList<>();
        //赋值
        for (ShoppingCart shoppingCart : list) {
            OrderDetail orderDetail=new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setAmount(shoppingCart.getAmount());
            orderDetail.setDishFlavor(shoppingCart.getDishFlavor());
            orderDetail.setImage(shoppingCart.getImage());
            orderDetail.setName(shoppingCart.getName());
            orderDetail.setSetmealId(shoppingCart.getSetmealId());
            amount.addAndGet(shoppingCart.getAmount().multiply(new BigDecimal(shoppingCart.getNumber())).intValue());
            orderDetails.add(orderDetail);
        }
        //保存在订单明细表中
        orderDetailService.saveBatch(orderDetails);
        //对订单表赋值，用来保存
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(currentId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(address.getConsignee());
        orders.setPhone(address.getPhone());
        orders.setAddress((address.getProvinceName() == null ? "" : address.getProvinceName())
                + (address.getCityName() == null ? "" : address.getCityName())
                + (address.getDistrictName() == null ? "" : address.getDistrictName())
                + (address.getDetail() == null ? "" : address.getDetail()));

        //保存到订单表中
        this.save(orders);
        //下单完成清空购物车
        shoppingCartService.remove(wrapper);
    }
}
