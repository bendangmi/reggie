package com.bdm.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bdm.reggie.common.R;
import com.bdm.reggie.entity.User;
import com.bdm.reggie.service.UserService;
import com.bdm.reggie.util.SMSUtils;
import com.bdm.reggie.util.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @code Description
 * @code author 本当迷
 * @code date 2022/8/8-9:31
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 发送验证码
     *
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    private R<String> sendMsg(@RequestBody User user, HttpSession session) {
        log.info(user.toString());
        // 获取手机号
        final String phone = user.getPhone();

        if (phone != null) {
            // 生成随机的4位验证码
            final String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}", code);

            // 调用阿里云提供的短信服务API完成发送短信
            // SMSUtils.sendMessage("本当迷博客", "SMS_248060472", phone, code);

            // 需要将生成的验证码保存到Session
            session.setAttribute(phone, code);
            return R.success("手机验证码发生成功！");
        }
        return R.error("短信发送失败！");
    }

    /**
     * 移动端用户登录
     *
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String, String> map, HttpSession session) {
        // 获取手机号
        String phone = map.get("phone").toString();

        // 获取验证码
        String code = map.get("code").toString();

        // 从Session中获取保存的验证码
        final Object codeInSession = session.getAttribute(phone);

        // 进行验证码的对比（页面提交的验证码喝Session中保存的验证码对比）
        if(codeInSession != null && codeInSession.equals(code)){
            // 如果能够对比成功，说明登录成功
            final LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);

            User user = userService.getOne(queryWrapper);
            if(user == null){
                // 判断当前手机号对应的用户是否为新用户，如果为新用户就自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user" , user.getId());
            return R.success(user);

        }

        return R.error("短信验证码不一致！！");
    }

    @PostMapping("/loginout")
    public R<String>loginOut(HttpSession session){
        session.removeAttribute("user");
        return R.success("退出成功");
    }


}
