package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

// 借阅历史表实体类
@TableName("lend_record")
@Data
public class LendRecord {
    private Long readerId;     // 读者ID（外键，关联user表）
    private Long bookId;       // 图书ID（外键，关联book表）
    @JsonFormat(locale="zh",timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    private Date lendTime;     // 借阅时间
    @JsonFormat(locale="zh",timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    private Date returnTime;   // 归还时间
    private String status;     // 借阅状态：0-未归还，1-已归还

}
