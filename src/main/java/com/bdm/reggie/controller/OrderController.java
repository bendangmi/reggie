package com.bdm.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bdm.reggie.common.BaseContext;
import com.bdm.reggie.common.R;
import com.bdm.reggie.dto.OrdersDto;
import com.bdm.reggie.entity.OrderDetail;
import com.bdm.reggie.entity.Orders;
import com.bdm.reggie.service.OrderDetailService;
import com.bdm.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @code Description
 * @code author 本当迷
 * @code date 2022/8/9-11:37
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;


    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}", orders);
        orderService.submit(orders);
        return  R.success("下单成功！");
    }


    /**
     * 订单分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page<OrdersDto>> userPage(int page, int pageSize){
        // 根据id进行分页查询订单
        final Page<Orders> ordersPage = new Page<>(page, pageSize);
        final LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(Orders::getOrderTime);
        final Page<Orders> page1 = orderService.page(ordersPage, queryWrapper);

        // 获取订单集合
        final List<Orders> records = page1.getRecords();


        final Page<OrdersDto> orderDtoPage = new Page<>();

        final List<OrdersDto> ordersDtos = new ArrayList<>();
        for (Orders record : records) {
            final OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(record, ordersDto);
            // 根据订单id查 订单明细表
            final LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
            orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId, record.getId());
            final List<OrderDetail> list = orderDetailService.list(orderDetailLambdaQueryWrapper);
            ordersDto.setOrderDetails(list);
            ordersDtos.add(ordersDto);
        }
        orderDtoPage.setRecords(ordersDtos);

        return R.success(orderDtoPage);

    }


    /**
     * 获取全部菜品信息
     * @param page
     * @param pageSize
     */
    @GetMapping("/page")
    public R<Page<Orders>> page(int page, int pageSize, String number, String beginTime, String endTime){
        final Page<Orders> ordersPage = new Page<>(page, pageSize);
        final LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(number != null, Orders::getNumber, number);
        queryWrapper.between(beginTime != null && endTime != null, Orders::getOrderTime, beginTime, endTime);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(ordersPage, queryWrapper);
        return R.success(ordersPage);

    }


    /**
     * 修改订单状态
     * @param orders
     * @return
     */
    @PutMapping
    public R<String>status(@RequestBody Orders orders){
        // 通过订单号修改订单状态
        final LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getId, orders.getId());
        final Orders orderOne = orderService.getOne(queryWrapper);
        orderOne.setStatus(orders.getStatus());
        // 保存修改
        orderService.updateById(orderOne);
        return R.success("状态修改成功!");

    }


    /**
     * 再来一单
     * @param orders
     * @return
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders){
        // 通过订单号查找订单
        final LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getId, orders.getId());
        final Orders orderOne = orderService.getOne(queryWrapper);
        // 通过订单号id查找订单明细
        final LambdaQueryWrapper<OrderDetail> orderDetailWrapper = new LambdaQueryWrapper<>();
        orderDetailWrapper.eq(OrderDetail::getOrderId, orders.getId());
        final List<OrderDetail> orderDetailList = orderDetailService.list(orderDetailWrapper);

        // 修改订单状态并添加订单
        orderOne.setStatus(2);
        long orderId = IdWorker.getId();//订单号id
        orderOne.setId(orderId);
        orderOne.setOrderTime(LocalDateTime.now());
        orderService.save(orderOne);

        for(OrderDetail orderDetail : orderDetailList){
            long orderDetailId = IdWorker.getId();//订单号明细id
            orderDetail.setOrderId(orderId);
            orderDetail.setId(orderDetailId);
            orderDetailService.save(orderDetail);
        }

        return R.success("再来一单, 添加成功！！");
    }


}
