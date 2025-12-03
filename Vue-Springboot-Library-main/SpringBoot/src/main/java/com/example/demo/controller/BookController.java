package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.commom.Result;
import com.example.demo.entity.Book;
import com.example.demo.mapper.BookMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/book")
public class BookController {
    @Resource
    BookMapper bookMapper;

    //接收前端传来的一个JSON格式的 Book 对象
    @PostMapping
    public Result<?> save(@RequestBody Book book){
        // 新增图书时初始化字段
        // 如果前端没有传来“被借阅次数”，就默认为 0
        if(book.getBorrowNum() == null){
            book.setBorrowNum(0);
        }
        // 如果没有传来“当前已借出数量”，也默认为 0
        if(book.getBorrowedQuantity() == null){
            book.setBorrowedQuantity(0);
        }
        // 如果没有传来“总库存量”，默认为 1
        if(book.getTotalQuantity() == null){
            book.setTotalQuantity(1);
        }
        // 如果没有设置状态，就默认为 "1" (代表“可借阅”)
        if(book.getStatus() == null || book.getStatus().isEmpty()){
            book.setStatus("1"); // 默认可借阅
        }

        bookMapper.insert(book);
        return Result.success();
    }

    //更新一本图书的信息
    @PutMapping
    public Result<?> update(@RequestBody Book book){
        // 获取数据库中的原始数据
        Book existingBook = bookMapper.selectById(book.getId());
        if(existingBook == null){
            return Result.error("图书不存在");
        }

        // 如果是借阅操作（borrowedQuantity增加）
        if(book.getBorrowedQuantity() != null &&
                book.getBorrowedQuantity() > existingBook.getBorrowedQuantity()){

            // 计算可借阅数量
            int availableQuantity = existingBook.getTotalQuantity() - existingBook.getBorrowedQuantity();

            if(availableQuantity <= 0){
                return Result.error("该图书暂无可借阅库存");
            }

            // 更新状态：如果借完了设为不可借阅
            if(book.getBorrowedQuantity() >= existingBook.getTotalQuantity()){
                book.setStatus("0"); // 不可借阅
            } else {
                book.setStatus("1"); // 可借阅
            }
        }

        // 如果是还书操作（borrowedQuantity减少）
        if(book.getBorrowedQuantity() != null &&
                book.getBorrowedQuantity() < existingBook.getBorrowedQuantity()){

            if(existingBook.getBorrowedQuantity() <= 0){
                return Result.error("没有借阅记录可归还");
            }

            // 还书后一定有库存，设为可借阅
            book.setStatus("1");
        }

        bookMapper.updateById(book);
        return Result.success();
    }

    // 批量删除
    @PostMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestBody List<Integer> ids){
        // 检查是否有图书正在被借阅
        for(Integer id : ids){
            Book book = bookMapper.selectById(id);
            if(book != null && book.getBorrowedQuantity() != null && book.getBorrowedQuantity() > 0){
                return Result.error("图书【" + book.getName() + "】仍有借阅记录，无法删除");
            }
        }

        bookMapper.deleteBatchIds(ids);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id){
        // 检查图书是否正在被借阅
        Book book = bookMapper.selectById(id);
        if(book != null && book.getBorrowedQuantity() != null && book.getBorrowedQuantity() > 0){
            return Result.error("该图书仍有借阅记录，无法删除");
        }

        bookMapper.deleteById(id);
        return Result.success();
    }

    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search1,//模糊匹配 ISBN
                              @RequestParam(defaultValue = "") String search2,//模糊匹配 书名
                              @RequestParam(defaultValue = "") String search3){//模糊匹配 作者
        //查询条件构造器
        LambdaQueryWrapper<Book> wrappers = Wrappers.<Book>lambdaQuery();
        //如果search1有值 就加上一个条件：isbn字段 LIKE %search1%
        if(StringUtils.isNotBlank(search1)){
            wrappers.like(Book::getIsbn,search1);
        }
        if(StringUtils.isNotBlank(search2)){
            wrappers.like(Book::getName,search2);
        }
        if(StringUtils.isNotBlank(search3)){
            wrappers.like(Book::getAuthor,search3);
        }

        Page<Book> bookPage = bookMapper.selectPage(new Page<>(pageNum,pageSize), wrappers);
        return Result.success(bookPage);
    }

    // 新增：获取图书详情
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id){
        Book book = bookMapper.selectById(id);
        if(book == null){
            return Result.error("图书不存在");
        }
        return Result.success(book);
    }
}