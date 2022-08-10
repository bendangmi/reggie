package com.bdm.reggie.dto;


import com.bdm.reggie.entity.Setmeal;
import com.bdm.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
