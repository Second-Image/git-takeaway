package org.SecondImage.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.SecondImage.reggie.entry.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {

}