package com.bdm.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bdm.reggie.dto.DishDto;
import com.bdm.reggie.entity.Dish;

/**
 * @code Description
 * @code author 本当迷
 * @code date 2022/8/5-10:32
 */
public interface DishService extends IService<Dish> {
    // 新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish、dish_flavor
    public  void saveWithFlavor(DishDto dishDto);

    // 根据id查询菜品菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    // 更新菜品信息, 同时更新对应的口味信息
    public void updateWithFlavor(DishDto dishDto);

    // 根据id删除菜品和对应的口味信息
    public boolean deleteByIdWithFlavor(Long id);
}
