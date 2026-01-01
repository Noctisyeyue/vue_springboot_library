package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.utils.LoginUser;
import com.example.demo.commom.Result;
import com.example.demo.entity.BookWithUser;
import com.example.demo.entity.User;
import com.example.demo.mapper.BookCollectionMapper;
import com.example.demo.mapper.BookWithUserMapper;
import com.example.demo.mapper.LendRecordMapper;
import com.example.demo.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;
import com.example.demo.utils.TokenUtils;


import javax.annotation.Resource;
import java.util.List;

/**
 * 用户管理控制器
 * 核心用途：提供用户注册、登录、信息管理、密码修改等接口
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    UserMapper userMapper;
    @Resource
    BookWithUserMapper bookWithUserMapper;
    @Resource
    LendRecordMapper lendRecordMapper;
    @Resource
    BookCollectionMapper bookCollectionMapper;

    /**
     * 用户注册（含用户名重复检查）
     * 核心逻辑：检查用户名是否已存在，不存在则插入新用户
     * @param user 用户对象，包含用户名、密码等基本信息
     * @return Result 统一响应对象，成功返回success，失败返回错误信息
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user){
        User res = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername,user.getUsername()));
        if(res != null)
        {
            return Result.error("-1","用户名已重复");
        }
        userMapper.insert(user);
        return Result.success();
    }
    /**
     * 用户登录（含身份验证与JWT token生成）
     * 核心逻辑：验证用户名和密码，生成JWT token，统计访问量
     * @param user 用户对象，包含用户名和密码
     * @return Result<User> 包含用户信息和token的响应对象，失败返回错误信息
     */
    @CrossOrigin
    @PostMapping("/login")
    public Result<?> login(@RequestBody User user){
        User res = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername,user.getUsername())
                .eq(User::getPassword,user.getPassword()));
        if(res == null)
        {
            return Result.error("-1","用户名或密码错误");
        }
        String token = TokenUtils.genToken(res);
        res.setToken(token);
        LoginUser.addVisitCount();
        return Result.success(res);
    }

    /**
     * 新增用户（管理员功能，含默认密码设置）
     * 核心逻辑：如果未设置密码，默认设置为"abc123456"
     * @param user 用户对象
     * @return Result 统一响应对象，成功返回success
     */
    @PostMapping
    public Result<?> save(@RequestBody User user){
        User res = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername,user.getUsername()));
        if(res != null)
        {
            return Result.error("-1","用户名已重复");
        }
        if(user.getPassword() == null){
            user.setPassword("abc123456");
        }
        userMapper.insert(user);
        return Result.success();
    }

    /**
     * 修改用户密码（通过请求参数）
     * @param id 用户ID
     * @param password2 新密码
     * @return Result 统一响应对象，成功返回success
     */
    @PutMapping("/password")
    public Result<?> update(@RequestParam Integer id,
                           @RequestParam String password2){
        LambdaUpdateWrapper<User> updateWrapper = Wrappers.<User>lambdaUpdate()
                .eq(User::getId, id);
        User user = new User();
        user.setPassword(password2);
        userMapper.update(user, updateWrapper);
        return Result.success();
    }

    /**
     * 修改用户信息（通过请求体）
     * 核心逻辑：如果修改了用户名，检查是否与其他用户重复
     * @param user 用户对象，包含需要更新的字段
     * @return Result 统一响应对象，成功返回success，失败返回错误信息
     */
    @PutMapping
    public Result<?> password(@RequestBody User user){
        // 如果修改了用户名，检查是否与其他用户重复
        if (user.getUsername() != null && !user.getUsername().isEmpty() && user.getId() != null) {
            LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>lambdaQuery()
                    .eq(User::getUsername, user.getUsername())
                    .ne(User::getId, user.getId()); // 排除当前用户自己
            User existingUser = userMapper.selectOne(queryWrapper);
            if (existingUser != null) {
                return Result.error("-1", "用户名已重复");
            }
        }
        userMapper.updateById(user);
        return Result.success();
    }

    /**
     * 批量删除用户（含未归还图书检查）
     * 核心逻辑：检查用户是否有未归还图书，有则不允许删除
     * @param ids 用户ID列表
     * @return Result 统一响应对象，成功返回success，失败返回错误信息
     */
    @PostMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestBody List<Integer> ids){
        for (Integer userId : ids) {
            LambdaQueryWrapper<BookWithUser> wrapper = Wrappers.<BookWithUser>lambdaQuery()
                    .eq(BookWithUser::getReaderId, userId);
            Integer count = bookWithUserMapper.selectCount(wrapper).intValue();
            if (count > 0) {
                User user = userMapper.selectById(userId);
                String userName = user != null ? user.getNickName() : "ID:" + userId;
                return Result.error("-1", "用户 [" + userName + "] 有未归还的图书，无法删除");
            }
        }
        userMapper.deleteBatchIds(ids);
        return Result.success();
    }

    /**
     * 删除单个用户（含未归还图书检查）
     * 核心逻辑：检查用户是否有未归还图书，有则不允许删除
     * @param id 用户ID
     * @return Result 统一响应对象，成功返回success，失败返回错误信息
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id){
        LambdaQueryWrapper<BookWithUser> wrapper = Wrappers.<BookWithUser>lambdaQuery()
                .eq(BookWithUser::getReaderId, id);
        Integer count = bookWithUserMapper.selectCount(wrapper).intValue();
        if (count > 0) {
            return Result.error("-1", "该用户有未归还的图书，无法删除");
        }
        userMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 分页查询用户列表（仅查询普通用户，支持昵称模糊搜索）
     * 核心逻辑：只查询role=2的普通用户，支持按昵称模糊查询
     * @param pageNum 页码，默认值为1
     * @param pageSize 每页条数，默认值为10
     * @param search 模糊匹配参数：用户昵称
     * @return Result<Page<User>> 包含分页信息的用户列表响应
     */
    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search){
        LambdaQueryWrapper<User> wrappers = Wrappers.<User>lambdaQuery();
        if(StringUtils.isNotBlank(search)){
            wrappers.like(User::getNickName,search);
        }
        wrappers.eq(User::getRole,2);  // 只查询普通用户
        Page<User> userPage =userMapper.selectPage(new Page<>(pageNum,pageSize), wrappers);
        return Result.success(userPage);
    }

    /**
     * 多条件分页查询用户列表（仅查询普通用户）
     * 核心逻辑：支持按ID、昵称、电话多条件模糊查询，只查询role=2的普通用户
     * @param pageNum 页码，默认值为1
     * @param pageSize 每页条数，默认值为10
     * @param search1 模糊匹配参数：用户ID
     * @param search2 模糊匹配参数：用户昵称
     * @param search3 模糊匹配参数：用户电话
     * @return Result<Page<User>> 包含分页信息的用户列表响应
     */
    @GetMapping("/usersearch")
    public Result<?> findPage2(@RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize,
                               @RequestParam(defaultValue = "") String search1,
                               @RequestParam(defaultValue = "") String search2,
                               @RequestParam(defaultValue = "") String search3){
        LambdaQueryWrapper<User> wrappers = Wrappers.<User>lambdaQuery();
        if(StringUtils.isNotBlank(search1)){
            wrappers.like(User::getId,search1);
        }
        if(StringUtils.isNotBlank(search2)){
            wrappers.like(User::getNickName,search2);
        }
        if(StringUtils.isNotBlank(search3)){
            wrappers.like(User::getPhone,search3);
        }
        wrappers.eq(User::getRole,2);  // 只查询普通用户
        Page<User> userPage =userMapper.selectPage(new Page<>(pageNum,pageSize), wrappers);
        return Result.success(userPage);
    }
}
