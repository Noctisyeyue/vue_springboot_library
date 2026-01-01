package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 访问统计实体类，对应数据库 visit_stats 表
 * 核心用途：记录系统总访问量，用于Dashboard数据统计展示
 */
@Data
@TableName("visit_stats")
public class VisitStats {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Long totalVisits;  // 总访问量
}