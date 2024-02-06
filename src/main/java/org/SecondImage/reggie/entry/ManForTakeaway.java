package org.SecondImage.reggie.entry;

import lombok.Data;

import java.io.Serializable;

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

    private String password;
}
