package com.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.Employee;
import com.example.common.R;
import com.example.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 员工管理
 */
@RestController
@ResponseBody
@RequestMapping("/employee")
@Slf4j
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request ,@RequestBody Employee employee){
       // System.out.println("-----------");
//        System.out.println(employee);
        R<Employee> login = employeeService.login(employee.getUsername(), employee.getPassword());
        if(login.getCode()==1){
            //System.out.println(login.getData());
            request.getSession().setAttribute("employee",login.getData());
        }
        return login;
    }

    /**
     * 退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //删除session
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工"+employee);
        //h5密码加密，初始密码为12345
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //设置创造和修改时间
        /**
         * 注释掉的这段有mybatis提供的MetaObjectHandler字段字段填充来实现
         */
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
        //获取当前操作者的id
//        Employee employee1 = (Employee) request.getSession().getAttribute("employee");
//        Long id = employee1.getId();
//        employee.setUpdateUser(id);
//        employee.setCreateUser(id);
        employeeService.save(employee);
        return R.success("添加成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param employee
     * @return
     */
    @GetMapping("/page")
    public R<Page> selectByPage(Integer page, Integer pageSize, Employee employee){
        Page<Employee> employeeIPage =(Page<Employee>) employeeService.selectByPage(page, pageSize, employee);
        System.out.println(employeeIPage.getTotal());
        return R.success(employeeIPage);
    }

    /**
     * 修改（数据回显）
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        Employee byId = employeeService.getById(id);
        if(byId!=null){
            return R.success(byId);
        }
        return R.error("操作失败");
    }

    /**
     * 修改
     */
    @PutMapping
    public R<String> Update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());
//        Employee employee1 = (Employee) request.getSession().getAttribute("employee");
//        employee.setUpdateUser(employee1.getId());
//        employee.setUpdateTime(LocalDateTime.now());
        boolean b = employeeService.updateById(employee);
        return(b? R.success("修改成功"): R.error("修改失败"));
    }













}
