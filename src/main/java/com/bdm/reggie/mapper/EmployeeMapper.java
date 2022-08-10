package com.bdm.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bdm.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @code Description
 * @code author 本当迷
 * @code date 2022/8/3-11:09
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

}
