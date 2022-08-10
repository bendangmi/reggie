package com.bdm.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bdm.reggie.dto.SetmealDto;
import com.bdm.reggie.entity.Setmeal;
import com.bdm.reggie.mapper.SetmealMapper;

import java.util.List;

/**
 * @code Description
 * @code author 本当迷
 * @code date 2022/8/5-10:34
 */
public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    public void removeWithDish(List<Long> ids);
}
