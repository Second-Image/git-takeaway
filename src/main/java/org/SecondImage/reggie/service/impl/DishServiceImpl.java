package org.SecondImage.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.SecondImage.reggie.common.CustomException;
import org.SecondImage.reggie.dto.DishDto;
import org.SecondImage.reggie.entry.Dish;
import org.SecondImage.reggie.entry.DishFlavor;
import org.SecondImage.reggie.entry.Setmeal;
import org.SecondImage.reggie.entry.SetmealDish;
import org.SecondImage.reggie.mapper.DishMapper;
import org.SecondImage.reggie.service.DishFlavorService;
import org.SecondImage.reggie.service.DishService;
import org.SecondImage.reggie.service.SetmealDishService;
import org.SecondImage.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 保存菜品和菜品口味
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //会自动识别多余字段
        this.save(dishDto);
        //给所有菜品口味对象的dishId赋值
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor df: flavors) {
            df.setDishId(dishId);
        }
        //批量保存
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * (批量)删除菜品信息
     * @param ids
     */
    @Override
    public void deleteWithFlavor(List<Long> ids) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids!=null,Dish::getId,ids);
        queryWrapper.eq(Dish::getStatus,1);
        int count = this.count(queryWrapper);
        if (count > 0){
            throw new CustomException("要删除的菜品中有正在启用的菜品");
        }
        this.removeByIds(ids);

        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(lambdaQueryWrapper);
    }

    /**
     * 根据ID查询菜品及口味
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        Dish dishId = this.getById(id);
        DishDto dishDto = new DishDto();

        BeanUtils.copyProperties(dishId,dishDto);

        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);

        dishDto.setFlavors(list);

        return dishDto;
    }

    /**
     * 修改菜品信息
     * @param dishDto
     */
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //修改菜品数据
        this.updateById(dishDto);
        //修改菜品口味数据
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors){
            flavor.setDishId(dishDto.getId());
        }
        dishFlavorService.saveBatch(flavors);//批量保存
    }

    /**
     * (批量)修改菜品状态，同时修改含有该菜品的套餐状态
     * @param status
     * @param ids
     */
    @Override
    @Transactional
    public void updateStatus(Integer status, List<Long> ids) {
        LambdaQueryWrapper<Dish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(ids != null,Dish::getId,ids);
        List<Dish> list1 = this.list(queryWrapper1);
        for (Dish dish : list1){
            if(dish != null) {
                dish.setStatus(status);
                this.updateById(dish);
            }
        }

    }
}
