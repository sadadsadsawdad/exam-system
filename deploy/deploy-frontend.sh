#!/bin/bash

# 前端部署脚本
# 使用方法：在项目根目录执行 bash deploy/deploy-frontend.sh

set -e  # 遇到错误立即退出

echo "=========================================="
echo "开始部署前端项目"
echo "=========================================="

# 配置变量
DEPLOY_DIR="/var/www/exam-frontend"
BUILD_DIR="vue/dist"

# 1. 安装依赖
echo ""
echo "[1/5] 安装依赖..."
cd vue
npm install

# 2. 构建项目
echo ""
echo "[2/5] 构建生产版本..."
npm run build

if [ ! -d "dist" ]; then
    echo "错误：构建失败，找不到 dist 目录"
    exit 1
fi

echo "构建成功！"

# 3. 创建部署目录
echo ""
echo "[3/5] 创建部署目录..."
sudo mkdir -p $DEPLOY_DIR

# 4. 备份旧版本
echo ""
echo "[4/5] 备份旧版本..."
if [ -d "$DEPLOY_DIR" ] && [ "$(ls -A $DEPLOY_DIR)" ]; then
    BACKUP_DIR="${DEPLOY_DIR}.$(date +%Y%m%d_%H%M%S).bak"
    sudo cp -r "$DEPLOY_DIR" "$BACKUP_DIR"
    echo "已备份为: $BACKUP_DIR"
    
    # 清空旧文件
    sudo rm -rf $DEPLOY_DIR/*
else
    echo "没有旧版本，跳过备份"
fi

# 5. 部署新版本
echo ""
echo "[5/5] 部署新版本..."
sudo cp -r dist/* $DEPLOY_DIR/
sudo chown -R www-data:www-data $DEPLOY_DIR
echo "新版本已部署"

# 重启 Nginx
echo ""
echo "重启 Nginx..."
sudo systemctl restart nginx

# 检查 Nginx 状态
if sudo systemctl is-active --quiet nginx; then
    echo ""
    echo "=========================================="
    echo "✓ 前端部署成功！"
    echo "=========================================="
    echo ""
    echo "访问地址：http://your-server-ip"
else
    echo ""
    echo "=========================================="
    echo "✗ Nginx 启动失败，请查看日志："
    echo "sudo tail -f /var/log/nginx/error.log"
    echo "=========================================="
    exit 1
fi
