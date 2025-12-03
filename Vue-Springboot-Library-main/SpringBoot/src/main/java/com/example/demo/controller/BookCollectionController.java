package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.commom.Result;
import com.example.demo.entity.Book;
import com.example.demo.entity.BookCollection;
import com.example.demo.mapper.BookCollectionMapper;
import com.example.demo.mapper.BookMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

@RestController
@RequestMapping("/bookCollection")
public class BookCollectionController {
    @Resource
    BookCollectionMapper bookCollectionMapper;

    @Resource
    BookMapper bookMapper;

    /**
     * 添加收藏
     */
    @PostMapping("/add")
    public Result<?> addCollection(@RequestBody BookCollection bookCollection){
        // 检查是否已收藏
        LambdaQueryWrapper<BookCollection> queryWrapper = Wrappers.<BookCollection>lambdaQuery()
                .eq(BookCollection::getReaderId, bookCollection.getReaderId())
                .eq(BookCollection::getBookId, bookCollection.getBookId());

        long count = bookCollectionMapper.selectCount(queryWrapper);
        if(count > 0){
            return Result.error("您已收藏该图书");
        }

        // 获取图书完整信息
        Book book = bookMapper.selectById(bookCollection.getBookId());
        if(book == null){
            return Result.error("图书不存在");
        }

        bookCollection.setIsbn(book.getIsbn());
        bookCollection.setBookName(book.getName());
        bookCollection.setAuthor(book.getAuthor());
        bookCollection.setCollectionTime(new Date());

        bookCollectionMapper.insert(bookCollection);
        return Result.success();
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/cancel")
    public Result<?> cancelCollection(@RequestParam Long readerId, @RequestParam Long bookId){
        LambdaQueryWrapper<BookCollection> queryWrapper = Wrappers.<BookCollection>lambdaQuery()
                .eq(BookCollection::getReaderId, readerId)
                .eq(BookCollection::getBookId, bookId);

        bookCollectionMapper.delete(queryWrapper);
        return Result.success();
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/checkCollection")
    public Result<?> checkCollection(@RequestParam Long readerId, @RequestParam Long bookId){
        LambdaQueryWrapper<BookCollection> queryWrapper = Wrappers.<BookCollection>lambdaQuery()
                .eq(BookCollection::getReaderId, readerId)
                .eq(BookCollection::getBookId, bookId);

        long count = bookCollectionMapper.selectCount(queryWrapper);
        return Result.success(count > 0);
    }

    /**
     * 获取用户收藏列表（分页）
     */
    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam Long readerId,
                              @RequestParam(defaultValue = "") String search){
        LambdaQueryWrapper<BookCollection> wrapper = Wrappers.<BookCollection>lambdaQuery()
                .eq(BookCollection::getReaderId, readerId)
                .orderByDesc(BookCollection::getCollectionTime);

        // 搜索功能：书名或作者
        if(StringUtils.isNotBlank(search)){
            wrapper.and(w -> w.like(BookCollection::getBookName, search)
                    .or()
                    .like(BookCollection::getAuthor, search));
        }

        Page<BookCollection> collectionPage = bookCollectionMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);
        return Result.success(collectionPage);
    }

    /**
     * 批量删除收藏
     */
    @PostMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestBody java.util.List<Long> ids){
        if(ids == null || ids.isEmpty()){
            return Result.error("请选择要删除的收藏");
        }
        bookCollectionMapper.deleteBatchIds(ids);
        return Result.success();
    }
}