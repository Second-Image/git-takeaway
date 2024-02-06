package org.SecondImage.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.SecondImage.reggie.entry.OrderDetail;
import org.SecondImage.reggie.entry.Orders;

import java.util.List;

public interface OrderService extends IService<Orders> {

    /**
     * 用户下单
     * @param orders
     */
    public void submit(Orders orders);

    public List<OrderDetail> getOrderDetailListByOrderId(Long orderId);
}
