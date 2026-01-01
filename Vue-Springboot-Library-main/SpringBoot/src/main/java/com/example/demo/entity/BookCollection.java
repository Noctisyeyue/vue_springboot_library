package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 图书收藏实体类，对应数据库 book_collection 表
 * 核心用途：记录用户收藏的图书信息，支持收藏和取消收藏功能
 */
@TableName("book_collection")
@Data
public class BookCollection {
    @TableId(type = IdType.AUTO)
    private Long id;           // 收藏记录ID（主键）
    private Long readerId;     // 读者ID（外键，关联user表）
    private Long bookId;       // 图书ID（外键，关联book表）
    @JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    private Date collectionTime; // 收藏时间
}