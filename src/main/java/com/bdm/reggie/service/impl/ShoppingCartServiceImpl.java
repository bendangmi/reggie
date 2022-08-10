package com.bdm.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bdm.reggie.entity.ShoppingCart;
import com.bdm.reggie.mapper.ShoppingCartMapper;
import com.bdm.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @code Description
 * @code author 本当迷
 * @code date 2022/8/9-9:18
 */

@Service
@Slf4j
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
