package org.SecondImage.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.SecondImage.reggie.common.R;
import org.SecondImage.reggie.dto.DishDto;
import org.SecondImage.reggie.dto.SetmealDto;
import org.SecondImage.reggie.entry.Category;
import org.SecondImage.reggie.entry.Setmeal;
import org.SecondImage.reggie.entry.SetmealDish;
import org.SecondImage.reggie.service.CategoryService;
import org.SecondImage.reggie.service.SetmealDishService;
import org.SecondImage.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息: {}",setmealDto);
        setmealService.addWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){

        Page<Setmeal> pageInfo = new Page<>(page,pageSize);

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null,Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo,queryWrapper);

        List<Setmeal> records = pageInfo.getRecords();

        for (Setmeal setmeal : records){
            Category byId = categoryService.getById(setmeal.getCategoryId());
            setmeal.setCategoryName(byId.getName());
        }
        return R.success(pageInfo);
    }

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("删除套餐ids: {}",ids);
        setmealService.deleteWithDish(ids);
        return R.success("套餐删除成功");
    }

    /**
     * 修改菜品信息
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDish(setmealDto);
        return R.success("修改成功");
    }
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    /**
     * 套餐（批量）启用、禁用
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable("status") Integer status,@RequestParam List<Long> ids){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids != null,Setmeal::getId,ids);
        List<Setmeal> list = setmealService.list(queryWrapper);
        for(Setmeal setmeal : list){
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }
        return R.success("套餐状态修改成功");
    }

    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }
}