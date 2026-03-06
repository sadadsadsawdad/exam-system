#!/bin/bash

# 一键部署脚本（前后端）
# 使用方法：在项目根目录执行 bash deploy/quick-deploy.sh

set -e

echo "=========================================="
echo "一键部署在线考试系统"
echo "=========================================="

# 检查是否在项目根目录
if [ ! -d "springboot" ] || [ ! -d "vue" ]; then
    echo "错误：请在项目根目录执行此脚本"
    exit 1
fi

# 部署后端
echo ""
echo "==================== 部署后端 ===================="
bash deploy/deploy-backend.sh

# 部署前端
echo ""
echo "==================== 部署前端 ===================="
bash deploy/deploy-frontend.sh

echo ""
echo "=========================================="
echo "✓ 全部部署完成！"
echo "=========================================="
echo ""
echo "服务状态："
echo "- 后端服务：sudo systemctl status exam-backend"
echo "- Nginx：sudo systemctl status nginx"
echo ""
echo "访问地址：http://your-server-ip"
echo "后端 API：http://your-server-ip/api/"
