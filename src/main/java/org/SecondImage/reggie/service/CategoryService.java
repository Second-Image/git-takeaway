package org.SecondImage.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.SecondImage.reggie.entry.Category;

public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}
