package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 当前借阅实体类，对应数据库 bookwithuser 表
 * 核心用途：记录用户当前未归还的借阅信息，支持续借功能
 */
@TableName("bookwithuser")
@Data
public class BookWithUser {
    @TableId(type = IdType.AUTO)
    private Long recordId;     // 借阅记录ID（主键）
    private Long readerId;     // 读者ID（外键，关联user表）
    private Long bookId;       // 图书ID（外键，关联book表）
    @JsonFormat(locale="zh",timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    private Date lendTime;     // 借阅时间
    @JsonFormat(locale="zh",timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    private Date deadTime;     // 应归还时间
    private Integer prolong;   // 续借次数
}
