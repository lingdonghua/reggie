package com.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.Employee;
import com.example.common.R;
import org.springframework.stereotype.Service;

@Service
public interface EmployeeService extends IService<Employee> {
    R<Employee> login(String username ,String password);
    //分页+模糊查询
    IPage<Employee> selectByPage(Integer current, Integer size, Employee employee);

}
