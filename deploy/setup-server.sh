#!/bin/bash

# 服务器初始化脚本（Ubuntu/Debian）
# 使用方法：在服务器上执行 sudo bash setup-server.sh

set -e

echo "=========================================="
echo "开始配置服务器环境"
echo "=========================================="

# 检查是否为 root 用户
if [ "$EUID" -ne 0 ]; then 
    echo "请使用 sudo 运行此脚本"
    exit 1
fi

# 1. 更新系统
echo ""
echo "[1/8] 更新系统包..."
apt update && apt upgrade -y

# 2. 安装 JDK 17
echo ""
echo "[2/8] 安装 JDK 17..."
apt install openjdk-17-jdk -y
java -version

# 3. 安装 MySQL
echo ""
echo "[3/8] 安装 MySQL..."
apt install mysql-server -y
systemctl start mysql
systemctl enable mysql

# 4. 安装 Nginx
echo ""
echo "[4/8] 安装 Nginx..."
apt install nginx -y
systemctl start nginx
systemctl enable nginx

# 5. 安装 Maven
echo ""
echo "[5/8] 安装 Maven..."
apt install maven -y
mvn -version

# 6. 安装 Node.js
echo ""
echo "[6/8] 安装 Node.js 20.x..."
curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
apt install nodejs -y
node -v
npm -v

# 7. 安装 GCC
echo ""
echo "[7/8] 安装 GCC..."
apt install build-essential -y
gcc --version

# 8. 配置防火墙
echo ""
echo "[8/8] 配置防火墙..."
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
echo "y" | ufw enable

echo ""
echo "=========================================="
echo "✓ 服务器环境配置完成！"
echo "=========================================="
echo ""
echo "下一步："
echo "1. 配置 MySQL 数据库"
echo "2. 上传项目文件到服务器"
echo "3. 配置 systemd 服务和 Nginx"
echo "4. 运行部署脚本"
echo ""
echo "详细步骤请参考 部署文档.md"
