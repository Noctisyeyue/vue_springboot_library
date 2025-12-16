package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.commom.Result;
import com.example.demo.entity.Book;
import com.example.demo.entity.LendRecord;
import com.example.demo.mapper.BookMapper;
import com.example.demo.mapper.LendRecordMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

@RestController
@RequestMapping("/LendRecord1")
public class LendRecordController1 {
    @Resource
    LendRecordMapper lendRecordMapper;
    @Resource
    BookMapper bookMapper;

    /**
     * 更新借阅历史记录（归还图书）
     */
    @PutMapping
    public Result<?> updateReturn(@RequestBody LendRecord lendRecord) {
        // 根据ISBN查询图书ID
        QueryWrapper<Book> bookQuery = new QueryWrapper<>();
        bookQuery.eq("isbn", lendRecord.getIsbn());
        Book book = bookMapper.selectOne(bookQuery);

        if (book == null) {
            return Result.error("归还失败：未找到对应的图书");
        }

        // 使用UpdateWrapper直接更新未归还的记录
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<LendRecord> updateWrapper =
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        updateWrapper.eq("reader_id", lendRecord.getReaderId())
                .eq("book_id", book.getId())
                .eq("lend_time", lendRecord.getLendTime()) // 添加lendTime条件
                .eq("status", "0") // 只更新未归还的记录
                .set("return_time", new Date())
                .set("status", "1");

        int result = lendRecordMapper.update(null, updateWrapper);

        if (result > 0) {
            return Result.success();
        } else {
            return Result.error("归还失败：未找到对应的借阅记录");
        }
    }
}
