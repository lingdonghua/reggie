package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.R;
import com.example.domain.Category;
import com.example.domain.Dish;
import com.example.domain.Setmeal;
import com.example.domain.SetmealDish;
import com.example.dto.SetmealDto;
import com.example.service.CategoryService;
import com.example.service.SetmealDishService;
import com.example.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishService setmealDishService;


    /**
     *  新增套餐
     * @param setmealDto
     * @return
     */
    @CacheEvict(value = "setmealCache",key = "#setmealDto.categoryId+'_'+#setmealDto.status")//删除指定key的缓存数据
    @PostMapping
    public R<String> saveBySetmeal(@RequestBody SetmealDto setmealDto){
        log.info("套餐的dto为："+setmealDto.toString());
        setmealService.saveBySetmeal(setmealDto);
        return R.success("保存成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> selectByPage(Long page ,Long pageSize,String name){
        Page<Setmeal> page1=new Page<>(page,pageSize);
        LambdaQueryWrapper<Setmeal> lqw=new LambdaQueryWrapper<>();
        lqw.like(Strings.isNotEmpty(name),Setmeal::getName,name);
        lqw.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(page1,lqw);
        //对象拷贝
        Page<SetmealDto> page2=new Page<>();
        BeanUtils.copyProperties(page1,page2,"records");

        List<SetmealDto> list=new ArrayList<>();
        List<Setmeal> records = page1.getRecords();
        for (Setmeal record : records) {
            SetmealDto setmealDto=new SetmealDto();
            BeanUtils.copyProperties(record,setmealDto);
            Long categoryId = record.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                String name1 = category.getName();
                setmealDto.setCategoryName(name1);
            }
            list.add(setmealDto);
        }
        page2.setRecords(list);
        return R.success(page2);
    }
    /**
     * 停售启售
     * @param status
     * @param ids
     * @return
     */
    @CacheEvict(value = "setmealCache",allEntries = true)
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status,Long[] ids){
        for (Long id : ids) {
            Setmeal setmeal = setmealService.getById(id);
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }
        return R.success("修改成功");
    }
    /**
     * 删除/批量删除
     * @param ids
     * @return
     */
    @CacheEvict(value = "setmealCache",allEntries = true)
    @DeleteMapping
    public R<String> delete(Long[] ids){
        log.info("删除的Id"+ids[0].toString());
        for (Long id : ids) {
            setmealService.removeById(id);
        }
        return R.success("删除成功");
    }
    /**
     * 修改（数据回显）
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> updateByData(@PathVariable Long id){
        log.info("接收到的修改Id："+id.toString());
        //数据回显
        SetmealDto setmealDto = setmealService.updateByData(id);

        return R.success(setmealDto);
    }
    /**
     * 修改
     * @param setmealDto
     */
    @CacheEvict(value = "setmealCache",key = "#setmealDto.categoryId+'_'+#setmealDto.status")
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        log.info("接收到的Dto类为："+setmealDto.toString());
        setmealService.updateForSetmeal(setmealDto);
        return R.success("修改完成");
    }

    /**
     * 根据条件查询套餐数据
     * @param categoryId
     * @param status
     * @return
     */
    @Cacheable(value = "setmealCache",key = "#categoryId+'_'+#status")
    @GetMapping("/list")
    public R<List<SetmealDto>> getSetmeal(String categoryId,Integer status){
        //首先查setmeal中关联的套餐
        LambdaQueryWrapper<Setmeal> lqw=new LambdaQueryWrapper<>();
        lqw.eq(Strings.isNotEmpty(categoryId),Setmeal::getCategoryId,categoryId);
        List<Setmeal> setmealList = setmealService.list(lqw);
        List<SetmealDto> setmealDtoList =new ArrayList<>();
        for (Setmeal setmeal : setmealList) {
            SetmealDto setmealDto=new SetmealDto();
            BeanUtils.copyProperties(setmeal,setmealDto);
            Long id = setmeal.getId();
            LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(id!=null,SetmealDish::getSetmealId,id);
            List<SetmealDish> list = setmealDishService.list(lambdaQueryWrapper);
            setmealDto.setSetmealDishes(list);
            setmealDtoList.add(setmealDto);
        }
        return R.success(setmealDtoList);
    }
}
