/*
 图书管理系统数据库 - 符合3NF设计的MySQL版本
 基于1.md Oracle版本设计修改
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user (用户表)
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `nick_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '姓名',
  `phone` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话号码',
  `sex` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '性别',
  `address` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址',
  `role` INT NOT NULL COMMENT '角色、1：管理员 2：普通用户',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_username` (`username`) USING BTREE COMMENT '用户名唯一约束',
  UNIQUE KEY `uk_phone` (`phone`) USING BTREE COMMENT '电话号码唯一约束'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for book (图书表)
-- ----------------------------
DROP TABLE IF EXISTS `book`;
CREATE TABLE `book` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `isbn` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图书编号',
  `name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
  `price` DECIMAL(10, 2) NULL DEFAULT NULL COMMENT '价格',
  `author` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '作者',
  `publisher` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '出版社',
  `create_time` DATE NULL DEFAULT NULL COMMENT '出版时间',
  `book_picture` VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片',
  `status` VARCHAR(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '0：不可借阅 1：可借阅',
  `borrow_num` INT NOT NULL DEFAULT 0 COMMENT '此书被借阅次数',
  `total_quantity` INT NOT NULL DEFAULT 1 COMMENT '图书总数量',
  `borrowed_quantity` INT NOT NULL DEFAULT 0 COMMENT '已借阅数量',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_isbn` (`isbn`) USING BTREE COMMENT 'ISBN唯一约束'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '图书信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bookwithuser (当前借阅表)
-- ----------------------------
DROP TABLE IF EXISTS `bookwithuser`;
CREATE TABLE `bookwithuser` (
  `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '借阅记录ID（主键）',
  `reader_id` BIGINT NOT NULL COMMENT '读者id',
  `book_id` BIGINT NOT NULL COMMENT '图书id',
  `lend_time` DATETIME NULL DEFAULT NULL COMMENT '借阅时间',
  `dead_time` DATETIME NULL DEFAULT NULL COMMENT '应归还时间',
  `prolong` INT NULL DEFAULT NULL COMMENT '续借次数',
  PRIMARY KEY (`record_id`) USING BTREE,
  KEY `fk_bookwithuser_user` (`reader_id`) USING BTREE,
  KEY `fk_bookwithuser_book` (`book_id`) USING BTREE,
  CONSTRAINT `fk_bookwithuser_user` FOREIGN KEY (`reader_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_bookwithuser_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '当前借阅表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for lend_record (借阅历史表)
-- ----------------------------
DROP TABLE IF EXISTS `lend_record`;
CREATE TABLE `lend_record` (
  `reader_id` BIGINT NOT NULL COMMENT '读者id',
  `book_id` BIGINT NOT NULL COMMENT '图书id',
  `lend_time` DATETIME NOT NULL COMMENT '借书日期',
  `return_time` DATETIME NULL DEFAULT NULL COMMENT '还书日期',
  `status` VARCHAR(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '0：未归还 1：已归还',
  PRIMARY KEY (`reader_id`, `book_id`, `lend_time`) USING BTREE,
  KEY `fk_lendrecord_user` (`reader_id`) USING BTREE,
  KEY `fk_lendrecord_book` (`book_id`) USING BTREE,
  CONSTRAINT `fk_lendrecord_user` FOREIGN KEY (`reader_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_lendrecord_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '借阅历史表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for book_collection (图书收藏表)
-- ----------------------------
DROP TABLE IF EXISTS `book_collection`;
CREATE TABLE `book_collection` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏记录ID',
  `reader_id` BIGINT NOT NULL COMMENT '读者ID',
  `book_id` BIGINT NOT NULL COMMENT '图书ID',
  `collection_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_book` (`reader_id`, `book_id`) USING BTREE COMMENT '同一用户同一本书只能收藏一次',
  KEY `fk_collection_user` (`reader_id`) USING BTREE,
  KEY `fk_collection_book` (`book_id`) USING BTREE,
  CONSTRAINT `fk_collection_user` FOREIGN KEY (`reader_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_collection_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '图书收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for visit_stats (访问统计表)
-- ----------------------------
DROP TABLE IF EXISTS `visit_stats`;
CREATE TABLE `visit_stats` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `total_visits` BIGINT NOT NULL DEFAULT 0 COMMENT '总访问量',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '访问统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user (用户数据)
-- ----------------------------
INSERT INTO `user` (`id`, `username`, `password`, `nick_name`, `phone`, `sex`, `address`, `role`) VALUES
(1, 'admin', '123', '管理员', '13800138000', '男', '北京市朝阳区', 1),
(2, 'user1', '123', '张三', '13800138001', '男', '上海市浦东新区', 2),
(3, 'user2', '123456', '李四', '13800138002', '女', '广州市天河区', 2),
(4, 'user3', '123456', '王五', '13800138003', '男', '深圳市南山区', 2),
(5, 'user4', '123456', '赵六', '13800138004', '女', '杭州市西湖区', 2);

-- ----------------------------
-- Records of book (图书数据)
-- ----------------------------
INSERT INTO `book` (`id`, `isbn`, `name`, `price`, `author`, `publisher`, `create_time`, `status`, `borrow_num`, `total_quantity`, `borrowed_quantity`) VALUES
(1, '9787020002207', '红楼梦', 59.70, '曹雪芹', '人民文学出版社', '2008-06-01', '1', 15, 10, 2),
(2, '9787020002208', '西游记', 45.00, '吴承恩', '人民文学出版社', '2008-06-01', '1', 12, 8, 1),
(3, '9787020002209', '水浒传', 52.30, '施耐庵', '人民文学出版社', '2008-06-01', '1', 10, 6, 0),
(4, '9787020002210', '三国演义', 48.90, '罗贯中', '人民文学出版社', '2008-06-01', '1', 18, 12, 3),
(5, '9787108005460', '围城', 39.50, '钱钟书', '人民文学出版社', '2010-01-01', '1', 8, 5, 1),
(6, '9787020024759', '活着', 35.00, '余华', '作家出版社', '2012-08-01', '1', 20, 15, 4),
(7, '9787530215009', '百年孤独', 55.00, '加西亚·马尔克斯', '南海出版公司', '2017-08-01', '1', 6, 4, 0),
(8, '9787020093456', '挪威的森林', 42.00, '村上春树', '人民文学出版社', '2018-03-01', '1', 9, 7, 2),
(9, '9787506347168', '小王子', 22.00, '安托万·德·圣埃克苏佩里', '人民文学出版社', '2003-08-01', '1', 25, 20, 5),
(10, '9787544217686', '追风筝的人', 36.00, '卡勒德·胡赛尼', '上海人民出版社', '2013-05-01', '1', 11, 8, 1);

-- ----------------------------
-- Records of bookwithuser (当前借阅数据)
-- ----------------------------
INSERT INTO `bookwithuser` (`record_id`, `reader_id`, `book_id`, `lend_time`, `dead_time`, `prolong`) VALUES
(1, 2, 1, '2024-12-01 10:30:00', '2024-12-15 10:30:00', 0),
(2, 2, 5, '2024-12-01 10:35:00', '2024-12-15 10:35:00', 1),
(3, 3, 6, '2024-12-02 14:20:00', '2024-12-16 14:20:00', 0),
(4, 4, 8, '2024-12-03 09:15:00', '2024-12-17 09:15:00', 0),
(5, 5, 10, '2024-12-03 16:45:00', '2024-12-17 16:45:00', 2);

-- ----------------------------
-- Records of lend_record (借阅历史数据)
-- ----------------------------
INSERT INTO `lend_record` (`reader_id`, `book_id`, `lend_time`, `return_time`, `status`) VALUES
(2, 1, '2024-11-01 10:30:00', '2024-11-15 14:20:00', '1'),
(2, 2, '2024-10-15 09:00:00', '2024-10-29 16:30:00', '1'),
(2, 5, '2024-11-10 11:15:00', '2024-11-24 10:45:00', '1'),
(3, 3, '2024-11-05 14:30:00', '2024-11-19 15:20:00', '1'),
(3, 6, '2024-12-02 14:20:00', NULL, '0'),
(4, 4, '2024-11-20 08:45:00', '2024-12-04 09:30:00', '1'),
(4, 7, '2024-10-25 13:15:00', '2024-11-08 14:00:00', '1'),
(4, 8, '2024-12-03 09:15:00', NULL, '0'),
(5, 9, '2024-11-08 15:30:00', '2024-11-22 16:15:00', '1'),
(5, 10, '2024-12-03 16:45:00', NULL, '0');

-- ----------------------------
-- Records of book_collection (收藏数据)
-- ----------------------------
INSERT INTO `book_collection` (`id`, `reader_id`, `book_id`, `collection_time`) VALUES
(1, 2, 1, '2024-11-01 11:00:00'),
(2, 2, 3, '2024-11-10 15:30:00'),
(3, 3, 4, '2024-11-05 16:20:00'),
(4, 3, 7, '2024-11-06 09:15:00'),
(5, 4, 2, '2024-11-20 10:45:00'),
(6, 4, 9, '2024-11-21 14:30:00'),
(7, 5, 6, '2024-11-25 13:20:00'),
(8, 5, 8, '2024-11-26 08:15:00'),
(9, 2, 10, '2024-12-01 12:00:00'),
(10, 3, 5, '2024-12-02 17:30:00');

-- ----------------------------
-- Records of visit_stats (访问统计数据)
-- ----------------------------
INSERT INTO `visit_stats` (`id`, `total_visits`) VALUES (1, 1250);

SET FOREIGN_KEY_CHECKS = 1;