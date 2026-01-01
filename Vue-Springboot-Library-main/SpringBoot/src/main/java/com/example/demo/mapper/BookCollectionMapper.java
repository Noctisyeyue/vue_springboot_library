package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.BookCollectionDTO;
import com.example.demo.entity.BookCollection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 图书收藏数据访问接口
 * 核心用途：提供图书收藏记录的基础CRUD操作和多表关联查询
 */
@Mapper
public interface BookCollectionMapper extends BaseMapper<BookCollection> {

    /**
     * 多表关联查询收藏记录（带分页和搜索条件）
     * 核心逻辑：LEFT JOIN关联book表，支持按图书名称、作者模糊查询，按收藏时间降序排列
     * @param page 分页对象，包含页码和每页条数
     * @param readerId 读者ID（必填），用于查询指定用户的收藏记录
     * @param search1 模糊匹配参数：图书名称，为空则不限制
     * @param search2 模糊匹配参数：图书作者，为空则不限制
     * @return Page<BookCollectionDTO> 包含分页信息的收藏记录DTO列表
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