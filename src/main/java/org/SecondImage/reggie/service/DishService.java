package org.SecondImage.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.SecondImage.reggie.dto.DishDto;
import org.SecondImage.reggie.entry.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    /**
     * 添加
     * @param dishDto
     */
    public void saveWithFlavor(DishDto dishDto);
    /**
     * 批量删除
     * @param ids
     */
    public void deleteWithFlavor(List<Long> ids);
    /**
     * 根据ID获取数据
     * @param id
     * @return
     */
    public DishDto getByIdWithFlavor(Long id);
    /**
     * 修改
     * @param dishDto
     */
    public void updateWithFlavor(DishDto dishDto);

    /**
     * 修改菜品状态，同时修改含有该菜品的套餐状态
     * @param status
     * @param ids
     */
    public void updateStatus(Integer status,List<Long> ids);
}
