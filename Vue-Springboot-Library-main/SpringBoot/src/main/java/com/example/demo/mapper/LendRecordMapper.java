package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.LendRecordDTO;
import com.example.demo.entity.LendRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.Date;

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
            "l.reader_id, l.book_id, l.lend_time, l.return_time, l.status, " +
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

    /**
     * 归还图书：更新借阅记录的归还时间
     * 使用原生SQL避免Oracle TIMESTAMP类型转换问题
     * @param readerId 读者ID
     * @param bookId 图书ID
     * @param lendTime 借阅时间
     * @param returnTime 归还时间
     * @return 更新结果
     */
    @Update("UPDATE lend_record SET return_time = #{returnTime}, status = '1' " +
            "WHERE reader_id = #{readerId} AND book_id = #{bookId} AND lend_time = #{lendTime}")
    int updateReturnTime(@Param("readerId") Long readerId,
                        @Param("bookId") Long bookId,
                        @Param("lendTime") Date lendTime,
                        @Param("returnTime") Date returnTime);
}
