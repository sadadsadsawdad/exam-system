# Railway + Vercel 部署步骤

## 📋 准备工作

### 1. 确保代码已提交到 GitHub

```bash
# 检查 Git 状态
git status

# 如果有未提交的更改
git add .
git commit -m "准备部署到 Railway 和 Vercel"

# 推送到 GitHub
git push origin main
```

如果还没有 GitHub 仓库：

```bash
# 初始化 Git
git init
git add .
git commit -m "Initial commit"

# 在 GitHub 创建新仓库，然后：
git remote add origin https://github.com/你的用户名/你的仓库名.git
git branch -M main
git push -u origin main
```

---

## 🚂 第一步：部署后端到 Railway

### 1. 注册并登录 Railway

1. 访问 https://railway.app/
2. 点击右上角 "Login"
3. 选择 "Login with GitHub"（推荐）
4. 授权 Railway 访问你的 GitHub

### 2. 创建新项目

1. 点击 "New Project"
2. 选择 "Deploy from GitHub repo"
3. 如果是第一次使用，需要配置 GitHub App
4. 选择你的项目仓库

### 3. Railway 会自动检测项目

Railway 会自动检测到 `railway.json` 配置文件并开始构建。

等待构建完成（大约 3-5 分钟）。

### 4. 添加 MySQL 数据库

1. 在项目页面，点击右上角 "+ New"
2. 选择 "Database"
3. 选择 "Add MySQL"
4. 等待 MySQL 创建完成

### 5. 配置环境变量

1. 点击你的后端服务（springboot）
2. 切换到 "Variables" 标签
3. 点击 "Raw Editor"
4. 添加以下环境变量：

```bash
# Railway 会自动提供 MySQL 连接变量，你需要引用它们
SPRING_DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQL_HOST}}:${{MySQL.MYSQL_PORT}}/${{MySQL.MYSQL_DATABASE}}?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=${{MySQL.MYSQL_USER}}
SPRING_DATASOURCE_PASSWORD=${{MySQL.MYSQL_PASSWORD}}
SERVER_PORT=8080
COMPILER_GCC_PATH=gcc
```

或者手动填写（如果使用外部数据库）：

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://你的数据库地址:3306/exam?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=examuser
SPRING_DATASOURCE_PASSWORD=你的密码
SERVER_PORT=8080
COMPILER_GCC_PATH=gcc
```

### 6. 重新部署

1. 点击 "Deploy" 按钮重新部署
2. 等待部署完成

### 7. 获取后端 URL

1. 在服务页面，点击 "Settings"
2. 找到 "Domains" 部分
3. 点击 "Generate Domain"
4. 复制生成的域名（类似：`your-app.up.railway.app`）

### 8. 初始化数据库

#### 方法 1：使用 Railway 的 MySQL 客户端

1. 点击 MySQL 服务
2. 点击 "Data" 标签
3. 点击 "Query" 按钮
4. 复制 `springboot/src/main/resources/sql/init.sql` 的内容
5. 粘贴并执行

#### 方法 2：使用本地 MySQL 客户端

1. 点击 MySQL 服务
2. 点击 "Connect" 标签
3. 复制连接命令，类似：

```bash
mysql -h containers-us-west-xxx.railway.app -P 6789 -u root -p
```

4. 在本地终端执行连接命令
5. 导入 SQL：

```bash
mysql -h [host] -P [port] -u [user] -p[password] railway < springboot/src/main/resources/sql/init.sql
```

---

## ▲ 第二步：部署前端到 Vercel

### 1. 注册并登录 Vercel

1. 访问 https://vercel.com/
2. 点击 "Sign Up"
3. 选择 "Continue with GitHub"
4. 授权 Vercel 访问你的 GitHub

### 2. 导入项目

1. 点击 "Add New..." → "Project"
2. 找到你的 GitHub 仓库
3. 点击 "Import"

### 3. 配置项目

在配置页面填写：

**Framework Preset:** Vite

**Root Directory:** `vue` （点击 "Edit" 修改）

**Build Command:** `npm run build`

**Output Directory:** `dist`

**Install Command:** `npm install`

### 4. 配置环境变量

在 "Environment Variables" 部分添加：

**Name:** `VITE_API_BASE_URL`

**Value:** `https://your-backend.up.railway.app` （替换为你的 Railway 后端地址）

