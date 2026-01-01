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

/**
 * 图书管理控制器
 * 核心用途：提供图书的增删改查、分页查询、热门推荐等接口
 */
@RestController
@RequestMapping("/book")
public class BookController {
    @Resource
    BookMapper bookMapper;

    /**
     * 新增图书（含字段初始化与默认值设置）
     * 核心逻辑：验证必填字段（ISBN、书名），检查ISBN唯一性，设置借阅次数、已借数量、总库存、状态的默认值
     * @param book 图书对象，包含ISBN、书名、作者等基本信息
     * @return Result 统一响应对象，成功返回success，失败返回错误信息
     */
    @PostMapping
    public Result<?> save(@RequestBody Book book){
        // 验证必填字段
        if(book.getIsbn() == null || book.getIsbn().trim().isEmpty()){
            return Result.error("图书编号（ISBN）不能为空");
        }
        if(book.getName() == null || book.getName().trim().isEmpty()){
            return Result.error("图书名称不能为空");
        }

        // 检查ISBN是否已存在
        Book existingBook = bookMapper.selectOne(Wrappers.<Book>lambdaQuery().eq(Book::getIsbn, book.getIsbn()));
        if(existingBook != null){
            return Result.error("图书编号（ISBN）已存在，请勿重复添加");
        }

        // 初始化字段默认值
        if(book.getBorrowNum() == null){
            book.setBorrowNum(0);
        }
        if(book.getBorrowedQuantity() == null){
            book.setBorrowedQuantity(0);
        }
        if(book.getTotalQuantity() == null){
            book.setTotalQuantity(1);
        }
        if(book.getStatus() == null || book.getStatus().isEmpty()){
            book.setStatus("1");  // 默认可借阅
        }

        bookMapper.insert(book);
        return Result.success();
    }

    /**
     * 更新图书信息（含借阅/还书状态自动更新）
     * 核心逻辑：根据已借数量变化自动更新图书可借状态，借阅时检查库存，还书时恢复可借状态
     * 如果修改了ISBN，检查是否与其他图书重复
     * @param book 图书对象，包含需要更新的字段
     * @return Result 统一响应对象，成功返回success，失败返回错误信息
     */
    @PutMapping
    public Result<?> update(@RequestBody Book book){
        Book existingBook = bookMapper.selectById(book.getId());
        if(existingBook == null){
            return Result.error("图书不存在");
        }

        // 如果修改了ISBN，检查是否与其他图书重复
        if (book.getIsbn() != null && !book.getIsbn().trim().isEmpty() && book.getId() != null) {
            // 如果ISBN确实发生了变化，才进行检查
            if (!book.getIsbn().equals(existingBook.getIsbn())) {
                LambdaQueryWrapper<Book> queryWrapper = Wrappers.<Book>lambdaQuery()
                        .eq(Book::getIsbn, book.getIsbn())
                        .ne(Book::getId, book.getId()); // 排除当前图书自己
                Book duplicateBook = bookMapper.selectOne(queryWrapper);
                if (duplicateBook != null) {
                    return Result.error("图书编号（ISBN）已存在，请勿重复使用");
                }
            }
        }

        // 借阅操作：已借数量增加
        if(book.getBorrowedQuantity() != null &&
                book.getBorrowedQuantity() > existingBook.getBorrowedQuantity()){
            int availableQuantity = existingBook.getTotalQuantity() - existingBook.getBorrowedQuantity();
            if(availableQuantity <= 0){
                return Result.error("该图书暂无可借阅库存");
            }
            // 根据库存情况更新状态
            book.setStatus(book.getBorrowedQuantity() >= existingBook.getTotalQuantity() ? "0" : "1");
        }

        // 还书操作：已借数量减少
        if(book.getBorrowedQuantity() != null &&
                book.getBorrowedQuantity() < existingBook.getBorrowedQuantity()){
            if(existingBook.getBorrowedQuantity() <= 0){
                return Result.error("没有借阅记录可归还");
            }
            book.setStatus("1");  // 还书后恢复可借状态
        }

        bookMapper.updateById(book);
        return Result.success();
    }

    /**
     * 批量删除图书（含借阅记录检查）
     * 核心逻辑：检查图书是否有未归还记录，有则不允许删除
     * @param ids 图书ID列表
     * @return Result 统一响应对象，成功返回success，失败返回错误信息
     */
    @PostMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestBody List<Integer> ids){
        for(Integer id : ids){
            Book book = bookMapper.selectById(id);
            if(book != null && book.getBorrowedQuantity() != null && book.getBorrowedQuantity() > 0){
                return Result.error("图书【" + book.getName() + "】仍有借阅记录，无法删除");
            }
        }
        bookMapper.deleteBatchIds(ids);
        return Result.success();
    }

    /**
     * 删除单本图书（含借阅记录检查）
     * 核心逻辑：检查图书是否有未归还记录，有则不允许删除
     * @param id 图书ID
     * @return Result 统一响应对象，成功返回success，失败返回错误信息
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id){
        Book book = bookMapper.selectById(id);
        if(book != null && book.getBorrowedQuantity() != null && book.getBorrowedQuantity() > 0){
            return Result.error("该图书仍有借阅记录，无法删除");
        }
        bookMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 分页查询图书列表（支持多条件模糊搜索）
     * 核心逻辑：动态构建查询条件，支持按ISBN、书名、作者模糊查询
     * @param pageNum 页码，默认值为1
     * @param pageSize 每页条数，默认值为10
     * @param search1 模糊匹配参数：图书ISBN
     * @param search2 模糊匹配参数：图书名称
     * @param search3 模糊匹配参数：图书作者
     * @return Result<Page<Book>> 包含分页信息的图书列表响应
     */
    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search1,
                              @RequestParam(defaultValue = "") String search2,
                              @RequestParam(defaultValue = "") String search3){
        LambdaQueryWrapper<Book> wrappers = Wrappers.<Book>lambdaQuery();
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

    /**
     * 获取图书详情
     * @param id 图书ID
     * @return Result<Book> 图书详细信息，不存在返回错误信息
     */
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id){
        Book book = bookMapper.selectById(id);
        if(book == null){
            return Result.error("图书不存在");
        }
        return Result.success(book);
    }

    /**
     * 获取借阅量最高的前N本书（用于首页推荐）
     * 核心逻辑：按借阅次数降序排列，使用分页查询实现TOP N
     * @param limit 返回数量，默认值为5
     * @return Result<List<Book>> 热门图书列表
     */
    @GetMapping("/top")
    public Result<?> getTopBooks(@RequestParam(defaultValue = "5") Integer limit){
        Page<Book> page = new Page<>(1, limit);
        LambdaQueryWrapper<Book> wrapper = Wrappers.<Book>lambdaQuery();
        wrapper.orderByDesc(Book::getBorrowNum);
        Page<Book> bookPage = bookMapper.selectPage(page, wrapper);
        return Result.success(bookPage.getRecords());
    }
}