package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

/**
 * 用户实体类，对应数据库 "user" 表（user为Oracle关键字，需加双引号）
 * 核心用途：封装用户基本信息，支持管理员和普通读者两种角色
 */
@TableName("\"user\"")
@Data
public class User {
    @TableId (type = IdType.AUTO)
    private Integer id;
    private String username;
    private String nickName;
    private String password;
    private String sex;
    private String address;
    private String phone;
    @TableField(exist = false)  // 非数据库字段，用户登录成功后存储JWT token
    private String token;
    private Integer role;  // 角色：1-管理员，2-普通读者

}
