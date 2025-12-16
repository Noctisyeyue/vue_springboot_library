package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.commom.Result;
import com.example.demo.dto.LendRecordDTO;
import com.example.demo.entity.LendRecord;
import com.example.demo.entity.User;
import com.example.demo.mapper.LendRecordMapper;
import com.example.demo.utils.TokenUtils;
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
    public Result<?> save(@RequestBody LendRecord lendRecord) {
        lendRecordMapper.insert(lendRecord);
        return Result.success();
    }

    //分页查询借阅历史记录（多表关联查询）
    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search1,
                              @RequestParam(defaultValue = "") String search2,
                              @RequestParam(defaultValue = "") String search3) {
        // 普通用户只能查看自己的记录，管理员可筛选任意用户
        User currentUser = TokenUtils.getUser();
        if (currentUser != null && currentUser.getRole() != null && currentUser.getRole() != 1) {
            search3 = String.valueOf(currentUser.getId());
        }
        Page<LendRecordDTO> lendRecordPage = lendRecordMapper.findPageWithDetails(
                new Page<>(pageNum, pageSize), search1, search2, search3);
        return Result.success(lendRecordPage);
    }

    //编辑图书信息
    @PutMapping
    public Result<?> returnBook(@RequestBody LendRecord lendRecord) {
        UpdateWrapper<LendRecord> updateWrapper = new UpdateWrapper<>();
        // 使用复合主键更新：reader_id, book_id, lend_time
         updateWrapper.eq("reader_id", lendRecord.getReaderId())
                .eq("book_id", lendRecord.getBookId())
                .eq("lend_time", lendRecord.getLendTime());
        // 创建要更新的实体
        LendRecord updateRecord = new LendRecord();
        updateRecord.setReturnTime(lendRecord.getReturnTime());
        updateRecord.setStatus(lendRecord.getStatus());

        // 执行更新
        int result = lendRecordMapper.update(updateRecord, updateWrapper);

        if (result > 0) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败，未找到对应的借阅记录");
        }

    }

    //删除借阅历史记录（根据复合主键）
    @DeleteMapping
    public Result<?> delete(@RequestBody LendRecord lendRecord) {
        // 使用Lambda表达式构建查询条件
        LambdaQueryWrapper<LendRecord> queryWrapper = Wrappers.<LendRecord>lambdaQuery()
                .eq(LendRecord::getReaderId, lendRecord.getReaderId())
                .eq(LendRecord::getBookId, lendRecord.getBookId())
                .eq(LendRecord::getLendTime, lendRecord.getLendTime());

        int result = lendRecordMapper.delete(queryWrapper);

        if (result > 0) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败，记录不存在");
        }

    }
}
