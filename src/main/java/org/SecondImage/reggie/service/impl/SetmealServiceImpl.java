package org.SecondImage.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.SecondImage.reggie.common.CustomException;
import org.SecondImage.reggie.dto.SetmealDto;
import org.SecondImage.reggie.entry.DishFlavor;
import org.SecondImage.reggie.entry.Setmeal;
import org.SecondImage.reggie.entry.SetmealDish;
import org.SecondImage.reggie.mapper.SetmealMapper;
import org.SecondImage.reggie.service.SetmealDishService;
import org.SecondImage.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐，保存套餐Setmeal和菜品的关联关系SetmealDish
     * @param setmealDto
     */
    @Transactional //操作多张表，要么全部操作成功，要么失败，保证事务的一致性
    public void addWithDish(SetmealDto setmealDto) {
        //保存套餐信息
        this.save(setmealDto);
        //保存套餐和菜品的关联信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for(SetmealDish setmealDish : setmealDishes){
            //获取套餐ID，赋值  先保存套餐信息再获取
            setmealDish.setSetmealId(setmealDto.getId());
        }
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * (批量)删除套餐信息Setmeal SetmealDish
     * @param ids
     */
    @Transactional
    public void deleteWithDish(List<Long> ids) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1); // 查询有没有启用的套餐
        int count = this.count(queryWrapper);
        if (count > 0){
            throw new CustomException("要删除的套餐中有正在启用的套餐");
        }
        //删除Setmeal
        this.removeByIds(ids);

        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        //删除SetmealDish
        setmealDishService.remove(lambdaQueryWrapper);
    }

    /**
     * 根据ID查找套餐信息和SetmealDish
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        Setmeal setmealId = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();

        BeanUtils.copyProperties(setmealId,setmealDto);

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        setmealDto.setSetmealDishes(list);

        return setmealDto;
    }

    /**
     * 修改套餐信息Setmeal 和 SetmealDish，包括套餐的启用、禁用
     * @param setmealDto
     */
    public void updateWithDish(SetmealDto setmealDto) {
        //修改套餐Setmeal数据
        this.updateById(setmealDto);
        //修改SetmealDish数据
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getDishId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for(SetmealDish setmealDish : setmealDishes){
            setmealDish.setSetmealId(setmealDto.getId());
        }
        //批量保存套餐关联的菜品
        setmealDishService.saveBatch(setmealDishes);
        //更新套餐
        this.updateById(setmealDto);
    }
}
