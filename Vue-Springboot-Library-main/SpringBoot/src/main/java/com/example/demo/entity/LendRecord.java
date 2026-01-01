package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 借阅历史实体类，对应数据库 lend_record 表
 * 核心用途：记录所有借阅历史（已归还和未归还），使用复合主键(reader_id, book_id, lend_time)
 */
@TableName("lend_record")
@Data
public class LendRecord {
    private Long readerId;     // 读者ID（外键，关联user表），复合主键之一
    private Long bookId;       // 图书ID（外键，关联book表），复合主键之一
    @JsonFormat(locale="zh",timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    private Date lendTime;     // 借阅时间，复合主键之一
    @JsonFormat(locale="zh",timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    private Date returnTime;   // 归还时间
    private String status;     // 借阅状态：0-未归还，1-已归还

    @TableField(exist = false)  // 非数据库字段，用于前端传输数据
    private String isbn;       // 图书编号（用于归还时查询bookId）

}
