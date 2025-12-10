package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

// 3NF规范化后的图书收藏表实体类 - 只包含关联字段
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