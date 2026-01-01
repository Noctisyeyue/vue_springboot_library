package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 借阅历史记录DTO
 * 核心用途：封装多表关联查询结果，包含借阅历史、图书信息、用户信息，便于前端展示
 */
@Data
public class LendRecordDTO {
    private Long readerId;     // 读者ID
    private Long bookId;       // 图书ID

    // 图书信息（从book表关联查询）
    private String isbn;       // 图书编号
    private String bookName;   // 图书名称
    private String author;     // 作者
    private String publisher;  // 出版社

    // 用户信息（从user表关联查询）
    private String nickName;   // 读者姓名

    // 借阅历史信息
    @JsonFormat(locale="zh",timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    private Date lendTime;     // 借阅时间
    @JsonFormat(locale="zh",timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    private Date returnTime;   // 归还时间
    private String status;     // 借阅状态：0-未归还，1-已归还
}