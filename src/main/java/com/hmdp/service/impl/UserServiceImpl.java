package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 1.获取手机号并判断是否符合要求
        String phone = loginForm.getPhone();
        boolean isPhone = RegexUtils.isPhoneInvalid(phone);
        if (!isPhone) {
            // 不符合 -> 返回错误信息
            return Result.fail("手机号格式有误，请重新输入！");
        }
        // 符合 -> 流程继续

        // 2.获取验证码
        // String code = (String) session.getAttribute("code");
        String code = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String userCode = loginForm.getCode();

        // 3.判断手机号和验证码都是否符合要求
        if (!isPhone || !code.equals(userCode)) {
            // 不符合 -> 返回错误信息
            return Result.fail("手机号或验证码有误，请重新输入！");
        }

        // 4.根据手机号判断数据库中是否有该用户
        User user = query().eq("phone", phone).one();
        if (user == null) {
            // 4.2 不存在，创建用户到数据库中
            user = createUserWithPhone(phone);
        }

        // 4.1 存在，保存用户到Redis
        // 生成token，作为登录令牌
        String token = UUID.randomUUID().toString();
        // 将User对象转为HashMap存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO,
                new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        // 存储
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 设置token有效期
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 返回token
        return Result.ok(token);
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomNumbers(10));
        save(user);
        return user;
    }

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1.1判断手机号是否符合要求
        if (!RegexUtils.isPhoneInvalid(phone)) {
            // 不符合 -> 返回错误信息
            return Result.fail("手机号格式有误，请重新输入！");
        }

        // 2.发送验证码
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        // 2.1保存验证码至Redis
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, uuid, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        log.debug("========Code:========" + uuid);

        return Result.ok();

    }
}
