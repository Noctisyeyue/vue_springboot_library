package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.LendRecordDTO;
import com.example.demo.entity.LendRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.Date;

/**
 * 借阅历史数据访问接口
 * 核心用途：提供借阅历史记录的基础CRUD操作和多表关联查询
 */
public interface LendRecordMapper extends BaseMapper<LendRecord> {

    /**
     * 多表关联查询借阅历史记录（带分页和搜索条件）
     * 核心逻辑：LEFT JOIN关联book表和user表，支持按图书ISBN、书名、读者ID模糊查询
     * @param page 分页对象，包含页码和每页条数
     * @param isbn 图书ISBN搜索条件，为空则不限制
     * @param bookName 图书名称搜索条件，为空则不限制
     * @param readerId 读者ID搜索条件，为空则不限制
     * @return Page<LendRecordDTO> 包含分页信息的借阅历史记录DTO列表
     */
    @Select("SELECT " +
            "l.reader_id, l.book_id, l.lend_time, l.return_time, l.status, " +
            "b.isbn, b.name as bookName, b.author, b.publisher, " +
            "u.nick_name " +
            "FROM lend_record l " +
            "LEFT JOIN book b ON l.book_id = b.id " +
            "LEFT JOIN \"user\" u ON l.reader_id = u.id " +
            "WHERE (#{isbn} IS NULL OR #{isbn} = '' OR b.isbn LIKE '%' || #{isbn} || '%') " +
            "AND (#{bookName} IS NULL OR #{bookName} = '' OR b.name LIKE '%' || #{bookName} || '%') " +
            "AND (#{readerId} IS NULL OR #{readerId} = '' OR TO_CHAR(l.reader_id) LIKE '%' || #{readerId} || '%')")
    Page<LendRecordDTO> findPageWithDetails(Page<LendRecordDTO> page,
                                           @Param("isbn") String isbn,
                                           @Param("bookName") String bookName,
                                           @Param("readerId") String readerId);

    /**
     * 归还图书：更新借阅记录的归还时间和状态
     * 核心逻辑：使用复合主键(reader_id, book_id, lend_time)更新记录，设置status为'1'表示已归还
     * @param readerId 读者ID，复合主键之一
     * @param bookId 图书ID，复合主键之一
     * @param lendTime 借阅时间，复合主键之一
     * @param returnTime 归还时间
     * @return int 更新的记录数，大于0表示更新成功
     */
    @Update("UPDATE lend_record SET return_time = #{returnTime}, status = '1' " +
            "WHERE reader_id = #{readerId} AND book_id = #{bookId} AND lend_time = #{lendTime}")
    int updateReturnTime(@Param("readerId") Long readerId,
                        @Param("bookId") Long bookId,
                        @Param("lendTime") Date lendTime,
                        @Param("returnTime") Date returnTime);
}
