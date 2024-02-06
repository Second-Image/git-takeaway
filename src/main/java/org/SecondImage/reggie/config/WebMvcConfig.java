package org.SecondImage.reggie.config;

import lombok.extern.slf4j.Slf4j;

import org.SecondImage.reggie.common.JacksonObjectMapper;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

    /**
     * 配置静态资源映射
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("进行静态资源映射......");
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
        registry.addResourceHandler("/takeout/**").addResourceLocations("classpath:/takeout/");
    }

    /**
     * 扩展mvc框架的消息转换器
     * mvc框架的消息转换器默认有八个
     * 该方法项目启动时就会被调用
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter= new MappingJackson2HttpMessageConverter();
        //设置消息转换器，底层使用Jackson把Java对象转换成Json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将设置的消息转换器对象追加到mvc框架的消息转换器集合中，并设置最优先使用index=0
        converters.add(0,messageConverter);//当自定义设置的消息转换器无法完成一些转换时，会寻找其他转换器，设置最优先不会让其他转换器失效
    }
}
