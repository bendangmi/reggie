package com.bdm.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bdm.reggie.common.R;
import com.bdm.reggie.entity.Employee;
import com.bdm.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @code Description
 * @code author 本当迷
 * @code date 2022/8/3-11:15
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        /**
         * 1.将页面提交的密码 password 进行MD5加密处理
         * 2.根据页面提交的用户名username 查询数据库
         * 3.如果没有查询到则返回登录失败结果
         * 4.密码对比，如果不一致则返回登录失败结果
         * 5.查看员工状态，如果已为禁用状态，则返回员工禁用结果
         * 6.登录成功，将员工 id 存入Session并返回登录成功结果
         */

        // 1.将页面提交的密码 password 进行MD5加密处理
        String password = employee.getPassword();
        password =  DigestUtils.md5DigestAsHex(password.getBytes());

        // 2.根据页面提交的用户名username 查询数据库
        final LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        final Employee emp = employeeService.getOne(queryWrapper);


        // 3.如果没有查询到则返回登录失败结果
        if(emp == null){
            return R.error("登录失败，用户不存在！");
        }

        // 4.密码对比，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return R.error("登录失败，密码错误！");
        }

        // 5.查看员工状态，如果已为禁用状态，则返回员工禁用结果
        if(emp.getStatus() == 0){
            return R.error("登录失败！账号已经被禁用！");
        }

        // 6.登录成功，将员工 id 存入Session并返回登录成功结果
        if(emp.getUsername().equals(employee.getUsername()) && emp.getPassword().equals(password)){
            System.out.println("登录成功");
            request.getSession().setAttribute("employee", emp.getId());
            return R.success(emp);
        }

        return null;
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> login(HttpServletRequest request){
        // 清除Session中保存的当前登录员工id
        request.getSession().removeAttribute("employee");

        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        log.info("新增员工，员工信息{}", employee.toString());

        // 设置初始密码123456，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        // 设置创建时间和更新时间
        // employee.setCreateTime(LocalDateTime.now());
        // employee.setUpdateTime(LocalDateTime.now());

        // 获取当前登录用户id，作为创建用户的人
        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);

        final boolean save = employeeService.save(employee);
        if (save) return R.success("保存成功");
        return R.error("保存失败");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page( int page,  int pageSize, String name ){
        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);

        // 构造分页构造器
        final Page pageInfo = new Page(page, pageSize);
        // 构造条件构造器
        final LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        // 添加过滤条件
        queryWrapper.like(name != null, Employee::getName, name);
        // 添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        // 执行查询
        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public R<String>update(HttpServletRequest httpServletRequest, @RequestBody Employee employee){

        final long id = Thread.currentThread().getId();
        log.info("线程id为：{}", id);

        // 设置更新时间
        // employee.setUpdateTime(LocalDateTime.now());
        // 设置更新人
        // Long empId = (Long) httpServletRequest.getSession().getAttribute("employee");
        // employee.setUpdateUser(empId);

        // 更新操作
        final boolean check = employeeService.updateById(employee);
        if(check) return R.success("员工信息修改成功！");
        return R.success("未知错误");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee>getById(@PathVariable Long id){
        log.info("根据id查询员工信息。。。");
        final Employee employee = employeeService.getById(id);
        if(employee != null){
            return R.success(employee);
        }
        return R.error("没有查询到此员工信息！");
    }






}
