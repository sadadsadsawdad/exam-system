# 🚀 使用 Gitee 部署到 Railway + Vercel

## ✅ 当前状态

- ✅ 代码已推送到 Gitee
- ✅ 仓库地址：https://gitee.com/lisa/exam-system

---

## 📋 部署流程

由于使用 Gitee 而不是 GitHub，部署流程略有不同：

### 方案 A：Gitee → GitHub → Railway + Vercel（推荐）

Railway 和 Vercel 主要支持 GitHub，所以最简单的方式是将 Gitee 同步到 GitHub。

#### 步骤 1：在 Gitee 设置仓库镜像

1. 访问你的 Gitee 仓库：https://gitee.com/lisa/exam-system
2. 点击 "管理" → "仓库镜像管理"
3. 点击 "添加镜像"
4. 填写：
   - 镜像方向：推送
   - 远程仓库地址：`https://github.com/sadadsadsawdad/exam-system.git`
   - 用户名：你的 GitHub 用户名
   - 密码：GitHub Personal Access Token（需要在 GitHub 生成）
5. 勾选 "强制同步"
6. 点击 "添加"

#### 步骤 2：生成 GitHub Personal Access Token

1. 访问：https://github.com/settings/tokens
2. 点击 "Generate new token" → "Generate new token (classic)"
3. 填写：
   - Note: `Gitee Mirror`
   - Expiration: 90 days 或更长
   - 勾选权限：`repo`（所有子选项）
4. 点击 "Generate token"
5. 复制生成的 token（只显示一次）
6. 将 token 粘贴到 Gitee 镜像设置的密码框

#### 步骤 3：手动同步

1. 在 Gitee 仓库镜像管理页面
2. 点击 "立即同步"
3. 等待同步完成

#### 步骤 4：使用 GitHub 仓库部署

同步完成后，按照原计划部署：
- Railway 连接 GitHub 仓库
- Vercel 连接 GitHub 仓库

---

### 方案 B：直接使用 Gitee 部署（需要手动配置）

如果不想同步到 GitHub，可以手动部署：

#### Railway 部署（使用 Docker）

1. 在本地构建 Docker 镜像
2. 推送到 Docker Hub
3. Railway 从 Docker Hub 部署

#### Vercel 部署（使用 CLI）

```bash
# 安装 Vercel CLI
npm install -g vercel

# 登录
vercel login

# 部署
cd vue
vercel --prod
```

---

### 方案 C：使用国内云平台（最简单）

既然已经用了 Gitee，不如直接用国内云平台：

#### 1. 使用 Gitee Pages（前端）

1. 在 Gitee 仓库页面
2. 点击 "服务" → "Gitee Pages"
3. 选择分支：main
4. 部署目录：vue/dist
5. 点击 "启动"

**注意**：需要先在本地构建前端：
```bash
cd vue
npm install
npm run build
git add dist
git commit -m "添加构建文件"
git push gitee main
```

#### 2. 使用阿里云/腾讯云（后端）

参考 `部署文档.md` 中的服务器部署方案。

---

## 🎯 推荐方案

根据你的情况，我推荐：

### 最简单：方案 A（Gitee → GitHub 镜像）

1. 设置 Gitee 到 GitHub 的自动同步
2. 使用 GitHub 仓库部署到 Railway + Vercel
3. 以后只需推送到 Gitee，会自动同步到 GitHub

**优点**：
- 一次配置，永久使用
- 自动同步，无需手动操作
- 使用 Railway 和 Vercel 的免费额度

---

## 📝 详细步骤：设置 Gitee → GitHub 镜像

### 1. 生成 GitHub Token

访问：https://github.com/settings/tokens/new

填写：
- Note: `Gitee Mirror`
- Expiration: `90 days`
- 勾选：`repo` (全部)

点击 "Generate token"，复制 token（格式：`ghp_xxxxxxxxxxxx`）

### 2. 在 Gitee 设置镜像

1. 访问：https://gitee.com/lisa/exam-system/settings#mirror
2. 点击 "添加镜像"
3. 填写：
   ```
   镜像方向：推送
   远程仓库地址：https://github.com/sadadsadsawdad/exam-system.git
   用户名：sadadsadsawdad
   密码：ghp_xxxxxxxxxxxx（你的 GitHub token）
   ```
4. 勾选 "强制同步"
5. 点击 "添加"
6. 点击 "立即同步"

### 3. 验证同步

访问 GitHub 仓库确认代码已同步：
https://github.com/sadadsadsawdad/exam-system

### 4. 继续部署

代码同步到 GitHub 后，按照 `开始部署.md` 的步骤：
1. Railway 部署后端
2. Vercel 部署前端

---

## 🔄 以后的工作流程

设置好镜像后，你的工作流程：

```bash
# 1. 修改代码
# 2. 提交到 Git
git add .
git commit -m "更新功能"

# 3. 推送到 Gitee
git push gitee main

# 4. Gitee 自动同步到 GitHub
# 5. Railway 和 Vercel 自动检测更新并重新部署
```

一切都是自动的！

---

## 💡 提示

- Gitee 镜像同步通常在 1-2 分钟内完成
- 可以在 Gitee 仓库的"仓库镜像管理"查看同步状态
- 如果同步失败，检查 GitHub token 是否正确

---

## 🆘 需要帮助？

如果遇到问题：
1. 确认 GitHub token 有 `repo` 权限
2. 确认 GitHub 仓库存在且可访问
3. 在 Gitee 查看镜像同步日志

---

选择一个方案，告诉我你的选择，我会详细指导你完成部署！🚀
