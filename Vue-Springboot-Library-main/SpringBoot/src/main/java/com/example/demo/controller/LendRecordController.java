package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.commom.Result;
import com.example.demo.dto.LendRecordDTO;
import com.example.demo.entity.LendRecord;
import com.example.demo.mapper.LendRecordMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

//借阅历史记录
@RestController
@RequestMapping("/LendRecord")
public class LendRecordController {
    @Resource
    LendRecordMapper lendRecordMapper;

    //新增借阅历史记录
    @PostMapping
    public Result<?> save(@RequestBody LendRecord lendRecord){
        lendRecordMapper.insert(lendRecord);
        return Result.success();
    }

    //分页查询借阅历史记录（多表关联查询）
    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String bookId,
                              @RequestParam(defaultValue = "") String bookName,
                              @RequestParam(defaultValue = "") String readerId){
        Page<LendRecordDTO> lendRecordPage = lendRecordMapper.findPageWithDetails(
                new Page<>(pageNum, pageSize), bookId, bookName, readerId);
        return Result.success(lendRecordPage);
    }

    //归还图书（更新归还时间）
    @PutMapping("/return")
    public Result<?> returnBook(@RequestBody LendRecord lendRecord){
        UpdateWrapper<LendRecord> updateWrapper = new UpdateWrapper<>();
        // 使用复合主键更新：reader_id, book_id, lend_time
        updateWrapper.eq("reader_id", lendRecord.getReaderId())
                    .eq("book_id", lendRecord.getBookId())
                    .eq("lend_time", lendRecord.getLendTime());

        LendRecord updateRecord = new LendRecord();
        updateRecord.setReturnTime(new Date()); // 设置归还时间为当前时间

        lendRecordMapper.update(updateRecord, updateWrapper);
        return Result.success();
    }

    //删除借阅历史记录（根据复合主键）
    @DeleteMapping
    public Result<?> delete(@RequestParam Long readerId,
                           @RequestParam Long bookId,
                           @RequestParam String lendTime){
        UpdateWrapper<LendRecord> deleteWrapper = new UpdateWrapper<>();
        deleteWrapper.eq("reader_id", readerId)
                    .eq("book_id", bookId)
                    .eq("lend_time", lendTime);

        lendRecordMapper.delete(deleteWrapper);
        return Result.success();
    }
}
