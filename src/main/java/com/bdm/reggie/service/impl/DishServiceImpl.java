package com.bdm.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bdm.reggie.dto.DishDto;
import com.bdm.reggie.entity.Dish;
import com.bdm.reggie.entity.DishFlavor;
import com.bdm.reggie.mapper.DishMapper;
import com.bdm.reggie.service.DishFlavorService;
import com.bdm.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @code Description
 * @code author 本当迷
 * @code date 2022/8/5-10:33
 */
@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */
    @Override
    @Transactional // 涉及多张表，添加事务
    public void saveWithFlavor(DishDto dishDto) {
        // 保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        final Long dishId = dishDto.getId(); // 菜品id

        // 菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors =  flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item ;
        }).collect(Collectors.toList());


        // 保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        // 查询菜品的基本信息，从dish表查询
        final Dish dish = this.getById(id);

        final DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        // 查询当前菜品对应的口味信息，从dish_flavor表查询
        final LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        final List<DishFlavor> list = dishFlavorService.list(queryWrapper);

        dishDto.setFlavors(list);
        return dishDto;
    }

    /**
     * // 更新菜品信息, 同时更新对应的口味信息
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        // 更新dish表的基本信息
        this.updateById(dishDto);

        // 清理当前菜品对应的口味数据
        final LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        // 添加当前提交过来的口味数据
        // 菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors =  flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item ;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);


    }

    /**
     * 根据id删除菜品和对应的口味
     * @param id
     * @return
     */
    @Override
    @Transactional // 涉及多张表，添加事务
    public boolean deleteByIdWithFlavor(Long id) {
        final boolean remove = this.removeById(id);

        // 根据菜品id删除菜品口味
        final LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);
        final boolean remove1 = dishFlavorService.remove(queryWrapper);
        return remove1 && remove;
    }
}
