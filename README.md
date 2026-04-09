# HireMate AI — 智能面试助手

> 基于 AI 大模型（通义千问）驱动的智能面试模拟平台，支持 AI 一对一面试、简历分析优化、岗位JD智能匹配、面试复盘报告等功能。

![Vue](https://img.shields.io/badge/Vue-3.4-41B883?style=flat-square&logo=vue.js)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?style=flat-square&logo=spring)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql)
![Redis](https://img.shields.io/badge/Redis-7.0-DC382D?style=flat-square&logo=redis)
![Vite](https://img.shields.io/badge/Vite-5.1-646CFF?style=flat-square&logo=vite)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0)

---

## 功能特性

| 模块 | 功能说明 |
|------|---------|
| **AI 模拟面试** | 支持综合面试、技术面试、HR面试、行为面试、终面五种类型；可上传简历进行针对性提问；对话过程流式返回（打字机效果） |
| **简历分析优化** | 上传简历 PDF，系统自动解析并给出简历评分、问题诊断与优化建议 |
| **AI 简历优化** | 结合目标岗位信息，AI 生成个性化简历优化方案 |
| **岗位 JD 匹配** | 输入岗位描述，系统自动分析简历与岗位的匹配度，并给出改进建议 |
| **面试复盘报告** | 面试结束后自动生成多维度评分报告（专业能力、表达能力、逻辑思维、自信心），含每道题目的详细点评与改进建议 |
| **面试题库** | 按岗位/类型/难度分类的面试题库，支持收藏、笔记、AI 生成新题 |
| **群面模拟** | AI 驱动的群体面试模拟，支持多人角色扮演 |

---

## 技术架构

### 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | ^3.4 | 核心框架（Composition API） |
| Vite | ^5.1 | 构建工具 |
| Element Plus | ^2.6 | UI 组件库 |
| Pinia | ^2.1 | 状态管理 |
| Vue Router | ^4.3 | 前端路由 |
| Axios | ^1.6 | HTTP 客户端 |
| SCSS | ^1.71 | 样式预处理 |

### 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.5 | 核心框架 |
| Spring Security | - | 认证与授权 |
| MyBatis-Plus | 3.5.7 | ORM 框架 |
| MySQL | 8.x | 关系型数据库 |
| Redis | - | 缓存与会话存储 |
| OkHttp | 4.12 | AI API HTTP 客户端 |
| JWT (jjwt) | 0.12.6 | Token 认证 |
| PDFBox | 3.0.1 | PDF 文本提取 |
| Knife4j | 4.5.0 | OpenAPI 3 文档 |
| Lombok | - | 简化代码 |

---

## 项目结构

```
HireMate_AI_web/
├── backend/                          # Spring Boot 后端
│   ├── src/main/java/com/hiremate/ai/
│   │   ├── config/                   # 配置类（SecurityConfig、DashScopeProperties 等）
│   │   ├── controller/               # REST API 控制器
│   │   ├── service/                  # 业务逻辑层
│   │   ├── mapper/                   # MyBatis-Plus 数据访问层
│   │   ├── entity/                   # 数据库实体
│   │   ├── dto/                      # 数据传输对象（请求/响应）
│   │   ├── common/                   # 通用类（异常、结果封装）
│   │   ├── security/                 # JWT 认证、过滤器
│   │   └── ai/                       # AI 相关模块
│   │       └── groupchat/            # 群面模拟
│   ├── src/main/resources/
│   │   ├── application.yml           # 主配置文件
│   │   └── schema.sql                # 数据库初始化脚本
│   └── pom.xml
│
├── frontend/                         # Vue 3 前端
│   ├── src/
│   │   ├── api/                      # API 接口封装
│   │   ├── components/               # 通用组件
│   │   │   └── ui/                   # UI 基础组件
│   │   ├── composables/              # Vue 组合式函数
│   │   ├── router/                   # 路由配置
│   │   ├── stores/                   # Pinia 状态管理
│   │   ├── styles/                   # 全局样式与设计变量
│   │   ├── utils/                    # 工具函数
│   │   ├── views/                    # 页面视图
│   │   ├── App.vue                   # 根组件
│   │   └── main.js                   # 入口文件
│   ├── vite.config.js
│   └── package.json
│
├── .env.example                      # 环境变量示例
├── .gitignore                        # Git 忽略配置
└── README.md                         # 项目文档
```

---

## 环境要求

| 依赖 | 版本要求 |
|------|---------|
| JDK | 17+ |
| Maven | 3.9+ |
| Node.js | 18+ |
| npm | 9+ |
| MySQL | 8.x |
| Redis | 6.x+ |

---

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/Weiki886/HireMateAI.git
cd HireMateAI
```

### 2. 配置环境变量

复制环境变量示例文件，填写实际配置：

```bash
cp .env.example .env
```

参考 [.env.example](#环境变量说明) 填写各项配置。

### 3. 初始化数据库

确保 MySQL 服务已启动，执行数据库初始化脚本：

```bash
mysql -u root -p < backend/src/main/resources/schema.sql
```

> 脚本会自动创建 `hiremate` 数据库及所有表结构。

### 4. 启动后端

```bash
cd HireMateAI_backend
mvn spring-boot:run
```

后端启动后访问：
- 接口文档：http://localhost:8080/doc.html （Knife4j）
- 健康检查：http://localhost:8080/actuator/health

### 5. 启动前端

```bash
cd HireMateAI_frontend
npm install
npm run dev
```

前端启动后访问：http://localhost:3000

### 6. 登录账号

- **用户名**：`admin`
- **密码**：`123456`

> 默认测试账号由 schema.sql 自动创建。

---

## 环境变量说明

部署前需在 `.env` 文件中填写以下配置（参考 `.env.example`）：

### 数据库配置

| 变量名 | 说明 | 示例值 |
|--------|------|--------|
| `DB_HOST` | MySQL 主机地址 | `localhost` |
| `DB_PORT` | MySQL 端口 | `3306` |
| `DB_NAME` | 数据库名 | `hiremate` |
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | `你的密码` |

### Redis 配置

| 变量名 | 说明 | 示例值 |
|--------|------|--------|
| `REDIS_HOST` | Redis 主机地址 | `localhost` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `REDIS_DATABASE` | Redis 数据库编号 | `0` |

### JWT 认证配置

| 变量名 | 说明 | 示例值 |
|--------|------|--------|
| `JWT_SECRET` | Token 签名密钥（生产环境务必替换） | `openssl rand -base64 64` 生成 |
| `JWT_EXPIRATION` | Token 有效期（毫秒） | `86400000`（24小时） |

### 阿里云 DashScope AI 配置

| 变量名 | 说明 | 示例值 |
|--------|------|--------|
| `DASHSCOPE_API_KEY` | 阿里云 DashScope API 密钥（必填） | `sk-xxxxxxxxxxxxxxxx` |
| `DASHSCOPE_MODEL` | 使用的模型名称 | `qwen-plus` |
| `DASHSCOPE_BASE_URL` | API 地址（通常不需要修改） | `https://dashscope.aliyuncs.com/api/v1` |

> **如何获取 DashScope API Key？**
> 1. 访问 [阿里云百炼控制台](https://dashscope.console.aliyun.com/)
> 2. 左侧菜单选择「API-KEY 管理」
> 3. 创建新的 API-KEY 并复制

---

## API 接口文档

启动后端后，通过 Knife4j 在线文档访问所有 API：

```
http://localhost:8080/doc.html
```

主要接口模块：

| 模块 | 前缀 | 说明 |
|------|------|------|
| 用户认证 | `/auth` | 注册、登录、Token 刷新 |
| 面试会话 | `/interview` | 创建会话、发送消息、SSE 流式响应 |
| 简历管理 | `/resume` | 简历 CRUD、AI 分析、AI 优化 |
| 岗位匹配 | `/job-match` | JD 分析、简历匹配度评估 |
| 面试题库 | `/question-bank` | 题库分类、题目 CRUD、AI 生成题目 |
| 复盘报告 | `/interview/review` | 报告生成、报告查询 |
| 群面模拟 | `/group-chat` | 群面会话管理 |
| 用户信息 | `/profile` | 个人资料修改、密码修改 |

---

## 数据库表结构

| 表名 | 说明 |
|------|------|
| `user` | 用户信息 |
| `interview_session` | 面试会话 |
| `interview_message` | 面试消息记录 |
| `resume` | 简历管理 |
| `resume_analysis_record` | 简历分析记录 |
| `job_description` | 岗位描述 |
| `job_description_match_record` | 岗位匹配记录 |
| `interview_review_report` | 面试复盘报告 |
| `interview_answer_review` | 面试回答详细评价 |
| `question_bank_category` | 题库分类 |
| `interview_question` | 面试题目 |
| `user_question_favorite` | 用户收藏题目 |

详见 `backend/src/main/resources/schema.sql`。

---

## 开发指南

### 前端开发

```bash
cd HireMateAI_frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview
```

### 后端开发

```bash
cd HireMateAI_frontend

# 编译打包
mvn clean package -DskipTests

# 启动（热部署，需配合 spring-boot-devtools）
mvn spring-boot:run

# 运行测试
mvn test
```

### 添加新的 API 接口

1. **Controller 层**：在 `controller/` 下新建 `XxxController.java`
2. **Service 层**：在 `service/` 下新建 `XxxService.java` 和 `XxxServiceImpl.java`
3. **Mapper 层**：在 `mapper/` 下新建 `XxxMapper.java`
4. **Entity**：在 `entity/` 下新建 `Xxx.java`
5. **DTO**：在 `dto/request/` 和 `dto/response/` 下新建对应的请求/响应类

---

## 许可证

本项目采用 **Apache License, Version 2.0**（Apache-2.0）开源许可。

在遵守许可条款的前提下，你可以自由使用、修改与分发本软件；分发时须保留原有版权声明与许可说明。第三方组件受其各自许可证约束。

---

## 联系方式

- 邮箱：weiki808@163.com