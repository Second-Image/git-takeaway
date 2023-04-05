package org.SecondImage.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.SecondImage.reggie.common.CustomException;
import org.SecondImage.reggie.entry.Category;
import org.SecondImage.reggie.entry.Dish;
import org.SecondImage.reggie.entry.Setmeal;
import org.SecondImage.reggie.mapper.CategoryMapper;
import org.SecondImage.reggie.service.CategoryService;
import org.SecondImage.reggie.service.DishService;
import org.SecondImage.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    @Override
    public void remove(Long id) {
        //查询当前分类是否关联了菜品
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.eq(Dish::getCategoryId,id);
        int count1 = dishService.count(dishQueryWrapper);
        if (count1 > 0){
            //已关联菜品，抛出业务异常
            throw new CustomException("已关联 菜品，不能删除");
        }
        //查询当前分类是否关联了套餐
        LambdaQueryWrapper<Setmeal> setmealQueryWrapper = new LambdaQueryWrapper<>();
        setmealQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(setmealQueryWrapper);
        if (count2 > 0){
            //已关联套餐，抛出业务异常
            throw new CustomException("已关联 套餐，不能删除");
        }

        //都没关联，正常删除,调用父类方法，即MP自带的删除
        super.removeById(id);
    }
}
