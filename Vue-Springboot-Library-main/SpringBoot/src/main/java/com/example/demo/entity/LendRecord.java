package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 借阅历史表实体类
 * 表 lend_record 使用复合主键：reader_id, book_id, lend_time
 * 没有自增主键 id 字段
 */
@TableName("lend_record")
@Data
public class LendRecord {
    // 使用复合主键 (reader_id, book_id, lend_time)
    private Long readerId;     // 读者ID（外键，关联user表），复合主键之一
    private Long bookId;       // 图书ID（外键，关联book表），复合主键之一
    @JsonFormat(locale="zh",timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    private Date lendTime;     // 借阅时间，复合主键之一
    @JsonFormat(locale="zh",timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    private Date returnTime;   // 归还时间
    private String status;     // 借阅状态：0-未归还，1-已归还

    // 非数据库字段，用于前端传输数据
    @TableField(exist = false)
    private String isbn;       // 图书编号（用于归还时查询bookId）

}
