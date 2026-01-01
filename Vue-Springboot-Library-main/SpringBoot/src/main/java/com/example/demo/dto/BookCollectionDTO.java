package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 图书收藏DTO
 * 核心用途：封装多表关联查询结果，包含收藏记录、图书信息，便于前端展示
 */
@Data
public class BookCollectionDTO {
    private Long id;           // 收藏记录ID
    private Long readerId;     // 读者ID
    private Long bookId;       // 图书ID

    // 图书信息（从book表关联查询）
    private String isbn;       // 图书编号
    private String bookName;   // 图书名称
    private String author;     // 作者
    private String publisher;  // 出版社
    private String status;     // 图书状态

    // 收藏信息
    @JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    private Date collectionTime; // 收藏时间
}