package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.BookWithUserDTO;
import com.example.demo.entity.BookWithUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 当前借阅数据访问接口
 * 核心用途：提供当前借阅记录的基础CRUD操作和多表关联查询
 */
public interface BookWithUserMapper extends BaseMapper<BookWithUser> {

    /**
     * 多表关联查询借阅记录（带分页和搜索条件）
     * 核心逻辑：LEFT JOIN关联book表和user表，支持按ISBN、书名、借阅者昵称模糊查询
     * @param page 分页对象，包含页码和每页条数
     * @param isbn 图书编号搜索条件，为空则不限制
     * @param bookName 图书名称搜索条件，为空则不限制
     * @param nickName 借阅者昵称搜索条件，为空则不限制
     * @return Page<BookWithUserDTO> 包含分页信息的借阅记录DTO列表
     */
    @Select("SELECT " +
            "b.record_id, b.reader_id, b.book_id, b.lend_time, b.dead_time, b.prolong, " +
            "bk.isbn, bk.name as bookName, bk.author, bk.publisher, " +
            "u.nick_name " +
            "FROM bookwithuser b " +
            "LEFT JOIN book bk ON b.book_id = bk.id " +
            "LEFT JOIN \"user\" u ON b.reader_id = u.id " +
            "WHERE (#{isbn} IS NULL OR #{isbn} = '' OR bk.isbn LIKE '%' || #{isbn} || '%') " +
            "AND (#{bookName} IS NULL OR #{bookName} = '' OR bk.name LIKE '%' || #{bookName} || '%') " +
            "AND (#{nickName} IS NULL OR #{nickName} = '' OR u.nick_name LIKE '%' || #{nickName} || '%')")
    Page<BookWithUserDTO> findPageWithDetails(Page<BookWithUserDTO> page,
                                            @Param("isbn") String isbn,
                                            @Param("bookName") String bookName,
                                            @Param("nickName") String nickName);
}
