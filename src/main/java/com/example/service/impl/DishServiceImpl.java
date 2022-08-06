package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.R;
import com.example.dao.DishDao;
import com.example.domain.Category;
import com.example.domain.Dish;
import com.example.domain.DishFlavor;
import com.example.dto.DishDto;
import com.example.dto.SetmealDto;
import com.example.service.CategoryService;
import com.example.service.DishFlavorService;
import com.example.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishDao, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private DishDao dishDao;
    @Autowired
    private CategoryService categoryService;
    /**
     * 保存菜品数据到两张表dish和dish_flavor
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存数据到dish表，因为dishdto继承自dish，所以直接用
        log.info("封装的类为："+dishDto.toString());
        /**
         * 妈的，刚刚自定义额save方法和调用的this.save重名了，导致栈溢出，改成saveWithFlavor就行了
         */
        this.save(dishDto);
//        dishDao.insert(dishDto);
        //保存口味信息到dish_flavor
        Long id = dishDto.getId();
        log.info("id为"+id);
        List<DishFlavor> flavors = dishDto.getFlavors();
        //把dishId赋值到flavors中
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(id);
        }
        //保存
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 菜品分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    public Page<DishDto> selectByPage(Long page ,Long pageSize,String name){
        Page<Dish> page1=new Page<>(page,pageSize);
        LambdaQueryWrapper<Dish> lqw=new LambdaQueryWrapper<>();
        lqw.like(Strings.isNotEmpty(name),Dish::getName,name);
        //按修改时间排序
        lqw.orderByDesc(Dish::getUpdateTime);
        //执行分页查询
        this.page(page1,lqw);
        log.info("查出来的dish："+page1.getRecords());
        //进行对象的拷贝
        Page<DishDto> page2=new Page<>();
        //除了records属性外，其他都拷贝
        BeanUtils.copyProperties(page1,page2,"records");
        //对dishdto中的菜名的赋值
        List<DishDto> dishDtos =new ArrayList<>();
        List<Dish> records = page1.getRecords();
        for (Dish record : records) {
            DishDto dishDto=new DishDto();
            //把查出来的dish属性拷贝到new 出来的dishdto里面
            BeanUtils.copyProperties(record,dishDto);
            //查菜品分类名称然后赋值
            Long id = record.getCategoryId();
            Category category = categoryService.getById(id);
            if(category!=null){
                String name1 = category.getName();
                dishDto.setCategoryName(name1);
            }
            dishDtos.add(dishDto);
        }
        page2.setRecords(dishDtos);
        return page2;
    }

    /**
     * (修改)数据回显
     * @param id
     * @return
     */
    public DishDto selectForData(Long id){
        DishDto dishDto=new DishDto();
        Dish dish= this.getById(id);
        BeanUtils.copyProperties(dish,dishDto);
        //查询口味表
        LambdaQueryWrapper<DishFlavor> lqw=new LambdaQueryWrapper<>();
        lqw.eq(id!=null,DishFlavor::getDishId,id);
        List<DishFlavor> list = dishFlavorService.list(lqw);
        dishDto.setFlavors(list);
        return dishDto;
    }

    /**
     *  修改菜品
     * @param dishDto
     */
    @Transactional
    public void updateByDish(DishDto dishDto){
        //菜品表直接update覆盖即可
        this.updateById(dishDto);
      // 对于口味表，只能先删除原来的，再添加新修改的，因为覆盖修改的话可能会添加新口味，无法修改
        LambdaQueryWrapper<DishFlavor> lqw=new LambdaQueryWrapper<>();
        lqw.eq(dishDto.getId()!=null,DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(lqw);
        //再新增,给dishid赋值
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishDto.getId());
        }
        dishFlavorService.saveBatch(flavors);
    }


}
