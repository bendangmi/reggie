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
    public R<String> deleteBatch(@RequestParam List<Long> ids){
        log.info("ids: {} ",ids.toString());

        setmealService.removeWithDish(ids);

        return R.success("删除成功！");
    }

    /**
     *
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
    public R<List<Setmeal>>list(Setmeal setmeal){
        final LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        final List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }




}
