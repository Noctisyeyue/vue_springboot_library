package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.BookCollectionDTO;
import com.example.demo.entity.BookCollection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BookCollectionMapper extends BaseMapper<BookCollection> {

    /**
     * 多表关联查询收藏记录（带分页和搜索条件）
     * @param page 分页对象
     * @param readerId 读者ID
     * @param search1 搜索条件（图书名称）
     * @param search2 搜索条件（作者）
     * @return 收藏记录DTO列表
     */
    @Select("SELECT " +
            "c.id, c.reader_id, c.book_id, c.collection_time, " +
            "b.isbn, b.name as bookName, b.author, b.publisher, b.status " +
            "FROM book_collection c " +
            "LEFT JOIN book b ON c.book_id = b.id " +
            "WHERE c.reader_id = #{readerId} " +
            "AND (#{search1} IS NULL OR #{search1} = '' OR b.name LIKE '%' || #{search1} || '%') " +
            "AND (#{search2} IS NULL OR #{search2} = '' OR b.author LIKE '%' || #{search2} || '%') " +
            "ORDER BY c.collection_time DESC")
    Page<BookCollectionDTO> findPageWithBookDetails(Page<BookCollectionDTO> page,
                                                   @Param("readerId") Long readerId,
                                                   @Param("search1") String search1,
                                                   @Param("search2") String search2);
}