package com.bdm.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bdm.reggie.entity.Orders;

/**
 * @code Description
 * @code author 本当迷
 * @code date 2022/8/9-11:35
 */
public interface OrderService extends IService<Orders> {

    /**
     * 用户下单
     * @param orders
     */
    public void submit(Orders orders);
}
