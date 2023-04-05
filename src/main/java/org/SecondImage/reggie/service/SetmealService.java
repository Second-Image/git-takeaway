package org.SecondImage.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.SecondImage.reggie.dto.SetmealDto;
import org.SecondImage.reggie.entry.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 添加
     * @param setmealDto
     */
    public void addWithDish(SetmealDto setmealDto);

    /**
     * 批量删除
     * @param ids
     */
    public void deleteWithDish(List<Long> ids);

    /**
     * 根据ID获取数据
     * @param id
     * @return
     */
    public SetmealDto getByIdWithDish(Long id);

    /**
     * 修改
     * @param setmealDto
     */
    public void updateWithDish(SetmealDto setmealDto);
}
