package org.SecondImage.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.SecondImage.reggie.entry.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
