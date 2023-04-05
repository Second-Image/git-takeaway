package org.SecondImage.reggie.dto;


import lombok.Data;
import org.SecondImage.reggie.entry.Dish;
import org.SecondImage.reggie.entry.DishFlavor;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
