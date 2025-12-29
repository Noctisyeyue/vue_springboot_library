package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.BookWithUserDTO;
import com.example.demo.entity.BookWithUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface BookWithUserMapper extends BaseMapper<BookWithUser> {

    /**
     * 多表关联查询借阅记录（带分页和搜索条件）
     * @param page 分页对象
     * @param isbn 图书编号搜索条件
     * @param bookName 图书名称搜索条件
     * @param readerId 读者ID搜索条件
     * @return 借阅记录DTO列表
     */
    @Select("SELECT " +
            "b.record_id, b.reader_id, b.book_id, b.lend_time, b.dead_time, b.prolong, " +
            "bk.ISBN as isbn, bk.name as bookName, bk.author, bk.publisher, " +
            "u.nick_name " +
            "FROM bookwithuser b " +
            "LEFT JOIN book bk ON b.book_id = bk.id " +
            "LEFT JOIN \"user\" u ON b.reader_id = u.id " +
            "WHERE (#{isbn} IS NULL OR #{isbn} = '' OR bk.ISBN LIKE '%' || #{isbn} || '%') " +
            "AND (#{bookName} IS NULL OR #{bookName} = '' OR bk.name LIKE '%' || #{bookName} || '%') " +
            "AND (#{readerId} IS NULL OR #{readerId} = '' OR TO_CHAR(b.reader_id) LIKE '%' || #{readerId} || '%')")
    Page<BookWithUserDTO> findPageWithDetails(Page<BookWithUserDTO> page,
                                            @Param("isbn") String isbn,
                                            @Param("bookName") String bookName,
                                            @Param("readerId") String readerId);
}
