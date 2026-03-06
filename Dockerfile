# 多阶段构建 Dockerfile

# 阶段 1: 构建后端
FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /app
COPY springboot/pom.xml ./
COPY springboot/src ./src
RUN mvn clean package -DskipTests

# 阶段 2: 构建前端
FROM node:20-alpine AS frontend-build
WORKDIR /app
COPY vue/package*.json ./
RUN npm install
COPY vue/ ./
RUN npm run build

# 阶段 3: 运行时镜像
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 安装 nginx 用于提供前端静态文件
RUN apk add --no-cache nginx

# 复制后端 jar
COPY --from=backend-build /app/target/springboot-0.0.1-SNAPSHOT.jar ./app.jar

# 复制前端构建文件
COPY --from=frontend-build /app/dist /usr/share/nginx/html

# 复制 nginx 配置
COPY docker/nginx.conf /etc/nginx/nginx.conf

# 复制启动脚本
COPY docker/start.sh ./start.sh
RUN chmod +x start.sh

# 暴露端口
EXPOSE 80 8081

# 启动脚本
CMD ["./start.sh"]
