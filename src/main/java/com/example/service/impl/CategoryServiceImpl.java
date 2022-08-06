package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dao.CategoryDao;
import com.example.domain.Category;
import com.example.domain.Dish;
import com.example.domain.Setmeal;
import com.example.exception.BusinessException;
import com.example.service.CategoryService;
import com.example.service.DishService;
import com.example.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 判断删除的分类是否关联了其他的菜品
     * @param id
     */
    public void remove(String id){

        LambdaQueryWrapper<Dish> lambdaQueryWrapper =new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count1 = dishService.count(lambdaQueryWrapper);
        if(count1>0){
            //关联到了其他的菜品，取消删除，抛异常
            throw new BusinessException("当前分类关联了菜品，不能删除");
        }
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper2 =new LambdaQueryWrapper<>();
        lambdaQueryWrapper2.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(lambdaQueryWrapper2);
        if(count2>0){
            //关联了其他菜品，继续抛异常
            throw new BusinessException("当前分类关联了菜品，不能删除");
        }
        //没有关联的菜品，可删除
        super.removeById(id);
    }

}
