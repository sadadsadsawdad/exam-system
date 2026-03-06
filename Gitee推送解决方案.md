# 🔐 Gitee 推送认证问题解决方案

## ❌ 当前问题

推送失败：`Incorrect username or password (access token)`

这是因为 Gitee 需要使用**个人访问令牌**而不是密码。

---

## ✅ 解决方案：使用个人访问令牌

### 第 1 步：生成 Gitee 个人访问令牌

1. **访问 Gitee 令牌页面**
   https://gitee.com/profile/personal_access_tokens

2. **点击"生成新令牌"**

3. **填写信息**：
   - 令牌描述：`exam-system-deploy`
   - 权限勾选：
     - ✅ `projects` (仓库权限)
   - 过期时间：选择一个合适的时间（建议 1 年）

4. **点击"提交"**

5. **复制令牌**
   - 令牌只显示一次，请立即复制保存
   - 格式类似：`xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

---

### 第 2 步：使用令牌推送代码

#### 方法 1：在 URL 中包含令牌（推荐）

在终端执行：

```bash
# 移除旧的远程仓库
git remote remove gitee

# 添加新的远程仓库（包含令牌）
git remote add gitee https://你的令牌@gitee.com/lisa/exam-system.git

# 推送代码
git push -u gitee main
```

**示例**（假设你的令牌是 `abc123xyz`）：
```bash
git remote add gitee https://abc123xyz@gitee.com/lisa/exam-system.git
git push -u gitee main
```

---

#### 方法 2：使用 Git Credential Manager

当推送时会弹出登录窗口：
- Username: 输入你的 Gitee 用户名（`lisa`）
- Password: 粘贴你的个人访问令牌（不是密码！）

---

### 第 3 步：验证推送成功

访问你的 Gitee 仓库：
https://gitee.com/lisa/exam-system

确认代码已上传。

---

## 🎯 完整操作步骤

### 1. 生成令牌

访问：https://gitee.com/profile/personal_access_tokens

点击"生成新令牌"，勾选 `projects` 权限，复制令牌。

### 2. 配置并推送

在终端执行（替换 `你的令牌` 为实际令牌）：

```bash
# 移除旧配置
git remote remove gitee

# 添加新配置（包含令牌）
git remote add gitee https://你的令牌@gitee.com/lisa/exam-system.git

# 推送代码
git push -u gitee main --force
```

### 3. 验证

访问：https://gitee.com/lisa/exam-system

---

## 💡 安全提示

- 个人访问令牌等同于密码，请妥善保管
- 不要将令牌提交到代码仓库
- 如果令牌泄露，立即在 Gitee 删除并重新生成

---

## 🔄 替代方案：使用 GitHub

如果 Gitee 推送一直有问题，可以：

1. **直接推送到 GitHub**（之前已配置）
   ```bash
   git push -u origin main --force
   ```

2. **使用 GitHub 部署**
   - Railway 连接 GitHub
   - Vercel 连接 GitHub

GitHub 的网络可能不稳定，但认证更简单。

---

## 📞 需要帮助？

生成令牌后，告诉我，我会帮你执行推送命令！
