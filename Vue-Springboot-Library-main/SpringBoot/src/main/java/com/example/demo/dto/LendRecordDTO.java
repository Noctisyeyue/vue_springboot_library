package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

// 借阅历史记录DTO - 包含关联查询的图书信息和用户信息
// 把从多个表查询的数据包装成一个对象，方便返回给前端
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