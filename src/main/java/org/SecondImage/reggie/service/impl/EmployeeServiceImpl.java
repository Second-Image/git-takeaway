package org.SecondImage.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.SecondImage.reggie.entry.Employee;
import org.SecondImage.reggie.mapper.EmployeeMapper;
import org.SecondImage.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
