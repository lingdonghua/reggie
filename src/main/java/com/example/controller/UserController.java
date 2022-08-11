package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.R;
import com.example.domain.User;
import com.example.service.UserService;
import com.example.untils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取登录验证码
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> getCode(@RequestBody User user , HttpSession session){
         //获取手机号
        String phone = user.getPhone();
        log.info("手机号："+phone);
        if(Strings.isNotEmpty(phone)){
            //获取验证码
            Integer code = ValidateCodeUtils.generateValidateCode(4);
            log.info("手机验证码为："+code.toString());
            //调用阿里云提供的短信服务API完成发送短信(略)
            //SMSUtils.sendMessage("瑞吉外卖","",phone,code);
            //
            //需要将生成的验证码保存到Session（便于验证）
            //session.setAttribute(phone,code);
            //把验证码存入redis中，并设置时间
            redisTemplate.opsForValue().set(phone,code.toString(),2, TimeUnit.MINUTES);
            return R.success("发送成功");
        }
       return R.error("发送失败");
    }

    /**
     * 登录校验
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    private R<String> login(@RequestBody Map map, HttpSession session){
        log.info("接收到的验证码："+map);
        //验证并且查看数据库是否存在改用户，若不存在，则新增
        String code = map.get("code").toString();
        String phone = map.get("phone").toString();
        //Object codeInSession = session.getAttribute(phone);
        //从redis中取出验证码
        Object codeInSession=redisTemplate.opsForValue().get(phone);
        log.info("Session中的验证码"+codeInSession);
        if(codeInSession!=null && codeInSession.toString().equals(code)){
            //验证码验证成功，查数据库是否存在
            LambdaQueryWrapper<User> lqw=new LambdaQueryWrapper<>();
            lqw.eq(Strings.isNotEmpty(phone),User::getPhone,phone);
            User user = userService.getOne(lqw);
            if(user==null){
                //数据库中不存在，给予注册
                user =new User();
                user.setPhone(phone);
                userService.save(user);
            }
            //保存
            session.setAttribute("user",user.getId());
            //登录成功，从redis中删除验证码
            redisTemplate.delete(phone);
            return R.success("登录成功");
        }

        return R.error("登录失败");
    }

    /**
     * 退出登录
     * @param request
     * @return
     */
    @PostMapping("/loginout")
    public R<String> loginOut(HttpServletRequest request){
        request.getSession().removeAttribute("user");
        return R.success("退出成功");
    }
}
