package org.SecondImage.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.SecondImage.reggie.common.R;
import org.SecondImage.reggie.entry.Orders;
import org.SecondImage.reggie.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        orderService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 查询订单分页
     * @return
     */
    @GetMapping("/page")
    public R<Page> list(int page,int pageSize,Long number,String beginTime, String endTime){
        log.info("number: {}",number);
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
    @PutMapping
    public R<String> update(@RequestBody Orders orders){
        Orders byId = orderService.getById(orders.getId());
        if (byId.getStatus() == 2){
            orders.setStatus(3);
            orderService.updateById(orders);
            return R.success("订单正在派送");
        }else {
            orders.setStatus(4);
            orderService.updateById(orders);
            return R.success("订单已完成");
        }
    }
}
