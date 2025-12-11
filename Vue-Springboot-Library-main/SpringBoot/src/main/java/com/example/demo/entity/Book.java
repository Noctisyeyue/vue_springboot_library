package com.example.demo.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;


import java.math.BigDecimal;
import java.util.Date;
//表明这个类对应数据库中名为 book 的表
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
    private Integer  totalQuantity;//图书总数量
    private Integer  borrowedQuantity;//已借阅数量

    // 计算可借阅数量的方法（非数据库字段）
    public Integer getAvailableQuantity() {
        if (totalQuantity == null || borrowedQuantity == null) {
            return 0;
        }
        return totalQuantity - borrowedQuantity;
    }

}
