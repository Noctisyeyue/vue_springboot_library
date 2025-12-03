# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Library Management System built with Vue.js 3 + Spring Boot architecture. It's a full-stack web application for managing books, users, and lending records in a library.

## Development Commands

### Backend (Spring Boot)
```bash
# Navigate to backend directory
cd SpringBoot

# Run the Spring Boot application
mvn spring-boot:run

# Build the JAR file
mvn clean package

# Run tests
mvn test

# The application runs on port 9090
```

### Frontend (Vue.js)
```bash
# Navigate to frontend directory
cd vue

# Install dependencies
npm install

# Start development server (runs on port 9876)
npm run serve

# Build for production
npm run build
```

### Database Setup
- Database: MySQL (database name: `test1`)
- Schema file: `sql/springboot-vue.sql`
- Default connection: `localhost:3306` with user `root` and password `123456`

## Architecture

### Technology Stack
- **Backend**: Spring Boot 2.6.1, Java 8, MyBatis Plus 3.4.3.1, JWT authentication
- **Frontend**: Vue 3.0.0, Vue Router 4.0.0, Vuex 4.0.0, Element Plus UI
- **Database**: MySQL with utf8mb4 encoding
- **Build Tools**: Maven (backend), npm/vue-cli (frontend)

### Project Structure
```
Vue-Springboot-Library-main/
├── SpringBoot/              # Backend Java application
│   ├── src/main/java/com/example/demo/
│   │   ├── DemoApplication.java    # Main entry point
│   │   ├── controller/             # REST API controllers
│   │   ├── entity/                 # JPA/MyBatis entities
│   │   ├── mapper/                 # MyBatis data access layer
│   │   ├── utils/                  # Utility classes
│   │   └── commom/                 # Common components
│   └── pom.xml                     # Maven configuration
├── vue/                    # Frontend Vue.js application
│   ├── src/
│   │   ├── main.js               # Vue entry point
│   │   ├── router/index.js        # Route definitions
│   │   ├── store/                 # Vuex state management
│   │   ├── views/                 # Page components
│   │   ├── components/            # Reusable components
│   │   └── layout/                # Layout components
│   ├── package.json               # npm configuration
│   └── vue.config.js              # Dev server config with CORS proxy
├── sql/                    # Database schema and initial data
└── run/                    # Deployment scripts and JAR files
```

### Key Configuration Details

#### Backend Configuration
- **Server Port**: 9090
- **Database**: MySQL on localhost:3306, database: `test1`
- **ORM**: MyBatis Plus with camelCase mapping enabled
- **Authentication**: JWT-based authentication

#### Frontend Configuration
- **Dev Server Port**: 9876
- **API Proxy**: `/api` routes proxy to `http://localhost:9090`
- **UI Library**: Element Plus with Chinese localization
- **Charts**: ECharts for data visualization

### Database Schema
Main tables:
- `user` - User accounts and roles (1: admin, 2: regular user)
- `book` - Book inventory with quantity tracking
- `lend_record` - Book borrowing history
- `bookwithuser` - Current borrowing records with due dates
- `book_collection` - User book collections/favorites

### Authentication & Authorization
- JWT token-based authentication
- Role-based access control (admin vs regular users)
- Login endpoint: `/api/user/login`
- Token validation in backend interceptors

### API Structure
Controllers provide RESTful endpoints:
- `BookController` - Book CRUD operations
- `UserController` - User management and authentication
- `LendRecordController` - Borrowing/returning operations
- `DashboardController` - Statistics and analytics

### CORS Configuration
Development proxy configured in `vue.config.js` to handle CORS between frontend (9876) and backend (9090).

## Development Notes

### Running the Full Application
1. Set up MySQL database and import `sql/springboot-vue.sql`
2. Update database credentials in `SpringBoot/src/main/resources/application.properties` if needed
3. Start backend: `cd SpringBoot && mvn spring-boot:run`
4. Start frontend: `cd vue && npm install && npm run serve`
5. Access application at `http://localhost:9876`

### Key Features
- User registration and login system
- Book inventory management with quantity tracking
- Borrow/return workflow with due date management
- Personal book collections for users
- Admin dashboard with statistics (ECharts)
- Chinese language support throughout the UI

---

# 中文版 (Chinese Version)

此文件为 Claude Code (claude.ai/code) 在此代码库中工作时提供指导。

## 项目概述

这是一个基于 Vue.js 3 + Spring Boot 架构的图书管理系统。它是一个用于管理图书馆图书、用户和借阅记录的全栈 Web 应用程序。

