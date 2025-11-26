package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("visit_stats")
public class VisitStats {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Long totalVisits;
}