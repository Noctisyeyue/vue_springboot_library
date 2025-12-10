package com.example.demo.controller;

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
     * 使用原生SQL避免Oracle TIMESTAMP类型转换问题
     */
    @PutMapping
    public Result<?> updateReturn(@RequestBody LendRecord lendRecord){
        // 使用自定义的更新方法，避免UpdateWrapper的TIMESTAMP类型转换问题
        int result = lendRecordMapper.updateReturnTime(
                lendRecord.getReaderId(),
                lendRecord.getBookId(),
                lendRecord.getLendTime(),
                new Date() // 设置归还时间为当前时间
        );

        if (result > 0) {
            return Result.success();
        } else {
            return Result.error("归还失败：未找到对应的借阅记录");
        }
    }
}
