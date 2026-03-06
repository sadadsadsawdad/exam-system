# 在线考试系统

基于 Spring Boot + Vue 3 的在线考试系统，支持多种题型、自动批改、成绩管理等功能。

## 🚀 快速开始

### 本地开发

#### 后端
```bash
cd springboot
mvn spring-boot:run
```

#### 前端
```bash
cd vue
npm install
npm run dev
```

访问 http://localhost:5173

### 云平台部署

#### 最简单的方式：Railway + Vercel

1. 将代码推送到 GitHub
2. 按照 `Railway部署步骤.md` 操作
3. 5分钟完成部署

详细步骤请查看：
- **快速指南**：`部署检查清单.md`
- **详细步骤**：`Railway部署步骤.md`
- **多平台方案**：`云平台部署指南.md`

#### Docker 部署

```bash
docker-compose up -d
```

访问 http://localhost

## 📁 项目结构

```
├── springboot/          # Spring Boot 后端
│   ├── src/
│   └── pom.xml
├── vue/                 # Vue 3 前端
│   ├── src/
│   └── package.json
├── deploy/              # 部署脚本和配置
├── docker-compose.yml   # Docker 配置
├── railway.json         # Railway 配置
└── vercel.json          # Vercel 配置
```

## 🛠️ 技术栈

### 后端
- Spring Boot 3.4.12
- MyBatis
- MySQL 8.0
- Java 17

### 前端
- Vue 3
- Element Plus
- Vite
- ECharts

## 📖 文档

- **部署文档**
  - `部署检查清单.md` - 快速检查清单
  - `Railway部署步骤.md` - Railway + Vercel 详细步骤
  - `云平台部署指南.md` - 多平台部署方案
  - `一键部署到云平台.md` - Docker 和云平台部署
  - `部署文档.md` - 完整的服务器部署文档
  - `快速部署指南.md` - 简化版部署指南

- **部署脚本**（`deploy/` 目录）
  - `setup-server.sh` - 服务器环境初始化
  - `deploy-backend.sh` - 后端部署
  - `deploy-frontend.sh` - 前端部署
  - `quick-deploy.sh` - 一键部署
  - `backup-db.sh` - 数据库备份

## 🎯 功能特性

- ✅ 用户管理（管理员、教师、学生）
- ✅ 班级管理
- ✅ 题库管理（选择题、填空题、编程题）
- ✅ 考试管理
- ✅ 自动批改
- ✅ 成绩统计
- ✅ 消息通知
- ✅ C 代码在线编译运行

## 🔐 默认账号

- 管理员：admin / admin123
- 教师：teacher / teacher123
- 学生：student / student123

## 📊 部署状态

### 支持的部署平台

- ✅ Railway（后端）
- ✅ Vercel（前端）
- ✅ Docker / Docker Compose
- ✅ 阿里云 / 腾讯云
- ✅ Render
- ✅ 任何支持 Java 的云平台

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License

## 📞 支持

如有问题，请查看文档或提交 Issue。
