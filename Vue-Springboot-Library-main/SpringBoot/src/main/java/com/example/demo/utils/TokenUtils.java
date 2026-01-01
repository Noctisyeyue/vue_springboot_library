package com.example.demo.utils;

import cn.hutool.core.date.DateUtil;

import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * JWT Token工具类
 * 核心用途：提供JWT token的生成和解析功能，用于用户身份认证
 */
@Slf4j// Lombok注解，自动生成日志记录器
@Component
public class TokenUtils {

    @Autowired
    private UserMapper userMapper;

    private static UserMapper staticUserMapper;

    @PostConstruct
    public void init() {
        staticUserMapper = userMapper;
    }

    /**
     * 生成JWT token
     * 核心逻辑：使用用户ID作为audience，用户密码作为签名密钥，有效期1天
     * @param user 用户对象，包含id和password字段
     * @return String JWT token字符串
     */
    public static String genToken(User user) {
        return JWT.create()
                .withExpiresAt(DateUtil.offsetDay(new Date(), 1))
                .withAudience(user.getId().toString())
                .sign(Algorithm.HMAC256(user.getPassword()));
    }

    /**
     * 从请求头获取token并解析用户信息
     * 核心逻辑：从请求头获取token，解析用户ID，查询用户信息
     * @return User 用户对象，解析失败返回null
     */
    public static User getUser() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = request.getHeader("token");
            String aud = JWT.decode(token).getAudience().get(0);
            Integer userId = Integer.valueOf(aud);
            return staticUserMapper.selectById(userId);
        } catch (Exception e) {
            log.error("解析token失败", e);
            return null;
        }
    }
}
