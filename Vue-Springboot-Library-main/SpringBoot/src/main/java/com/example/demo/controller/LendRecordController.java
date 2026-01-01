package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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

/**
 * 借阅历史管理控制器
 * 核心用途：提供借阅历史记录的增删改查接口，支持权限控制
 */
@RestController
@RequestMapping("/LendRecord")
public class LendRecordController {
    @Resource
    LendRecordMapper lendRecordMapper;

    /**
     * 新增借阅历史记录
     * @param lendRecord 借阅历史记录对象
     * @return Result 统一响应对象，成功返回success
     */
    @PostMapping
    public Result<?> save(@RequestBody LendRecord lendRecord) {
        lendRecordMapper.insert(lendRecord);
        return Result.success();
    }

    /**
     * 分页查询借阅历史记录（多表关联查询，含权限控制）
     * 核心逻辑：普通用户只能查看自己的记录，管理员可查看所有记录
     * @param pageNum 页码，默认值为1
     * @param pageSize 每页条数，默认值为10
     * @param search1 模糊匹配参数：图书ISBN
     * @param search2 模糊匹配参数：图书名称
     * @param search3 模糊匹配参数：读者ID（普通用户自动设置为当前用户ID）
     * @return Result<Page<LendRecordDTO>> 包含分页信息的借阅历史列表响应
     */
    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search1,
                              @RequestParam(defaultValue = "") String search2,
                              @RequestParam(defaultValue = "") String search3) {
        // 权限控制：普通用户只能查看自己的记录
        User currentUser = TokenUtils.getUser();
        if (currentUser != null && currentUser.getRole() != null && currentUser.getRole() != 1) {
            search3 = String.valueOf(currentUser.getId());
        }
        Page<LendRecordDTO> lendRecordPage = lendRecordMapper.findPageWithDetails(
                new Page<>(pageNum, pageSize), search1, search2, search3);
        return Result.success(lendRecordPage);
    }

    /**
     * 更新借阅历史记录（管理员操作）
     * 核心逻辑：使用复合主键(reader_id, book_id, lend_time)更新归还时间和状态
     * @param lendRecord 借阅历史记录对象，包含复合主键字段和需要更新的字段
     * @return Result 统一响应对象，成功返回success，失败返回错误信息
     */
    @PutMapping
    public Result<?> returnBook(@RequestBody LendRecord lendRecord) {
        LambdaUpdateWrapper<LendRecord> updateWrapper = Wrappers.<LendRecord>lambdaUpdate()
                .eq(LendRecord::getReaderId, lendRecord.getReaderId())
                .eq(LendRecord::getBookId, lendRecord.getBookId())
                .eq(LendRecord::getLendTime, lendRecord.getLendTime());
        
        LendRecord updateRecord = new LendRecord();
        updateRecord.setReturnTime(lendRecord.getReturnTime());
        updateRecord.setStatus(lendRecord.getStatus());

        int result = lendRecordMapper.update(updateRecord, updateWrapper);

        if (result > 0) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败，未找到对应的借阅记录");
        }
    }

    /**
     * 删除借阅历史记录（根据复合主键）
     * 核心逻辑：使用复合主键(reader_id, book_id, lend_time)删除记录
     * @param lendRecord 借阅历史记录对象，包含复合主键字段
     * @return Result 统一响应对象，成功返回success，失败返回错误信息
     */
    @DeleteMapping
    public Result<?> delete(@RequestBody LendRecord lendRecord) {
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
