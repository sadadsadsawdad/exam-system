# 🚀 立即部署到 Railway + Vercel

## ✅ 准备完成

- ✅ 代码已推送到 GitHub
- ✅ 仓库地址：https://github.com/sadadsadsawdad/exam-system

现在开始部署！

---

## 第一步：部署后端到 Railway（5分钟）

### 1. 访问 Railway

打开浏览器，访问：https://railway.app/

### 2. 登录

- 点击右上角 "Login"
- 选择 "Login with GitHub"
- 授权 Railway 访问你的 GitHub

### 3. 创建新项目

- 点击 "New Project"
- 选择 "Deploy from GitHub repo"
- 如果是第一次使用，点击 "Configure GitHub App"
- 选择你的仓库：`sadadsadsawdad/exam-system`
- 点击 "Deploy Now"

### 4. 等待自动构建

Railway 会自动：
- 检测到 `railway.json` 配置
- 使用 Maven 构建 Spring Boot 项目
- 启动后端服务

等待 3-5 分钟，直到显示 "Success" 或 "Active"

### 5. 添加 MySQL 数据库

- 在项目页面，点击右上角 "+ New"
- 选择 "Database"
- 选择 "Add MySQL"
- 等待 MySQL 创建完成（约 1 分钟）

### 6. 配置环境变量

- 点击你的后端服务（springboot）
- 点击 "Variables" 标签
- 点击 "Raw Editor"
- 粘贴以下内容：

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQL_HOST}}:${{MySQL.MYSQL_PORT}}/${{MySQL.MYSQL_DATABASE}}?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=${{MySQL.MYSQL_USER}}
SPRING_DATASOURCE_PASSWORD=${{MySQL.MYSQL_PASSWORD}}
SERVER_PORT=8080
COMPILER_GCC_PATH=gcc
```

- 点击 "Update Variables"
- 服务会自动重新部署

### 7. 生成域名

- 点击 "Settings" 标签
- 找到 "Domains" 部分
- 点击 "Generate Domain"
- 复制生成的域名（例如：`your-app.up.railway.app`）

**重要：保存这个域名，后面部署前端时需要用到！**

### 8. 初始化数据库

#### 方法 1：使用 Railway 的 MySQL 客户端

- 点击 MySQL 服务
- 点击 "Data" 标签
- 点击 "Query" 按钮
- 打开项目中的 `springboot/src/main/resources/sql/init.sql`
- 复制所有内容
- 粘贴到 Query 窗口
- 点击 "Run" 执行

#### 方法 2：使用本地 MySQL 客户端

- 点击 MySQL 服务
- 点击 "Connect" 标签
- 复制连接命令
- 在本地终端执行：

```bash
mysql -h [host] -P [port] -u [user] -p[password] railway < springboot/src/main/resources/sql/init.sql
```

### 9. 测试后端

在浏览器访问：
```
https://your-app.up.railway.app/api/health
```

如果看到 `{"status":"UP",...}`，说明后端部署成功！✅

---

## 第二步：部署前端到 Vercel（3分钟）

### 1. 访问 Vercel

打开浏览器，访问：https://vercel.com/

### 2. 登录

- 点击 "Sign Up" 或 "Login"
- 选择 "Continue with GitHub"
- 授权 Vercel 访问你的 GitHub

### 3. 导入项目

- 点击 "Add New..." → "Project"
- 找到你的 GitHub 仓库：`sadadsadsawdad/exam-system`
- 点击 "Import"

### 4. 配置项目

在配置页面填写：

**Framework Preset**: Vite

**Root Directory**: 
- 点击 "Edit" 按钮
- 输入：`vue`

**Build Command**: `npm run build`

**Output Directory**: `dist`

**Install Command**: `npm install`

### 5. 配置环境变量

在 "Environment Variables" 部分：

- **Name**: `VITE_API_BASE_URL`
- **Value**: `https://your-app.up.railway.app`（你的 Railway 后端地址）

⚠️ **重要**：
- 替换为你在第一步第7点复制的 Railway 域名
- 不要在 URL 末尾加 `/api`

### 6. 部署

- 点击 "Deploy" 按钮
- 等待构建完成（约 2-3 分钟）
- 部署成功后，Vercel 会显示你的网站 URL

### 7. 访问你的应用

点击 Vercel 提供的 URL（例如：`your-app.vercel.app`）

---

## 🎉 完成！测试你的应用

### 1. 访问前端

打开 Vercel 提供的 URL

### 2. 登录测试

使用默认账号登录：
- **管理员**：admin / admin123
- **教师**：teacher / teacher123
- **学生**：student / student123

### 3. 测试功能

- 查看用户列表
- 查看题库
- 查看考试列表
- 测试各项功能

---

## 📊 部署信息记录

请记录你的部署信息：

- **GitHub 仓库**：https://github.com/sadadsadsawdad/exam-system
- **Railway 后端**：https://________________.up.railway.app
- **Vercel 前端**：https://________________.vercel.app
- **部署时间**：____年__月__日

---

## 🐛 故障排查

### 后端无法访问

1. 检查 Railway 部署日志
2. 确认环境变量配置正确
3. 确认数据库已初始化
4. 访问 `/api/health` 检查服务状态

### 前端无法连接后端

1. 检查 Vercel 的 `VITE_API_BASE_URL` 环境变量
2. 确认后端 URL 正确（不要有多余的斜杠）
3. 打开浏览器开发者工具（F12）查看网络请求
4. 检查后端 CORS 配置

### 数据库连接失败

1. 确认 MySQL 服务正在运行
2. 检查环境变量引用语法
3. 重新部署后端服务

---

## 🔄 更新部署

当你修改代码后：

```bash
git add .
git commit -m "更新功能"
git push origin main
```

Railway 和 Vercel 会自动检测更新并重新部署！

---

## 💡 下一步

### 配置自定义域名（可选）

**Vercel**：
1. 在项目设置中点击 "Domains"
2. 添加你的域名
3. 按照提示配置 DNS

**Railway**：
1. 在服务设置中找到 "Domains"
2. 添加自定义域名
3. 配置 DNS CNAME 记录

### 配置 HTTPS

Railway 和 Vercel 都自动提供免费的 SSL 证书，无需额外配置。

---

## 🎊 恭喜！

你的在线考试系统已成功部署到云端！

现在你可以：
- 分享前端 URL 给其他人使用
- 继续开发新功能
- 监控应用运行状态

祝使用愉快！🚀
