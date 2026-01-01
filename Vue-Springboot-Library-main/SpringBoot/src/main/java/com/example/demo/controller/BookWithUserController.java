package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.commom.Result;
import com.example.demo.dto.BookWithUserDTO;
import com.example.demo.entity.BookWithUser;
import com.example.demo.entity.User;
import com.example.demo.mapper.BookWithUserMapper;
import com.example.demo.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 当前借阅管理控制器
 * 核心用途：提供借阅记录的增删改查、续借等接口
 */
@RestController
@RequestMapping("/bookwithuser")
public class BookWithUserController {
    @Resource
    BookWithUserMapper bookWithUserMapper;

    /**
     * 新增借阅记录（含重复借阅检查与必填参数验证）
     * 核心逻辑：同一用户不可重复借阅同一本图书，需校验读者ID、图书ID必填
     * @param bookWithUser 借阅记录封装对象，包含readerId（读者ID）、bookId（图书ID）等核心字段
     * @return Result 统一响应对象，成功返回success，失败返回错误信息
     */
    @PostMapping("/insertNew")
    public Result<?> insertNew(@RequestBody BookWithUser bookWithUser){
        if (bookWithUser.getReaderId() == null) {
            return Result.error("读者ID不能为空");
        }
        if (bookWithUser.getBookId() == null) {
            return Result.error("图书ID不能为空");
        }

        // 检查是否已借阅相同图书
        LambdaQueryWrapper<BookWithUser> queryWrapper = Wrappers.<BookWithUser>lambdaQuery()
                .eq(BookWithUser::getReaderId, bookWithUser.getReaderId())
                .eq(BookWithUser::getBookId, bookWithUser.getBookId());

        long count = bookWithUserMapper.selectCount(queryWrapper);
        if(count > 0){
            return Result.error("您已借阅该书，归还后才能再次借阅");
        }

        bookWithUserMapper.insert(bookWithUser);
        return Result.success();
    }

    /**
     * 更新借阅记录（用于续借等操作）
     * 核心逻辑：根据读者ID和图书ID更新借阅信息，只更新到期时间和可续借次数等必要字段
     * @param bookWithUser 借阅记录对象，包含需要更新的字段（deadTime、prolong）
     * @return Result 统一响应对象，成功返回success
     */
    @PostMapping
    public Result<?> update(@RequestBody BookWithUser bookWithUser){
        LambdaUpdateWrapper<BookWithUser> updateWrapper = Wrappers.<BookWithUser>lambdaUpdate()
                .eq(BookWithUser::getBookId, bookWithUser.getBookId())
                .eq(BookWithUser::getReaderId, bookWithUser.getReaderId());

        // 只更新需要变更的字段，避免误改主键等关键字段
        BookWithUser updateRecord = new BookWithUser();
        updateRecord.setDeadTime(bookWithUser.getDeadTime());
        updateRecord.setProlong(bookWithUser.getProlong());

        bookWithUserMapper.update(updateRecord, updateWrapper);
        return Result.success();
    }

    /**
     * 删除单条借阅记录（还书时调用）
     * 核心逻辑：根据读者ID和图书ID删除当前借阅记录
     * @param bookWithUser 借阅记录对象，包含readerId和bookId
     * @return Result 统一响应对象，成功返回success
     */
    @PostMapping("/deleteRecord")
    public Result<?> deleteRecord(@RequestBody BookWithUser bookWithUser){
        LambdaQueryWrapper<BookWithUser> queryWrapper = Wrappers.<BookWithUser>lambdaQuery()
                .eq(BookWithUser::getBookId, bookWithUser.getBookId())
                .eq(BookWithUser::getReaderId, bookWithUser.getReaderId());

        bookWithUserMapper.delete(queryWrapper);
        return Result.success();
    }

    /**
     * 批量删除借阅记录
     * @param bookWithUsers 借阅记录列表
     * @return Result 统一响应对象，成功返回success
     */
    @PostMapping("/deleteRecords")
    public Result<?> deleteRecords(@RequestBody List<BookWithUser> bookWithUsers){
        for(BookWithUser record : bookWithUsers) {
            LambdaQueryWrapper<BookWithUser> queryWrapper = Wrappers.<BookWithUser>lambdaQuery()
                    .eq(BookWithUser::getBookId, record.getBookId())
                    .eq(BookWithUser::getReaderId, record.getReaderId());
            bookWithUserMapper.delete(queryWrapper);
        }
        return Result.success();
    }

    /**
     * 分页查询借阅记录（支持多条件搜索，多表关联查询）
     * 核心逻辑：使用多表关联查询，返回包含图书和用户信息的DTO
     * @param pageNum 页码，默认值为1
     * @param pageSize 每页条数，默认值为10
     * @param search1 模糊匹配参数：图书ISBN
     * @param search2 模糊匹配参数：图书名称
     * @param search3 模糊匹配参数：借阅者昵称
     * @return Result<Page<BookWithUserDTO>> 包含分页信息的借阅记录列表响应
     */
    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search1,
                              @RequestParam(defaultValue = "") String search2,
                              @RequestParam(defaultValue = "") String search3){
        // 权限控制：普通用户只能查看自己的记录
        User currentUser = TokenUtils.getUser();
        if (currentUser != null && currentUser.getRole() != null && currentUser.getRole() != 1) {
            search3 = String.valueOf(currentUser.getNickName());
        }
        Page<BookWithUserDTO> result = bookWithUserMapper.findPageWithDetails(new Page<>(pageNum, pageSize), search1, search2, search3);
        return Result.success(result);
    }
}