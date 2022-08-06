package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.R;
import com.example.domain.Dish;
import com.example.dto.DishDto;

public interface DishService extends IService<Dish> {
    void saveWithFlavor(DishDto dishDto);

    /**
     * 菜品分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
     Page<DishDto> selectByPage(Long page , Long pageSize, String name);

    /**
     * (修改)数据回显
     * @param id
     * @return
     */
    DishDto selectForData(Long id);

    /**
     * 修改菜品
     * @param dishDto
     */
    void updateByDish(DishDto dishDto);
}
