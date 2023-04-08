package org.SecondImage.reggie.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/orderDetail")
public class OrderDetailController {
    //订单内容打印出来给商家，要有商家接口，涉及orderDetail和order两表联查
}
