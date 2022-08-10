package com.bdm.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bdm.reggie.entity.User;
import com.bdm.reggie.mapper.UserMapper;
import com.bdm.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @code Description
 * @code author 本当迷
 * @code date 2022/8/8-9:30
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
