package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.LendRecordDTO;
import com.example.demo.entity.LendRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface LendRecordMapper extends BaseMapper<LendRecord> {

    /**
     * 多表关联查询借阅历史记录（带分页和搜索条件）
     * @param page 分页对象
     * @param bookId 图书ID搜索条件
     * @param bookName 图书名称搜索条件
     * @param readerId 读者ID搜索条件
     * @return 借阅历史记录DTO列表
     */
    @Select("SELECT " +
            "l.reader_id, l.book_id, l.lend_time, l.return_time, " +
            "b.isbn, b.name as bookName, b.author, b.publisher, " +
            "u.nick_name " +
            "FROM lend_record l " +
            "LEFT JOIN book b ON l.book_id = b.id " +
            "LEFT JOIN \"user\" u ON l.reader_id = u.id " +
            "WHERE (#{bookId} IS NULL OR #{bookId} = '' OR CAST(l.book_id AS CHAR) LIKE '%' || #{bookId} || '%') " +
            "AND (#{bookName} IS NULL OR #{bookName} = '' OR b.name LIKE '%' || #{bookName} || '%') " +
            "AND (#{readerId} IS NULL OR #{readerId} = '' OR CAST(l.reader_id AS CHAR) LIKE '%' || #{readerId} || '%')")
    Page<LendRecordDTO> findPageWithDetails(Page<LendRecordDTO> page,
                                           @Param("bookId") String bookId,
                                           @Param("bookName") String bookName,
                                           @Param("readerId") String readerId);
}
