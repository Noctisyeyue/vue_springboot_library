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
     * @param search 搜索条件（图书名称或作者）
     * @return 收藏记录DTO列表
     */
    @Select("SELECT " +
            "c.id, c.reader_id, c.book_id, c.collection_time, " +
            "b.isbn, b.name as bookName, b.author, b.publisher, b.status " +
            "FROM book_collection c " +
            "LEFT JOIN book b ON c.book_id = b.id " +
            "WHERE c.reader_id = #{readerId} " +
            "AND (#{search} IS NULL OR #{search} = '' OR b.name LIKE '%' || #{search} || '%' OR b.author LIKE '%' || #{search} || '%') " +
            "ORDER BY c.collection_time DESC")
    Page<BookCollectionDTO> findPageWithBookDetails(Page<BookCollectionDTO> page,
                                                   @Param("readerId") Long readerId,
                                                   @Param("search") String search);
}