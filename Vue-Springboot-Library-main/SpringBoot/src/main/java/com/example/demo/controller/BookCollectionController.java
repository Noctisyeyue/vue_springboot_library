package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.commom.Result;
import com.example.demo.dto.BookCollectionDTO;
import com.example.demo.entity.Book;
import com.example.demo.entity.BookCollection;
import com.example.demo.mapper.BookCollectionMapper;
import com.example.demo.mapper.BookMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 图书收藏管理控制器
 * 核心用途：提供图书收藏、取消收藏、收藏列表查询等接口
 */
@RestController
@RequestMapping("/bookCollection")
public class BookCollectionController {
    @Resource
    BookCollectionMapper bookCollectionMapper;

    @Resource
    BookMapper bookMapper;

    /**
     * 添加收藏（含重复收藏检查与图书存在性验证）
     * 核心逻辑：检查是否已收藏、图书是否存在，设置收藏时间后插入记录
     * @param bookCollection 收藏记录对象，包含readerId（读者ID）、bookId（图书ID）
     * @return Result 统一响应对象，成功返回success，失败返回错误信息
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

        // 检查图书是否存在
        Book book = bookMapper.selectById(bookCollection.getBookId());
        if(book == null){
            return Result.error("图书不存在");
        }

        bookCollection.setCollectionTime(new Date());
        bookCollectionMapper.insert(bookCollection);
        return Result.success();
    }

    /**
     * 取消收藏
     * @param readerId 读者ID
     * @param bookId 图书ID
     * @return Result 统一响应对象，成功返回success
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
     * @param readerId 读者ID
     * @param bookId 图书ID
     * @return Result<Boolean> true表示已收藏，false表示未收藏
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
     * 分页查询用户收藏列表（多表关联查询）
     * 核心逻辑：使用多表关联查询，返回包含图书详细信息的DTO
     * @param pageNum 页码，默认值为1
     * @param pageSize 每页条数，默认值为10
     * @param readerId 读者ID（必填）
     * @param search1 模糊匹配参数：图书ISBN
     * @param search2 模糊匹配参数：图书名称
     * @return Result<Page<BookCollectionDTO>> 包含分页信息的收藏列表响应
     */
    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam Long readerId,
                              @RequestParam(defaultValue = "") String search1,
                              @RequestParam(defaultValue = "") String search2){
        Page<BookCollectionDTO> collectionPage = bookCollectionMapper.findPageWithBookDetails(
                new Page<>(pageNum, pageSize), readerId, search1, search2);
        return Result.success(collectionPage);
    }

    /**
     * 批量删除收藏
     * @param ids 收藏记录ID列表
     * @return Result 统一响应对象，成功返回success，失败返回错误信息
     */
    @PostMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestBody List<Long> ids){
        if(ids == null || ids.isEmpty()){
            return Result.error("请选择要删除的收藏");
        }
        bookCollectionMapper.deleteBatchIds(ids);
        return Result.success();
    }
}