package org.SecondImage.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.SecondImage.reggie.common.R;
import org.SecondImage.reggie.entry.Employee;
import org.SecondImage.reggie.service.EmployeeService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登陆
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){

        //1.将页面提交的明文密码进行md5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2.根据提交的username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper); //username字段有unique约束，独一无二可以用getOne方法

        //3.判断返回结果有无该username用户
        if (emp == null){
            return R.error("用户名错误");
        }
        //4.比对数据库返回的密码和页面提交的密码
        if (!emp.getPassword().equals(password)){
            return R.error("密码错误");
        }
        //5.查询员工状态status 1为可用 0为禁用 表示已有人登陆、已禁止使用
        if (emp.getStatus() == 0){
            return R.error("该用户已禁用！");
        }

        //6.登陆成功，将员工ID存入session，并返回查询的结果emp
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出登陆
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * 给与初始密码123456，后期员工登录账号自行修改
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Employee employee){  //数据以Json形式传入，参数加@RequestBody以获取数据
        log.info("新增员工信息: {}",employee.toString());
        //md5 加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes())); //getBytes不指定编码方式默认中文编码ISO_8859_1

//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
// 该代码块弃用，方法参数HttpServletRequest request冗余，已删除。已用自动填充字段代替
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page") //非rest风格，不需要占位符，直接在方法参数中映射，参数名一一对应
    public R<Page> page(int page,int pageSize,String name){
        log.info("员工分页 page= {}, pageSize= {}, name= {}",page,pageSize,name);
        //分页构造器
        Page<Employee> pageInfo = new Page(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //按修改时间排序
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行条件查询   查询结果封装到Page对象中
        employeeService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 修改员工信息,包括启用、禁用
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info("employee: {}",employee.toString());

        Long empId = (Long) request.getSession().getAttribute("employee");

        if (employee.getUsername()=="admin" || employee.getId()==1){
            return  R.error("管理员不能被禁用！");
        }

        employee.setUpdateUser(empId);
        employee.setUpdateTime(LocalDateTime.now());

        employeeService.updateById(employee);

        return R.success("员工修改成功");
    }

    @GetMapping("/{id}")  //@PathVariable 路径变量，REST风格
    public R<Employee> getById(@PathVariable Long id){
        Employee byId = employeeService.getById(id);
        if (byId != null) {
            return R.success(byId);
        }
        return R.error("ID查询为空");
    }
}
