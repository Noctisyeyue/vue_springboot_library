package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

// 3NF规范化后的借阅历史表实体类 - 只包含关联字段
@TableName("lend_record")
@Data
public class LendRecord {
    private Long readerId;     // 读者ID（外键，关联user表）
    private Long bookId;       // 图书ID（外键，关联book表）
    @JsonFormat(locale="zh",timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    private Date lendTime;     // 借阅时间
    @JsonFormat(locale="zh",timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    private Date returnTime;   // 归还时间（null表示未归还）

}
