# 前端 API 配置使用说明

## 当前状态

项目中的 Vue 文件使用了硬编码的 `http://localhost:8081` API 地址。

## 部署到 Railway + Vercel 的配置

### 方案 1：保持现有代码不变（推荐）

在 Vercel 部署时配置环境变量，通过 `vercel.json` 的 rewrites 功能代理 API 请求。

**优点：** 不需要修改任何 Vue 代码
**缺点：** 依赖 Vercel 的代理功能

**配置步骤：**

1. 在 Vercel 项目设置中添加环境变量：
   ```
   VITE_API_BASE_URL=https://your-backend.up.railway.app
   ```

2. `vercel.json` 已配置好代理规则（已创建）

### 方案 2：使用统一的 API 配置（更灵活）

修改所有 Vue 文件，使用 `vue/src/config/api.js` 中的配置。

**优点：** 更灵活，适用于任何部署方式
**缺点：** 需要修改大量文件

**使用方法：**

```javascript
// 在 Vue 文件中导入
import { apiUrl, apiFetch } from '@/config/api'

// 使用方式 1：构建 URL
const url = apiUrl('api/users')
const res = await fetch(url)

// 使用方式 2：直接使用封装的 fetch
const res = await apiFetch('api/users')
```

## 当前推荐方案

**使用方案 1**，因为：
1. 不需要修改现有代码
2. 部署更快速
3. Vercel 的代理功能稳定可靠

## 环境变量配置

### 开发环境（.env.development）
```
VITE_API_BASE_URL=http://localhost:8081
```

### 生产环境（Vercel）
在 Vercel 项目设置中配置：
```
VITE_API_BASE_URL=https://your-backend.up.railway.app
```

## 如果需要修改代码

如果将来需要使用方案 2，可以批量替换：

```bash
# 查找所有硬编码的 API 地址
grep -r "http://localhost:8081" vue/src/views/

# 需要将所有的：
fetch('http://localhost:8081/api/users')

# 替换为：
import { apiUrl } from '@/config/api'
fetch(apiUrl('api/users'))
```

但目前不需要这样做，使用 Vercel 的代理功能即可。
