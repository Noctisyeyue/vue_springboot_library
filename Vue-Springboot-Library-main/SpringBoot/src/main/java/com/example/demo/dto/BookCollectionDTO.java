package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

// 图书收藏DTO - 包含关联查询的图书信息
// 把从多个表查询的数据包装成一个对象，方便返回给前端
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