package com.example.demo.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;


import java.math.BigDecimal;
import java.util.Date;

/**
 * 图书实体类，对应数据库 book 表
 * 核心用途：封装图书基础信息与可借阅数量计算逻辑
 */
@TableName("book")
@Data
public class Book {

    @TableId (type = IdType.AUTO)
    private Long id;
    private String isbn;
    private String name;
    private BigDecimal price;
    private String author;
    private Integer borrowNum;
    private String publisher;
    @JsonFormat(locale="zh",timezone="GMT+8", pattern="yyyy-MM-dd")
    private Date createTime;
    private String status;
    private Integer totalQuantity;      // 图书总数量
    private Integer borrowedQuantity;   // 已借阅数量
    private String bookPicture;         // 图书图片路径

    /**
     * 计算可借阅数量（非数据库字段）
     * 核心逻辑：总数量减去已借阅数量，处理空值避免空指针异常
     * @return 可借阅数量，如果字段为空则返回0
     */
    public Integer getAvailableQuantity() {
        if (totalQuantity == null || borrowedQuantity == null) {
            return 0;
        }
        return totalQuantity - borrowedQuantity;
    }

}
