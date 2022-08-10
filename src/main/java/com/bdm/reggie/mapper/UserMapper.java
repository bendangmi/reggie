package com.bdm.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bdm.reggie.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @code Description
 * @code author 本当迷
 * @code date 2022/8/8-9:28
 */

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
