package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.R;
import com.example.dao.SetmealDao;
import com.example.domain.Setmeal;
import com.example.domain.SetmealDish;
import com.example.dto.SetmealDto;
import com.example.service.SetmealDishService;
import com.example.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealDao, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;


    /**
     * 保存数据
     * @param setmealDto
     */
    @Transactional
    public void saveBySetmeal(SetmealDto setmealDto){
        //保存数据到setemeal表
        this.save(setmealDto);
        //is_delect
        //保存到SetmealDish表
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealDto.getId());
        }
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 修改（数据回显）
     * @param id
     * @return
     */
    public SetmealDto updateByData(@PathVariable Long id){
        SetmealDto setmealDto=new SetmealDto();
        Setmeal setmeal = this.getById(id);
        Long setmealId = setmeal.getId();
        //对象拷贝
        BeanUtils.copyProperties(setmeal,setmealDto);
        //查询套餐关联表
        LambdaQueryWrapper<SetmealDish> lqw=new LambdaQueryWrapper<>();
        lqw.eq(setmealId!=null,SetmealDish::getSetmealId,setmealId);
        List<SetmealDish> list = setmealDishService.list(lqw);
        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }

    /**
     * 修改
     * @param setmealDto
     */
    @Transactional
    public void  updateForSetmeal(SetmealDto setmealDto){
        //setmeal表直接update即可
        this.updateById(setmealDto);
        //// 对于套餐集合表，只能先删除原来的，再添加新修改的，因为覆盖修改的话可能会添加新菜品，无法修改
        LambdaQueryWrapper<SetmealDish> lqw=new LambdaQueryWrapper<>();
        lqw.eq(setmealDto.getId()!=null,SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(lqw);
        //再添加
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealDto.getId());
        }
        setmealDishService.saveBatch(setmealDishes);
    }

}
