package com.bdm.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bdm.reggie.common.R;
import com.bdm.reggie.entity.Category;
import com.bdm.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @code Description 分类管理
 * @code author 本当迷
 * @code date 2022/8/5-9:21
 */
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("添加分类：{}}", category);
        final boolean save = categoryService.save(category);
        if(save) return R.success("添加分类成功！");
        return R.error("添加分类失败, 未知错误！");
    }


    /**
     * 分类查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page<Category>> page(int page, int pageSize){

        // 分页构造器
        Page<Category> categoryPage = new Page<>(page, pageSize);

        // 如果当前页码值大于总页码值，那么重新执行查询操作，使用最大页码值作为当前页码值
        if(page > categoryPage.getPages()){

            categoryPage = new Page<>(categoryPage.getPages(), pageSize);
        }

        // 条件构造器
        final LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        // 添加排序条件，根据sort进行排序
        wrapper.orderByAsc(Category::getSort);

        // 分页查询
        categoryService.page(categoryPage, wrapper);
        return R.success(categoryPage);
    }

    /**
     * 根据id删除分类
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("删除分类：id = {}", ids);
//        categoryService.removeById(ids);
        categoryService.remove(ids);
        return R.success("分类信息删除成功！");

    }

    /**
     * 根据id修改分类信息
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        final boolean flag = categoryService.updateById(category);
        if(flag) return R.success("修改分类信息成功");
        return R.error("未知错误");
    }

    /**
     * 根据条件查询分类数据
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list( Category category){
        // 条件构造器
        final LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 添加条件
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        // 添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        return R.success(categoryService.list(queryWrapper));

    }


}
