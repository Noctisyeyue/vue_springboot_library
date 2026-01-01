package com.example.demo.utils;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.demo.entity.VisitStats;
import com.example.demo.mapper.VisitStatsMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 访问统计工具类
 * 核心用途：管理系统总访问量统计，支持数据库存储和内存备用方案
 */
@Component
public class LoginUser {

    @Resource
    private VisitStatsMapper visitStatsMapper;

    private static LoginUser loginUser;

    // 备用方案：内存中的访问量（数据库不可用时使用）
    private static int backupVisitCount = 0;

    /**
     * 初始化方法：在依赖注入完成后自动执行
     * 核心逻辑：初始化访问统计数据库记录，如果不存在则创建
     */
    @PostConstruct
    public void init() {
        loginUser = this;
        try {
            VisitStats stats = visitStatsMapper.selectById(1);
            if (stats == null) {
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
     * 核心逻辑：数据库访问量+1，数据库不可用时使用内存备用方案
     */
    public static void addVisitCount() {
        try {
            if (loginUser != null && loginUser.visitStatsMapper != null) {
                UpdateWrapper<VisitStats> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", 1)
                            .setSql("total_visits = total_visits + 1");
                loginUser.visitStatsMapper.update(null, updateWrapper);
            } else {
                backupVisitCount++;
            }
        } catch (Exception e) {
            backupVisitCount++;
            System.err.println("访问量更新失败，使用内存备用: " + e.getMessage());
        }
    }

    /**
     * 获取访问量
     * 核心逻辑：从数据库查询访问量，数据库不可用时返回内存备用值
     * @return int 总访问量
     */
    public static int getVisitCount() {
        try {
            if (loginUser != null && loginUser.visitStatsMapper != null) {
                VisitStats stats = loginUser.visitStatsMapper.selectById(1);
                return stats != null ? stats.getTotalVisits().intValue() : backupVisitCount;
            } else {
                return backupVisitCount;
            }
        } catch (Exception e) {
            return backupVisitCount;
        }
    }
}