**注意：** 不要在 URL 末尾加 `/api`，代码会自动处理。

### 5. 部署

1. 点击 "Deploy"
2. 等待构建完成（大约 2-3 分钟）
3. 部署成功后，Vercel 会显示你的网站 URL

### 6. 访问你的应用

点击 Vercel 提供的 URL（类似：`your-app.vercel.app`）

---

## ✅ 第三步：验证部署

### 1. 测试后端

在浏览器访问：
```
https://your-backend.up.railway.app/api/health
```

如果返回 JSON 数据，说明后端正常运行。

### 2. 测试前端

访问你的 Vercel URL，应该能看到登录页面。

### 3. 测试完整流程

1. 在登录页面尝试登录
2. 默认管理员账号：
   - 用户名：`admin`
   - 密码：`admin123`

如果能成功登录，说明前后端连接正常！

---

## 🔧 常见问题

### 问题 1：后端构建失败

**原因：** Maven 构建超时或依赖下载失败

**解决：**
1. 在 Railway 项目设置中增加构建超时时间
2. 检查 `pom.xml` 是否正确
3. 查看构建日志找到具体错误

### 问题 2：数据库连接失败

**原因：** 环境变量配置错误

**解决：**
1. 检查 Railway 的环境变量是否正确
2. 确保使用了 Railway 提供的 MySQL 变量引用
3. 重新部署后端服务

### 问题 3：前端无法连接后端

**原因：** CORS 或 API 地址配置错误

**解决：**
1. 检查 Vercel 的 `VITE_API_BASE_URL` 环境变量
2. 确保后端 URL 正确（不要有多余的斜杠）
3. 打开浏览器开发者工具查看网络请求
4. 检查后端是否配置了 CORS

### 问题 4：前端页面空白

**原因：** 构建配置错误

**解决：**
1. 检查 Root Directory 是否设置为 `vue`
2. 检查 Build Command 和 Output Directory
3. 查看 Vercel 构建日志

### 问题 5：Railway 免费额度用完

**解决：**
- Railway 提供每月 $5 免费额度
- 如果用完，可以：
  1. 升级到付费计划
  2. 使用其他平台（Render、Heroku）
  3. 部署到自己的服务器

---

## 📊 监控和日志

### Railway 日志

1. 进入你的服务
2. 点击 "Deployments" 标签
3. 点击最新的部署
4. 查看实时日志

### Vercel 日志

1. 进入你的项目
2. 点击 "Deployments"
3. 点击最新的部署
4. 查看构建和运行时日志

---

## 🔄 更新部署

### 更新代码

```bash
# 修改代码后
git add .
git commit -m "更新功能"
git push origin main
```

Railway 和 Vercel 会自动检测到代码更改并重新部署！

### 手动触发部署

**Railway:**
1. 进入服务页面
2. 点击 "Deploy" 按钮

**Vercel:**
1. 进入项目页面
2. 点击 "Redeploy"

---

## 🎉 完成！

现在你的应用已经成功部署到云端：

- **后端：** https://your-backend.up.railway.app
- **前端：** https://your-app.vercel.app

你可以分享前端 URL 给其他人使用！

---

## 💡 下一步

### 1. 配置自定义域名

**Vercel:**
1. 在项目设置中点击 "Domains"
2. 添加你的域名
3. 按照提示配置 DNS

**Railway:**
1. 在服务设置中找到 "Domains"
2. 添加自定义域名
3. 配置 DNS CNAME 记录

### 2. 配置 HTTPS

Railway 和 Vercel 都自动提供免费的 SSL 证书，无需额外配置。

### 3. 监控和分析

- 使用 Railway 的监控功能查看资源使用情况
- 使用 Vercel Analytics 查看访问统计

---

## 📞 需要帮助？

如果遇到问题：

1. 查看 Railway 文档：https://docs.railway.app/
2. 查看 Vercel 文档：https://vercel.com/docs
3. 检查构建和运行日志
4. 确认环境变量配置正确

祝部署顺利！🚀
