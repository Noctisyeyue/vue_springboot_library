package com.example.demo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.demo.entity.VisitStats;
import com.example.demo.mapper.VisitStatsMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class LoginUser {

    @Resource
    private VisitStatsMapper visitStatsMapper;  //非静态依赖

    private static LoginUser loginUser; //静态引用

    // 备用：内存中的访问量（数据库不可用时使用）
    private static int backupVisitCount = 0;

    @PostConstruct  //在依赖注入完成后自动执行
    public void init() {
        loginUser = this;
        // 初始化数据库记录（如果不存在）
        try {
            VisitStats stats = visitStatsMapper.selectById(1);
            if (stats == null) {
                // 插入初始记录
                VisitStats newStats = new VisitStats();
                newStats.setTotalVisits(0L);
                visitStatsMapper.insert(newStats);
            }
        } catch (Exception e) {
            System.err.println("初始化访问统计失败: " + e.getMessage());
        }
    }

    /**
     * 增加访问量
     */
    public static void addVisitCount() {
        try {
            if (loginUser != null && loginUser.visitStatsMapper != null) {
                UpdateWrapper<VisitStats> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", 1)
                            .setSql("total_visits = total_visits + 1");
                loginUser.visitStatsMapper.update(null, updateWrapper);
            } else {
                // 备用方案：内存计数
                backupVisitCount++;
            }
        } catch (Exception e) {
            // 数据库操作失败，使用内存备用方案
            backupVisitCount++;
            System.err.println("访问量更新失败，使用内存备用: " + e.getMessage());
        }
    }

    /**
     * 获取访问量
     */
    public static int getVisitCount() {
        try {
            if (loginUser != null && loginUser.visitStatsMapper != null) {
                VisitStats stats = loginUser.visitStatsMapper.selectById(1);
                return stats != null ? stats.getTotalVisits().intValue() : backupVisitCount;
            } else {
                // 备用方案：内存计数
                return backupVisitCount;
            }
        } catch (Exception e) {
            // 数据库操作失败，使用内存备用方案
            return backupVisitCount;
        }
    }
}
