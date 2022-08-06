package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.common.BaseContext;
import com.example.common.R;
import com.example.domain.ShoppingCart;
import com.example.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<String> addShoppingCart(@RequestBody ShoppingCart shoppingCart){
        log.info(shoppingCart.toString());
        shoppingCart.setUserId(BaseContext.getCurrentId());
        shoppingCart.setCreateTime(LocalDateTime.now());
        LambdaUpdateWrapper<ShoppingCart> lqw=new LambdaUpdateWrapper<>();
        lqw.eq(shoppingCart.getDishId()!=null,ShoppingCart::getDishId,shoppingCart.getDishId());
        List<ShoppingCart> byId = shoppingCartService.list(lqw);
        log.info("byid:"+byId);
        //log.info("查出来的口味："+byId.getDishFlavor());
        if(byId!=null) {
            for (ShoppingCart cart : byId) {
                if(shoppingCart.getDishFlavor().equals(cart.getDishFlavor())){
                    //id和口味都相同，number+1
                    log.info("-------");
                    LambdaUpdateWrapper<ShoppingCart> lambdaUpdateWrapper=new LambdaUpdateWrapper<>();
                    lambdaUpdateWrapper.eq(cart.getId()!=null,ShoppingCart::getId,cart.getId());
                    lambdaUpdateWrapper.set(ShoppingCart::getNumber,cart.getNumber()+1);
                    shoppingCartService.update(lambdaUpdateWrapper);
                    return R.success("添加成功");
                }
            }
        }
        shoppingCartService.save(shoppingCart);
        return R.success("添加成功");
    }
    /**
     * 展示购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> getList(){
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(BaseContext.getCurrentId()!=null,ShoppingCart::getUserId,BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartService.list(lambdaQueryWrapper);
        return R.success(list);
    }

    /**
     * 购物车中减少数量
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<String> subShoppingCart(@RequestBody ShoppingCart shoppingCart){
        log.info(shoppingCart.toString());
        // 找出number
        LambdaQueryWrapper<ShoppingCart> lqw=new LambdaQueryWrapper<>();
        lqw.eq(shoppingCart.getDishId()!=null,ShoppingCart::getDishId,shoppingCart.getDishId());
        lqw.eq(Strings.isNotEmpty(shoppingCart.getDishFlavor()),ShoppingCart::getDishFlavor,shoppingCart.getDishFlavor());
        ShoppingCart one = shoppingCartService.getOne(lqw);
        if(one.getNumber()-1==0){
            //数量为0，从购物车中删除
            shoppingCartService.remove(lqw);
            return R.success("减少成功");
        }
        //更改数量
        LambdaUpdateWrapper<ShoppingCart> lambdaUpdateWrapper=new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(shoppingCart.getDishId()!=null,ShoppingCart::getDishId,shoppingCart.getDishId());
        lambdaUpdateWrapper.eq(Strings.isNotEmpty(shoppingCart.getDishFlavor()),ShoppingCart::getDishFlavor,shoppingCart.getDishFlavor());
        lambdaUpdateWrapper.set(ShoppingCart::getNumber,one.getNumber()-1);
        shoppingCartService.update(lambdaUpdateWrapper);
        return R.success("减少成功");
    }

    @DeleteMapping("/clean")
    public R<String> cleanShoppingCart(){
        LambdaUpdateWrapper<ShoppingCart> lambdaUpdateWrapper=new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(BaseContext.getCurrentId()!=null,ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(lambdaUpdateWrapper);
        return R.success("清除成功");
    }
}
