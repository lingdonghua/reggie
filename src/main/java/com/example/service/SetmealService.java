package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.Setmeal;
import com.example.dto.SetmealDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface SetmealService extends IService<Setmeal> {

     void saveBySetmeal(SetmealDto setmealDto);

    SetmealDto updateByData(Long id);

     void  updateForSetmeal( SetmealDto setmealDto);
}
