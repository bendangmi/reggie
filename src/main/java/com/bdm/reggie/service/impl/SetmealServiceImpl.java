package com.bdm.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bdm.reggie.common.CustomException;
import com.bdm.reggie.common.R;
import com.bdm.reggie.dto.SetmealDto;
import com.bdm.reggie.entity.Setmeal;
import com.bdm.reggie.entity.SetmealDish;
import com.bdm.reggie.mapper.SetmealMapper;
import com.bdm.reggie.service.SetmealDishService;
import com.bdm.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @code Description
 * @code author 本当迷
 * @code date 2022/8/5-10:36
 */
@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {


    @Autowired
    SetmealDishService setmealDishService;


    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        // 保存套餐的基本信息，操作setmeal,执行Insert操作
        this.save(setmealDto);

        final List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for(int i = 0; i < setmealDishes.size(); i++){
            final SetmealDish setmealDish = setmealDishes.get(i);
            setmealDish.setSetmealId(setmealDto.getId());
            setmealDishes.set(i, setmealDish);
        }

        // 保存套餐和菜品的关联关系，操作setmeal_dish，执行insert操作
        setmealDishService.saveBatch(setmealDishes);

    }

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     *
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        // 查询套餐状态，确定是否可以删除
        final LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);
        final int count = this.count(queryWrapper);
        if(count > 0){
            // 如果不能删除，抛出一个业务异常
            throw new CustomException("套餐正在售卖中，不允许删除");
        }

        // 如果可以删除，先删除套餐表中的数据
        this.removeByIds(ids);

        // 删除关系表中的数据
        final LambdaQueryWrapper<SetmealDish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);

        setmealDishService.remove(dishLambdaQueryWrapper);

    }

}
