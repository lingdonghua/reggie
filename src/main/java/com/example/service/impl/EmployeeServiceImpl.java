package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dao.EmployeeDao;
import com.example.domain.Employee;
import com.example.common.R;
import com.example.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class EmployeeServiceImpl extends ServiceImpl<EmployeeDao, Employee> implements EmployeeService {
    @Autowired
    private EmployeeDao employeeDao;

    public R<Employee> login(String username ,String password){
        //md5加密处理
        String s = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        password=s;
        //数据库匹配用户名
        LambdaQueryWrapper<Employee> lqw=new LambdaQueryWrapper<Employee>();
        lqw.eq(Strings.isNotEmpty(username),Employee::getUsername,username);
        Employee employee = employeeDao.selectOne(lqw);
        //判断
        if(employee==null){
            return R.error("用户名不存在");
        }
        //用户名存在，判断密码
        if(!employee.getPassword().equals(password)){
            return R.error("密码错误");
        }
        //密码也正确。判断用户是否被禁用
        if(employee.getStatus()==0){
            return R.error("该用户被禁用");
        }
        //登录成功
        return R.success(employee);
    }

    @Override
    public IPage<Employee> selectByPage(Integer current, Integer size, Employee employee) {
        LambdaQueryWrapper<Employee> lqw=new LambdaQueryWrapper<Employee>();
        //模糊查询
        lqw.like(Strings.isNotEmpty(employee.getName()),Employee::getName,employee.getName());
        //分页
        IPage page =new Page(current,size);
        //排序
        lqw.orderByDesc(Employee::getUpdateTime);
        employeeDao.selectPage(page,lqw);
        return page;
    }

}
