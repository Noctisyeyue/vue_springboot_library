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
import com.example.demo.mapper.BookWithUserMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/bookwithuser")
public class BookWithUserController {
    @Resource
    BookWithUserMapper bookWithUserMapper;

    /**
     * 新增借阅记录 - 带重复检查
     */
    @PostMapping("/insertNew")
    public Result<?> insertNew(@RequestBody BookWithUser bookWithUser){
        // 检查该用户是否已经借阅了相同的书 - 查询"某个用户"借阅了"某本特定书"的记录
        LambdaQueryWrapper<BookWithUser> queryWrapper = Wrappers.<BookWithUser>lambdaQuery()
                .eq(BookWithUser::getReaderId, bookWithUser.getReaderId())
                .eq(BookWithUser::getBookId, bookWithUser.getBookId());

        long count = bookWithUserMapper.selectCount(queryWrapper);//去数据库里查询有多少条符合条件的记录，并返回数量
        if(count > 0){
            return Result.error("您已借阅该书，归还后才能再次借阅");
        }

        bookWithUserMapper.insert(bookWithUser);
        return Result.success();
    }

    @PostMapping
    public Result<?> update(@RequestBody BookWithUser bookWithUser){
        LambdaUpdateWrapper<BookWithUser> updateWrapper = Wrappers.<BookWithUser>lambdaUpdate()
                .eq(BookWithUser::getBookId, bookWithUser.getBookId())
                .eq(BookWithUser::getReaderId, bookWithUser.getReaderId());

        bookWithUserMapper.update(bookWithUser, updateWrapper);
        return Result.success();
    }

    /**
     * 删除一条记录（还书时调用）
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
     * 批量删除记录
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
     * 分页查询借阅记录（支持多条件搜索）
     * search1: 搜索ISBN图书编号
     * search2: 搜索图书名称
     * search3: 搜索读者ID
     */
    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search1,
                              @RequestParam(defaultValue = "") String search2,
                              @RequestParam(defaultValue = "") String search3){

        // 使用多表关联查询，返回包含图书和用户信息的DTO
        Page<BookWithUserDTO> page = new Page<>(pageNum, pageSize);
        Page<BookWithUserDTO> result = bookWithUserMapper.findPageWithDetails(page, search1, search2, search3);

        return Result.success(result);
    }
}