## 开发命令

### 后端 (Spring Boot)
```bash
# 进入后端目录
cd SpringBoot

# 运行 Spring Boot 应用程序
mvn spring-boot:run

# 构建 JAR 文件
mvn clean package

# 运行测试
mvn test

# 应用程序运行在 9090 端口
```

### 前端 (Vue.js)
```bash
# 进入前端目录
cd vue

# 安装依赖
npm install

# 启动开发服务器（运行在 9876 端口）
npm run serve

# 生产构建
npm run build
```

### 数据库设置
- 数据库：MySQL（数据库名：`test1`）
- 架构文件：`sql/springboot-vue.sql`
- 默认连接：`localhost:3306`，用户名 `root`，密码 `123456`

## 架构

### 技术栈
- **后端**：Spring Boot 2.6.1、Java 8、MyBatis Plus 3.4.3.1、JWT 认证
- **前端**：Vue 3.0.0、Vue Router 4.0.0、Vuex 4.0.0、Element Plus UI
- **数据库**：MySQL，使用 utf8mb4 编码
- **构建工具**：Maven（后端）、npm/vue-cli（前端）

### 项目结构
```
Vue-Springboot-Library-main/
├── SpringBoot/              # 后端 Java 应用程序
│   ├── src/main/java/com/example/demo/
│   │   ├── DemoApplication.java    # 主入口点
│   │   ├── controller/             # REST API 控制器
│   │   ├── entity/                 # JPA/MyBatis 实体
│   │   ├── mapper/                 # MyBatis 数据访问层
│   │   ├── utils/                  # 工具类
│   │   └── commom/                 # 通用组件
│   └── pom.xml                     # Maven 配置
├── vue/                    # 前端 Vue.js 应用程序
│   ├── src/
│   │   ├── main.js               # Vue 入口点
│   │   ├── router/index.js        # 路由定义
│   │   ├── store/                 # Vuex 状态管理
│   │   ├── views/                 # 页面组件
│   │   ├── components/            # 可重用组件
│   │   └── layout/                # 布局组件
│   ├── package.json               # npm 配置
│   └── vue.config.js              # 开发服务器配置和 CORS 代理
├── sql/                    # 数据库架构和初始数据
└── run/                    # 部署脚本和 JAR 文件
```

### 关键配置详情

#### 后端配置
- **服务器端口**：9090
- **数据库**：MySQL localhost:3306，数据库：`test1`
- **ORM**：MyBatis Plus，启用驼峰命名映射
- **认证**：基于 JWT 的认证

#### 前端配置
- **开发服务器端口**：9876
- **API 代理**：`/api` 路由代理到 `http://localhost:9090`
- **UI 库**：Element Plus，支持中文本地化
- **图表**：ECharts 用于数据可视化

### 数据库架构
主要表：
- `user` - 用户账户和角色（1：管理员，2：普通用户）
- `book` - 图书库存，包含数量跟踪
- `lend_record` - 图书借阅历史
- `bookwithuser` - 当前借阅记录，包含到期日期
- `book_collection` - 用户图书收藏/收藏夹

### 认证与授权
- 基于 JWT 令牌的认证
- 基于角色的访问控制（管理员 vs 普通用户）
- 登录端点：`/api/user/login`
- 后端拦截器中的令牌验证

### API 结构
控制器提供 RESTful 端点：
- `BookController` - 图书 CRUD 操作
- `UserController` - 用户管理和认证
- `LendRecordController` - 借阅/归还操作
- `DashboardController` - 统计和分析

### CORS 配置
在 `vue.config.js` 中配置开发代理，处理前端（9876）和后端（9090）之间的 CORS。

## 开发说明

### 运行完整应用程序
1. 设置 MySQL 数据库并导入 `sql/springboot-vue.sql`
2. 如需要，更新 `SpringBoot/src/main/resources/application.properties` 中的数据库凭据
3. 启动后端：`cd SpringBoot && mvn spring-boot:run`
4. 启动前端：`cd vue && npm install && npm run serve`
5. 在 `http://localhost:9876` 访问应用程序

### 主要功能
- 用户注册和登录系统
- 带数量跟踪的图书库存管理
- 带到期日期管理的借阅/归还流程
- 用户个人图书收藏
- 管理员仪表板，包含统计信息（ECharts）
- 整个 UI 的中文语言支持


从现在开始，我提出的问题，你不要节外生枝，不需要多余的功能，尽量使用简洁的方法，并且在增加，删除和修改代码的时候增加注释