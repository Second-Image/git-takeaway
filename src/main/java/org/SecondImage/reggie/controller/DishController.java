package org.SecondImage.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.SecondImage.reggie.common.R;
import org.SecondImage.reggie.dto.DishDto;
import org.SecondImage.reggie.entry.*;
import org.SecondImage.reggie.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController //表示所有方法都加上了@ResponseBody注解，把返回值转换成Json格式
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        //管理者新增菜品，清理该菜品分类下的redis缓存
        String key = "dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);
        //还需要获得与菜品关联的套餐，清除套餐的redis缓存

        return R.success("新增菜品成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        Page<Dish> pageInfo = new Page<>(page,pageSize);

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(name != null,Dish::getName,name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo,queryWrapper);

        //在Dish实体类中新增了 private String categoryName; 不参与SQL操作，用该属性存储通过categoryId查询到的菜品分类类名。已注释掉多余代码
//        Page<DishDto> dtoPage = new Page<>(page,pageSize);
//        BeanUtils.copyProperties(pageInfo,dtoPage,"records");

        List<Dish> records = pageInfo.getRecords();
//        List<DishDto> list = records.stream().map((d)-> {
//            DishDto dishDto = new DishDto();
//            BeanUtils.copyProperties(d,dishDto);
//            Long categoryId = d.getCategoryId();
//            Category category = categoryService.getById(categoryId);
//            String categoryName = category.getName();
//            dishDto.setCategoryName(categoryName);
//            return dishDto;
//        }).collect(Collectors.toList());
//
//        dtoPage.setRecords(list);
//
//        return R.success(dtoPage);

        for (Dish dish : records){
            Category category = categoryService.getById(dish.getCategoryId());
            dish.setCategoryName(category.getName());
        }
        return R.success(pageInfo);
    }

    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id){
        log.info("要修改的菜品ID： {}",id);
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品信息
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){

        dishService.updateWithFlavor(dishDto);

        //管理者修改菜品，清理某个分类下的redis缓存   如果修改菜品的分类则不行
        String key = "dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);

        return R.success("修改成功");
    }

    /**
     * 根据条件查询菜品数据
     * @param dish
     * @return
     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
//        //查询状态为1，即启售的菜品
//        queryWrapper.eq(Dish::getStatus,1);
//        //根据名字模糊查询
//        queryWrapper.like(dish.getName() != null,Dish::getName,dish.getName());
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> list = dishService.list(queryWrapper);
//
//        return R.success(list);
//    }

    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtoList = null;
        //动态构造key
        String key = "dish_"+dish.getCategoryId()+"_"+dish.getStatus();
        //先从Redis查找菜品数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        if (dishDtoList != null){
            return R.success(dishDtoList);
        }

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        //查询状态为1，即启售的菜品
        queryWrapper.eq(Dish::getStatus,1);
        //根据名字模糊查询
        queryWrapper.like(dish.getName() != null,Dish::getName,dish.getName());
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);
        dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        //如果Redis中没有菜品数据，则存入redis，保存两小时
        redisTemplate.opsForValue().set(key,dishDtoList,2, TimeUnit.HOURS);

        return R.success(dishDtoList);
    }

    /**
     * 菜品（批量）启用、禁用
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable("status") Integer status,@RequestParam List<Long> ids){
        dishService.updateStatus(status,ids);
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return R.success("菜品和含有该菜品的套餐状态已修改");
    }
}
