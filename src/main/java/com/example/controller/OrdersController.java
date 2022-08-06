package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.BaseContext;
import com.example.common.R;
import com.example.domain.OrderDetail;
import com.example.domain.Orders;
import com.example.domain.ShoppingCart;
import com.example.dto.OrdersDto;
import com.example.service.OrderDetailService;
import com.example.service.OrdersService;
import com.example.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrdersController {

    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ShoppingCartService shoppingCartService;


    /**
     * 提交订单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submitOrder(@RequestBody Orders orders){
        ordersService.saveByOrder(orders);
      return R.success("下单成功");
    }

    /**
     * 查询订单
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> selectByPage(Integer page, Integer pageSize, String number, String beginTime,String endTime){
        log.info("page:"+page.toString()+"pagesize:"+pageSize.toString());
        Page<Orders> page1=new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> wrapper =new LambdaQueryWrapper<>();
        wrapper.like(Strings.isNotEmpty(number),Orders::getNumber,number);
        //时间的比较：
        if(beginTime!=null&&endTime!=null){
            wrapper.ge(Orders::getOrderTime,beginTime);
            wrapper.le(Orders::getOrderTime,endTime);
        }
        wrapper.orderByDesc(Orders::getCheckoutTime);
        ordersService.page(page1,wrapper);
        return R.success(page1);
    }
    //订单管理
    @Transactional
    @GetMapping("/userPage")
    public R<Page> userPage(int page,int pageSize){
        //构造分页构造器
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        Page<OrdersDto> ordersDtoPage = new Page<>();

        //构造条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        //添加排序条件
        queryWrapper.orderByDesc(Orders::getOrderTime);

        //进行分页查询
        ordersService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,ordersDtoPage,"records");

        List<Orders> records=pageInfo.getRecords();

        List<OrdersDto> list = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();

            BeanUtils.copyProperties(item, ordersDto);
            Long Id = item.getId();
            //根据id查分类对象
            Orders orders = ordersService.getById(Id);
            String number = orders.getNumber();
            LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(OrderDetail::getOrderId,number);
            List<OrderDetail> orderDetailList = orderDetailService.list(lambdaQueryWrapper);
            int num=0;

            for(OrderDetail l:orderDetailList){
                num+=l.getNumber().intValue();
            }

            ordersDto.setSumNum(num);
            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(list);

        return R.success(ordersDtoPage);
    }

    @PutMapping
    public R<String> changeStatus(@RequestBody Orders orders){
        log.info(orders.toString());
        LambdaUpdateWrapper<Orders> wrapper=new LambdaUpdateWrapper<>();
        wrapper.eq(orders.getId()!=null,Orders::getId,orders.getId());
        wrapper.set(Orders::getStatus,orders.getStatus());
        ordersService.update(wrapper);
        return R.success("更改成功");
    }

    /**
     * 再来一单
     * @param order1
     * @return
     */
    //再来一单
    @Transactional
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders order1){
        //取得orderId
        Long id = order1.getId();
//        Orders orders = ordersService.getById(id);
        LambdaQueryWrapper<OrderDetail> wrapper =new LambdaQueryWrapper<>();
        wrapper.eq(id!=null,OrderDetail::getOrderId,id);
        List<OrderDetail> orderDetails = orderDetailService.list(wrapper);
        List<ShoppingCart> shoppingCarts=new ArrayList<>();
        for (OrderDetail orderDetail : orderDetails) {
            ShoppingCart shoppingCart=new ShoppingCart();
            shoppingCart.setName(orderDetail.getName());
            shoppingCart.setImage(orderDetail.getImage());
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setSetmealId(orderDetail.getSetmealId());
            shoppingCart.setNumber(orderDetail.getNumber());
            shoppingCart.setAmount(orderDetail.getAmount());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setDishFlavor(orderDetail.getDishFlavor());
            shoppingCarts.add(shoppingCart);
        }
        shoppingCartService.saveBatch(shoppingCarts);
        return R.success("再来一单成功");

    }


}
