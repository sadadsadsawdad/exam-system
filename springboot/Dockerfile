# Spring Boot 后端 Dockerfile

# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 安装 GCC（用于 C 代码编译功能）
RUN apk add --no-cache gcc musl-dev

# 复制 jar 文件
COPY --from=build /app/target/springboot-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8081

# JVM 参数优化
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
