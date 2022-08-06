package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.R;
import com.example.domain.Dish;
import com.example.domain.DishFlavor;
import com.example.dto.DishDto;
import com.example.service.DishFlavorService;
import com.example.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 添加菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info("封装的类为："+dishDto.toString());
        log.info(dishDto.getFlavors().toString());
        dishService.saveWithFlavor(dishDto);
     return R.success("添加成功");
    }

    /**
     * 分页
     */
    @GetMapping("/page")
    public R<Page<DishDto>> selectByPage(Long page ,Long pageSize,String name){
        Page<DishDto> dishDtoPage = dishService.selectByPage(page, pageSize, name);
        return R.success(dishDtoPage);
    }

    /**
     * (修改)数据回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> updateOfData(@PathVariable Long id){
        DishDto dishDto = dishService.selectForData(id);
        return R.success(dishDto);
    }

    /**
     * 修改
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
       dishService.updateByDish(dishDto);
       return R.success("添加成功");
    }

    /**
     * 停售启售
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status,Long[] ids){
        System.out.println("------"+ids[0]);
        for (Long id : ids) {
            Dish dish = dishService.getById(id);
            dish.setStatus(status);
            dishService.updateById(dish);
        }
        return R.success("修改成功");
    }

    /**
     * 删除/批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long[] ids){
        log.info("删除的Id"+ids[0].toString());
        for (Long id : ids) {
            dishService.removeById(id);
        }
        return R.success("删除成功");
    }

//    @GetMapping("/list")
//    public R<List<Dish>> selectByCategoryId(Long categoryId){
//        log.info("categoryId:"+categoryId.toString());
//        LambdaQueryWrapper<Dish> lqw=new LambdaQueryWrapper<>();
//        lqw.eq(categoryId!=null,Dish::getCategoryId,categoryId);
//        List<Dish> list = dishService.list(lqw);
//        return R.success(list);
//    }
@GetMapping("/list")
public R<List<DishDto>> selectByCategoryId(Long categoryId){
    log.info("categoryId:"+categoryId.toString());
    LambdaQueryWrapper<Dish> lqw=new LambdaQueryWrapper<>();
    lqw.eq(categoryId!=null,Dish::getCategoryId,categoryId);
    List<Dish> list = dishService.list(lqw);
    List<DishDto> dishDtoList=new ArrayList<>();
    for (Dish dish : list) {
        DishDto dishDto =new DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        Long id = dish.getId();
        LambdaQueryWrapper<DishFlavor> lqwdto=new LambdaQueryWrapper<>();
        lqwdto.eq(id!=null,DishFlavor::getDishId,id);
        List<DishFlavor> list1 = dishFlavorService.list(lqwdto);
        dishDto.setFlavors(list1);
        dishDtoList.add(dishDto);
    }
    return R.success(dishDtoList);
}
}
