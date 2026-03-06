# Vue 前端 Dockerfile

# 构建阶段
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# 运行阶段 - 使用 Nginx
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# 删除默认的 nginx 内容
RUN rm -rf ./*

# 复制构建文件
COPY --from=build /app/dist .

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
