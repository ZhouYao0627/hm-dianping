package com.hmdp.config;

import com.hmdp.Interceptor.LoginInterceptor;
import com.hmdp.Interceptor.RefreshTokenInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录拦截器
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns(
                        // 排除不需要拦截的路径
                        "/shop/**", // 店铺
                        "/voucher/**", // 优惠券信息
                        "/shop-type/**", // 店铺类型
                        "/upload/**", // 上传
                        "/blog/hot",  // 博客热点
                        "/user/code", // 发送验证码
                        "/user/login" // 用户登录
                ).order(1);
        // token刷新的拦截器
        registry.addInterceptor(new RefreshTokenInterceptor())
                .excludePathPatterns("/**")
                .order(0);
    }
}
