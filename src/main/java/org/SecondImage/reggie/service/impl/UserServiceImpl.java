package org.SecondImage.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.SecondImage.reggie.entry.User;
import org.SecondImage.reggie.mapper.UserMapper;
import org.SecondImage.reggie.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
