package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.demo.LoginUser;
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

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    @Resource
    private UserMapper userMapper;
    @Resource
    private LendRecordMapper lendRecordMapper;
    @Resource
    private BookMapper bookMapper;
    @GetMapping
    public  Result<?> dashboardrecords(){
        int visitCount = LoginUser.getVisitCount();

        QueryWrapper<User> queryWrapper1=new QueryWrapper();//查询条件
        int userCount = userMapper.selectCount(queryWrapper1);//用户表记录数量；

        QueryWrapper<LendRecord> queryWrapper2=new QueryWrapper();
        int lendRecordCount = lendRecordMapper.selectCount(queryWrapper2);//借阅记录数量

        QueryWrapper<Book> queryWrapper3=new QueryWrapper();
        int bookCount = bookMapper.selectCount(queryWrapper3);//图书数量

        Map<String, Object> map = new HashMap<>();//键值对形式
        map.put("visitCount", visitCount);//放置visitCount到map中
        map.put("userCount", userCount);
        map.put("lendRecordCount", lendRecordCount);
        map.put("bookCount", bookCount);
        return Result.success(map);
    }



}
