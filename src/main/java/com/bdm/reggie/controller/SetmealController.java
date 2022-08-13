package com.bdm.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bdm.reggie.common.R;
import com.bdm.reggie.dto.SetmealDto;
import com.bdm.reggie.entity.Category;
import com.bdm.reggie.entity.Setmeal;
import com.bdm.reggie.entity.SetmealDish;
import com.bdm.reggie.service.CategoryService;
import com.bdm.reggie.service.SetmealDishService;
import com.bdm.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @code Description 套餐管理
 * @code author 本当迷
 * @code date 2022/8/7-9:26
 */

@RestController
@RequestMapping("/setmeal")
@Slf4j
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
    @CacheEvict(value = "setmealCache", allEntries = true) // 删除setmealCach这个缓存分类下的所有数据
    public R<String>save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息：{}", setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功！");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<SetmealDto>> selectPage(int page, int pageSize, String name){
        // 添加条件查询，根据name进行模糊查询
        final LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.like(name != null, Setmeal::getName, name);
        // 添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);


        // 分页构造器
        final Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        setmealService.page(setmealPage, queryWrapper);

        // 把 setmeal 的page对象数据拷贝到 setmealDato 中的page对象去
        final Page<SetmealDto> dtoPageSetmealPage = new Page<>(page, pageSize);
        BeanUtils.copyProperties(setmealPage, dtoPageSetmealPage);

        final List<SetmealDto> records = dtoPageSetmealPage.getRecords();
        for(int i = 0; i < records.size(); i++){
            final SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(records.get(i), setmealDto);
            // 通过套餐分类id查询具体的套餐分类名字
            final Category category = categoryService.getById(setmealDto.getCategoryId());
            if(category != null){
                setmealDto.setCategoryName(category.getName());
            }

            records.set(i, setmealDto);
        }

        return R.success(dtoPageSetmealPage);

    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache", allEntries = true) // 删除setmealCach这个缓存分类下的所有数据
    public R<String> deleteBatch(@RequestParam List<Long> ids){
        log.info("ids: {} ",ids.toString());

        setmealService.removeWithDish(ids);

        return R.success("删除成功！");
    }

    /**
     * 修改套餐状态
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> bulkSales(@PathVariable Integer status, @RequestParam List<Long> ids){
        log.info("status : {}", status);
        for (Long id : ids) {
            final Setmeal setmeal = setmealService.getById(id);
            setmeal.setStatus(status);
            final boolean updateById = setmealService.updateById(setmeal);
            if(!updateById) return R.error("修改失败！");

        }

        return R.success("修改成功！");
    }


    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId + '_' + #setmeal.status")
    public R<List<Setmeal>>list(Setmeal setmeal){
        final LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        final List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 根据id查询套餐信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto>getInfo(@PathVariable long id){
        // 根据id查询套餐信息
        final Setmeal setmeal = setmealService.getById(id);
        final SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);

        final LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getDishId, id);
        final List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);

        setmealDto.setSetmealDishes(setmealDishList);

        return R.success(setmealDto);
    }

    /**
     * 修改套餐信息
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        // 修改套餐
        final Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDto, setmeal);
        setmealService.updateById(setmeal);

        // 修改套餐菜品
        // 根据套餐id删除菜品
        final Long setmealId = setmealDto.getId();
        final LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealId);
        setmealDishService.remove(queryWrapper);

        // 添加新的菜品
        final List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for(int i = 0; i < setmealDishes.size(); i++){
            final SetmealDish setmealDish = setmealDishes.get(i);
            setmealDish.setSetmealId(setmealId);
            setmealDishes.set(i, setmealDish);
        }
        setmealDishService.saveBatch(setmealDishes);

        return R.success("修改成功！");
    }

    /**
     * 前端页面点击图片获取套餐数据
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<List<SetmealDish>> getFrontInfo(@PathVariable long id){

        final LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        final List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);

        return R.success(setmealDishList);
    }



}
