package org.SecondImage.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.SecondImage.reggie.entry.ShoppingCart;

public interface ShoppingCartService extends IService<ShoppingCart> {
    public void clean();
}
