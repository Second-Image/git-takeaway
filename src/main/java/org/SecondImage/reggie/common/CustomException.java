package org.SecondImage.reggie.common;

/**
 * 自定义业务异常，由全局异常捕获处理
 */
public class CustomException extends RuntimeException{
    public CustomException(String message){
        super(message);
    }
}
