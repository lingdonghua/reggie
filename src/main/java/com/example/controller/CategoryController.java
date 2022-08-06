package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.R;
import com.example.domain.Category;
import com.example.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品分类管理
 */
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    /**
     * 新增
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("菜品分类"+category);
        categoryService.save(category);
        return R.success("添加成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page<Category>> selectByPage( Long page, Long pageSize){
        Page<Category> page1 =new Page<>(page,pageSize);
        //排序
        LambdaQueryWrapper<Category> lambdaQueryWrapper =new LambdaQueryWrapper<Category>();
        lambdaQueryWrapper.orderByAsc(Category::getSort);
        categoryService.page(page1,lambdaQueryWrapper);
        return R.success(page1);
    }

    /**
     *  修改
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info(category.toString());
        categoryService.updateById(category);
        return R.success("修改成功");
    }

    /**
     * 删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids){
        log.info(ids.toString());
         categoryService.remove(ids.toString());
         return R.success("删除成功");
    }

    /**
     * 添加菜品的下拉框回显
     * @param category
     * @return
     */
    @GetMapping("/list")
    private R<List<Category>> list(Category category){
  //      log.info("类型为："+(category.getType()==1?"菜品":"套餐"));
        LambdaQueryWrapper<Category> lqw=new LambdaQueryWrapper<>();
        lqw.eq(category.getType()!=null,Category::getType,category.getType());
        lqw.orderByAsc(Category::getSort);
        List<Category> list = categoryService.list(lqw);
        log.info(list.toString());
        return R.success(list);
    }

}
