package org.SecondImage.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.SecondImage.reggie.entry.ShoppingCart;
import org.SecondImage.reggie.mapper.ShoppingCartMapper;
import org.SecondImage.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
