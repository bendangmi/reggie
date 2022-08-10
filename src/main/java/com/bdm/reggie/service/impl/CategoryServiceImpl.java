package com.bdm.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bdm.reggie.common.CustomException;
import com.bdm.reggie.entity.Category;
import com.bdm.reggie.entity.Dish;
import com.bdm.reggie.entity.Setmeal;
import com.bdm.reggie.mapper.CategoryMapper;
import com.bdm.reggie.service.CategoryService;
import com.bdm.reggie.service.DishService;
import com.bdm.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @code Description
 * @code author 本当迷
 * @code date 2022/8/5-9:18
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService; //菜品

    @Autowired
    private SetmealService setmealService; // 套餐

    /**
     * 根据id删除分类，删除之前需要进行判断
     * @param id
     */
    @Override
    public void remove(Long id) {
        final LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件，根据分类id进行查询
        dishQueryWrapper.eq(Dish::getCategoryId, id);
        final int countDish = dishService.count(dishQueryWrapper);

        // 查询当前分类是否关联了菜品，如果已经关联，抛出一个业务异常
        if(countDish > 0){
            // 已经关联菜品，抛出一个业务异常
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        // 查询当前分类是否关联套餐，如果已经关联，抛出一个业务异常
        final LambdaQueryWrapper<Setmeal> setmealQueryWrapper = new LambdaQueryWrapper<>();
        setmealQueryWrapper.eq(Setmeal::getCategoryId, id);

        final int conutSetmeal = setmealService.count(setmealQueryWrapper);
        if(conutSetmeal > 0){
            // 已经关联套餐，抛出一个业务异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }


        // 正常删除
        removeById(id);



    }
}
