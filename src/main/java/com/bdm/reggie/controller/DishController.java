package com.bdm.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bdm.reggie.common.R;
import com.bdm.reggie.dto.DishDto;
import com.bdm.reggie.entity.Category;
import com.bdm.reggie.entity.Dish;
import com.bdm.reggie.entity.DishFlavor;
import com.bdm.reggie.service.CategoryService;
import com.bdm.reggie.service.DishFlavorService;
import com.bdm.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @code Description 菜品管理
 * @code author 本当迷
 * @code date 2022/8/6-9:26
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService; // 菜品

    @Autowired
    private DishFlavorService flavorService; // 菜品口味

    @Autowired
    private CategoryService categoryService; // 菜品分类

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        // 清理所有菜品的缓存数据
/*        final Set keys = redisTemplate.keys("dish_*");
        assert keys != null;
        redisTemplate.delete(keys);*/

        // 精确清理某个菜品的缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("新增菜品成功！");
    }

    /**
     * 菜品信息分类查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name){

        // 条件构造器
        final LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 添加过滤体条件
        queryWrapper.like(name != null, Dish::getName, name);
        // 添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        // 分页构造器
        final Page<Dish> pageInfo = new Page<>(page, pageSize);
        final Page<DishDto> dishDtoPage = new Page<>();

        // 执行分页查询
        dishService.page(pageInfo, queryWrapper);

        // 对象拷贝
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        final List<Dish> records = pageInfo.getRecords();

        List<DishDto> list  =  records.stream().map((item) -> {
            final DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item, dishDto);

            final Long categoryId = item.getCategoryId(); // 分类id


            // 根据id查询分类对象
            final Category category = categoryService.getById(categoryId);

            if(category != null){
                final String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;

        }).collect(Collectors.toList());


        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);

    }


    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> select(@PathVariable long id){
        final DishDto byIdWithFlavor = dishService.getByIdWithFlavor(id);
        return R.success(byIdWithFlavor);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){

        log.info(dishDto.toString());

       dishService.updateWithFlavor(dishDto);

       // 清理所有菜品的缓存数据
/*        final Set keys = redisTemplate.keys("dish_*");
        assert keys != null;
        redisTemplate.delete(keys);*/

        // 精确清理某个菜品的缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("菜品修改成功！");
    }


    /**
     * 批量起售和停售
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> bulkSales(@PathVariable int status, String ids){
        log.info(ids);
        final String[] splitIds = ids.split(",");

        for(String s : splitIds){
            long id = Long.parseLong(s);
            final Dish dish = dishService.getById(id);
            dish.setStatus(status);
            final boolean save = dishService.updateById(dish);
            if (!save) return R.error("系统错误，请检查日志！");
        }
        return R.success("修改成功！");
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String>bulkDelete(String ids){
        final String[] splitIds = ids.split(",");
        for(String s : splitIds){
            // 菜品id
            long id = Long.parseLong(s);

            final boolean deleteFlag = dishService.deleteByIdWithFlavor(id);
            if(!deleteFlag) return R.error("删除失败！");


        }
        return R.success("删除成功！");
    }

    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){

        List<DishDto> dishDtoList = null;

        // 动态构造key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();

        // 先从Redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if(dishDtoList != null){
            // 如果存在，直接返回，无需查询数据库
            return R.success(dishDtoList);
        }




        // 构造条件
        final LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        // 添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus, 1);

        // 排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        final List<Dish> list = dishService.list(queryWrapper);

        dishDtoList = list.stream().map(item ->{
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item, dishDto);

            final Long categoryId = item.getCategoryId();
            // 根据id查询分类对象
            final Category category = categoryService.getById(categoryId);
            if(category != null){
                dishDto.setCategoryName(category.getName());
            }

            // 当前菜品的id
            final Long dishId = item.getId();
            final LambdaQueryWrapper<DishFlavor> dishFlavorQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorQueryWrapper.eq(DishFlavor::getDishId, dishId);
            final List<DishFlavor> flavorList = flavorService.list(dishFlavorQueryWrapper);
            dishDto.setFlavors(flavorList);

            return dishDto;
        }).collect(Collectors.toList());

        // 如果不存在，需要查询数据库，将查询到的菜品数据缓存到Redis
        redisTemplate.opsForValue().set(key, dishDtoList, 1, TimeUnit.HOURS);

        return R.success(dishDtoList);

    }







}
