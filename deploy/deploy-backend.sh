#!/bin/bash

# 后端部署脚本
# 使用方法：在项目根目录执行 bash deploy/deploy-backend.sh

set -e  # 遇到错误立即退出

echo "=========================================="
echo "开始部署后端服务"
echo "=========================================="

# 配置变量
DEPLOY_DIR="/opt/exam-backend"
JAR_NAME="springboot-0.0.1-SNAPSHOT.jar"
SERVICE_NAME="exam-backend"

# 1. 构建项目
echo ""
echo "[1/6] 构建 Spring Boot 项目..."
cd springboot
mvn clean package -DskipTests

if [ ! -f "target/$JAR_NAME" ]; then
    echo "错误：构建失败，找不到 jar 文件"
    exit 1
fi

echo "构建成功！"

# 2. 创建部署目录
echo ""
echo "[2/6] 创建部署目录..."
sudo mkdir -p $DEPLOY_DIR

# 3. 停止旧服务
echo ""
echo "[3/6] 停止旧服务..."
if sudo systemctl is-active --quiet $SERVICE_NAME; then
    sudo systemctl stop $SERVICE_NAME
    echo "服务已停止"
else
    echo "服务未运行，跳过停止步骤"
fi

# 4. 备份旧版本
echo ""
echo "[4/6] 备份旧版本..."
if [ -f "$DEPLOY_DIR/$JAR_NAME" ]; then
    BACKUP_NAME="${JAR_NAME}.$(date +%Y%m%d_%H%M%S).bak"
    sudo cp "$DEPLOY_DIR/$JAR_NAME" "$DEPLOY_DIR/$BACKUP_NAME"
    echo "已备份为: $BACKUP_NAME"
else
    echo "没有旧版本，跳过备份"
fi

# 5. 部署新版本
echo ""
echo "[5/6] 部署新版本..."
sudo cp "target/$JAR_NAME" "$DEPLOY_DIR/"
sudo chown www-data:www-data "$DEPLOY_DIR/$JAR_NAME"
echo "新版本已部署"

# 6. 启动服务
echo ""
echo "[6/6] 启动服务..."
sudo systemctl start $SERVICE_NAME

# 等待服务启动
sleep 3

# 检查服务状态
if sudo systemctl is-active --quiet $SERVICE_NAME; then
    echo ""
    echo "=========================================="
    echo "✓ 后端部署成功！"
    echo "=========================================="
    echo ""
    sudo systemctl status $SERVICE_NAME --no-pager
else
    echo ""
    echo "=========================================="
    echo "✗ 服务启动失败，请查看日志："
    echo "sudo journalctl -u $SERVICE_NAME -n 50"
    echo "=========================================="
    exit 1
fi
