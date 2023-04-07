package org.SecondImage.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.SecondImage.reggie.common.R;
import org.SecondImage.reggie.entry.User;
import org.SecondImage.reggie.service.UserService;
import org.SecondImage.reggie.utils.SMSUtils;
import org.SecondImage.reggie.utils.ValidateCodeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    //注入RedisTemplate对象，缓存验证码
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)){
            //随机生成四位验证码
            String s = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("Code: {}",s);
            //通过阿里云短信服务API发送验证码 目前未开通
//            SMSUtils.sendMessage("瑞吉外卖","SMS_275460402",phone,s);
            //保存验证码至对应手机号的session
//            session.setAttribute(phone,s);
            //将生成的验证码缓存到redis中，寿命5分钟
            redisTemplate.opsForValue().set(phone,s,5,TimeUnit.MINUTES);

            return R.success("短信发送成功");
        }
        return R.error("短信发送失败");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession session){
        log.info("phone + code: {}",map);
        //获取手机号
        String phone =map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //校验验证码   通过session获取手机号对应的验证码
//        String checkCode = session.getAttribute(phone).toString();
        //从redis获取验证码
        String checkCode = redisTemplate.opsForValue().get(phone).toString();
        if (checkCode != null && code.equals(checkCode)){
            //对比成功则登录
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            //判断是否为新用户，是则注册账户
            if (user == null){
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            //登陆成功删除redis中的验证码
            redisTemplate.delete(phone);
            return R.success(user);
        }

        return R.error("登陆失败");
    }
}
