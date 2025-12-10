package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.demo.commom.Result;
import com.example.demo.entity.LendRecord;
import com.example.demo.mapper.LendRecordMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

@RestController
@RequestMapping("/LendRecord1")
public class LendRecordController1 {
    @Resource
    LendRecordMapper lendRecordMapper;

    /**
     * 更新借阅历史记录（归还图书）
     * 根据复合主键定位记录：reader_id, book_id, lend_time
     */
    @PutMapping
    public Result<?> updateReturn(@RequestBody LendRecord lendRecord){
        UpdateWrapper<LendRecord> updateWrapper = new UpdateWrapper<>();
        // 使用3NF后的复合主键定位记录
        updateWrapper.eq("reader_id", lendRecord.getReaderId())
                    .eq("book_id", lendRecord.getBookId())
                    .eq("lend_time", lendRecord.getLendTime());

        LendRecord updateRecord = new LendRecord();
        updateRecord.setReturnTime(new Date()); // 设置归还时间为当前时间

        lendRecordMapper.update(updateRecord, updateWrapper);
        return Result.success();
    }
}
