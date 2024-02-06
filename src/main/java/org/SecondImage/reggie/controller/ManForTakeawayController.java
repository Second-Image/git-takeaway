package org.SecondImage.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.SecondImage.reggie.common.DistributedLock;
import org.SecondImage.reggie.common.R;
import org.SecondImage.reggie.entry.Employee;
import org.SecondImage.reggie.entry.ManForTakeaway;
import org.SecondImage.reggie.entry.Orders;
import org.SecondImage.reggie.service.ManForTakeawayService;
import org.SecondImage.reggie.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/manForTakeaway")
public class ManForTakeawayController {
    @Autowired
    private ManForTakeawayService manForTakeawayService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 登录
     * @param request
     * @param manForTakeaway
     * @return
     */
    @PostMapping("/login")
    public R<ManForTakeaway> login(HttpServletRequest request, @RequestBody ManForTakeaway manForTakeaway){
        //1.将页面提交的明文密码进行md5加密
        String password = manForTakeaway.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2.根据提交的username查询数据库
        LambdaQueryWrapper<ManForTakeaway> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ManForTakeaway::getUsername,manForTakeaway.getUsername());
        ManForTakeaway emp = manForTakeawayService.getOne(queryWrapper); //username字段有unique约束，独一无二可以用getOne方法

        //3.判断返回结果有无该username用户
        if (emp == null){
            return R.error("用户名错误");
        }
        //4.比对数据库返回的密码和页面提交的密码
        if (!emp.getPassword().equals(password)){
            return R.error("密码错误");
        }
        //5.查询外卖员状态status 1为可用 0为禁用 表示已有人登陆、已禁止使用
        if (emp.getStatus() == 0){
            return R.error("该用户已禁用！");
        }

        //6.登陆成功，将外卖员ID存入session，并返回查询的结果emp
        request.getSession().setAttribute("manForTakeaway",emp.getId());
        return R.success(emp);
    }

    /**
     * 注册
     * @param manForTakeaway
     * @return
     */
    @PostMapping("/add")
    public R<String> save(@RequestBody ManForTakeaway manForTakeaway){  //数据以Json形式传入，参数加@RequestBody以获取数据
        log.info("注册外卖员信息: {}",manForTakeaway.toString());
        //md5 加密
        manForTakeaway.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes())); //getBytes不指定编码方式默认中文编码ISO_8859_1

        manForTakeawayService.save(manForTakeaway);

        return R.success("注册外卖员成功");
    }

    /**
     * 查询订单分页
     * @return
     */
    @GetMapping("/order/page")
    public R<Page> list(int page, int pageSize, Long number, String beginTime, String endTime){
        log.info("number: {}",number); //订单号
        log.info("beginTime: {}",beginTime);
        log.info("endTime: {}",endTime);

        Page<Orders> pageInfo = new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(number != null,Orders::getNumber,number);
        queryWrapper.ge(beginTime != null,Orders::getOrderTime,beginTime);
        queryWrapper.le(endTime != null,Orders::getOrderTime,endTime);
        queryWrapper.orderByDesc(Orders::getOrderTime);

        orderService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }
    /**
     * 订单状态修改
     * @return
     */
    @Transactional
    @PutMapping("/order")
    public R<String> update(HttpServletRequest request,@RequestBody Orders orders){
        Long manForTakeawayID = (Long) request.getSession().getAttribute("manForTakeaway");
        log.info("外卖员ID:{}",manForTakeawayID);
        LambdaQueryWrapper<ManForTakeaway> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ManForTakeaway::getId,manForTakeawayID);
        ManForTakeaway one = manForTakeawayService.getOne(queryWrapper);

        Orders byId = orderService.getById(orders.getId());

        String lockKey = "getOrder_"+orders.getId();
        //获取并发锁
        DistributedLock distributedLock = new DistributedLock(lockKey, redisTemplate);
        Boolean lock = distributedLock.trylock(3);
        if (! lock){
            return R.error("此单已被人获取，请重试");
        }
        //成功获取锁，对数据库修改
        try {
            if (byId.getStatus() == 2) {
                orders.setStatus(3);
                orders.setTakeoutMan(one.getName());
                orders.setTakeoutManId(one.getId());
                orderService.updateById(orders);
                return R.success("订单正在派送");
            } else {
                orders.setStatus(4);
                orders.setTakeoutMan(one.getName());
                orders.setTakeoutManId(one.getId());
                orderService.updateById(orders);
                return R.success("订单已完成");
            }
        }finally {
            //无论成功与否都释放锁
            distributedLock.unlock();
        }

    }
}
