package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.utils.LoginUser;
import com.example.demo.commom.Result;
import com.example.demo.entity.Book;
import com.example.demo.entity.LendRecord;
import com.example.demo.entity.User;
import com.example.demo.mapper.BookMapper;
import com.example.demo.mapper.LendRecordMapper;
import com.example.demo.mapper.UserMapper;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据统计控制器
 * 核心用途：提供Dashboard数据统计接口，返回系统核心指标
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    @Resource
    private UserMapper userMapper;
    @Resource
    private LendRecordMapper lendRecordMapper;
    @Resource
    private BookMapper bookMapper;

    /**
     * 获取Dashboard统计数据
     * 核心逻辑：统计总访问量、用户总数、借阅记录总数、图书总数
     * @return Result<Map<String, Object>> 包含visitCount、userCount、lendRecordCount、bookCount的响应对象
     */
    @GetMapping
    public Result<?> dashboardrecords(){
        int visitCount = LoginUser.getVisitCount();

        LambdaQueryWrapper<User> queryWrapper1 = Wrappers.<User>lambdaQuery();
        int userCount = userMapper.selectCount(queryWrapper1);

        LambdaQueryWrapper<LendRecord> queryWrapper2 = Wrappers.<LendRecord>lambdaQuery();
        int lendRecordCount = lendRecordMapper.selectCount(queryWrapper2);

        LambdaQueryWrapper<Book> queryWrapper3 = Wrappers.<Book>lambdaQuery();
        int bookCount = bookMapper.selectCount(queryWrapper3);

        Map<String, Object> map = new HashMap<>();
        map.put("visitCount", visitCount);
        map.put("userCount", userCount);
        map.put("lendRecordCount", lendRecordCount);
        map.put("bookCount", bookCount);
        return Result.success(map);
    }



}
