package org.SecondImage.reggie.entry;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ManForTakeaway implements Serializable {

    private static final long serialVersionUID = 1L;
    //ID
    private Long id;
    //状态
    private Integer status;
    //外卖员姓名
    private String name;
    //手机号
    private String phone;

    private String username;

    private String password;//默认123456

    private String sex;

    private String idNumber;   //身份证号码

    @TableField(fill = FieldFill.INSERT) //插入时填充
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE) //插入和修改时填充
    private LocalDateTime updateTime;
}
