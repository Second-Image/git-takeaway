package org.SecondImage.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.SecondImage.reggie.entry.DishFlavor;
import org.SecondImage.reggie.mapper.DishFlavorMapper;
import org.SecondImage.reggie.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
