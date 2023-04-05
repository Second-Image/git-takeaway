package org.SecondImage.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.SecondImage.reggie.entry.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
