package org.SecondImage.reggie.dto;


import lombok.Data;
import org.SecondImage.reggie.entry.OrderDetail;
import org.SecondImage.reggie.entry.Orders;

import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
	
}
