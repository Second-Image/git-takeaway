package org.SecondImage.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.SecondImage.reggie.common.BaseContext;
import org.SecondImage.reggie.common.CustomException;
import org.SecondImage.reggie.common.R;
import org.SecondImage.reggie.entry.Dish;
import org.SecondImage.reggie.entry.Setmeal;
import org.SecondImage.reggie.entry.ShoppingCart;
import org.SecondImage.reggie.service.DishService;
import org.SecondImage.reggie.service.SetmealService;
import org.SecondImage.reggie.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    //查询添加到购物车的菜品或套餐的状态，因为可能用户在浏览时管理者将某个菜品或套餐停售了
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车数据: {}",shoppingCart);
        //设置用户ID，获取当前用户ID
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        //判断添加到购物车的是菜品还是套餐
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //查找当前用户ID的购物车
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,currentId);
        //判断
        if (dishId != null){
            lambdaQueryWrapper.eq(ShoppingCart::getDishId,dishId);
            //查询菜品状态
            Dish byId = dishService.getById(dishId);
            if (byId.getStatus() == 0){
                return R.error("该商品状态异常，请重新访问");
            }
        }else if (setmealId != null){
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId,setmealId);
            //查询套餐状态
            Setmeal byId = setmealService.getById(setmealId);
            if (byId.getStatus() == 0){
                return R.error("该商品状态异常，请重新访问");
            }
        }else{
            throw new CustomException("无菜品或套餐ID传入！");
        }
        //判断是否已存在该菜品 or 套餐
        ShoppingCart one = shoppingCartService.getOne(lambdaQueryWrapper);
        if (one != null){
            Integer number = one.getNumber();
            one.setNumber(number + 1);
            shoppingCartService.updateById(one);
        }else{
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            one = shoppingCart;
        }
        return R.success(one);
    }

    /**
     * 查询当前用户的购物车信息
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        Long currentId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        if (list == null){
            list = new ArrayList<>();
        }
        return R.success(list);
    }

    /**
     * 该修改功能是响应移动端点击 减号 按钮的动作请求
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<String> update(@RequestBody ShoppingCart shoppingCart){
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);

        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();

        if (dishId != null){
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else if (setmealId != null){
            queryWrapper.eq(ShoppingCart::getSetmealId,setmealId);
        }else {
            throw new CustomException("修改购物车商品数据异常");
        }

        ShoppingCart one = shoppingCartService.getOne(queryWrapper);
        //如果商品数量大于0
        if (one != null && one.getNumber() > 1){
            Integer number = one.getNumber();
            one.setNumber(number - 1);
            shoppingCartService.updateById(one);
        }else if (one != null && one.getNumber() == 1){
            shoppingCartService.remove(queryWrapper);
        }

        return R.success("减少成功");
    }

    @DeleteMapping("/clean")
    public R<String> delete(){
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
        shoppingCartService.remove(queryWrapper);
//        shoppingCartService.removeById(); removeById方法传入的应该是是购物车主键才行
        return R.success("已清空购物车");
    }
}
