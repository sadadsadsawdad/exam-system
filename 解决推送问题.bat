@echo off
chcp 65001 >nul
echo ========================================
echo 解决 GitHub 推送问题
echo ========================================
echo.

echo 方案 1: 使用 SSH 方式推送（推荐）
echo ----------------------------------------
echo 如果你已配置 SSH key，执行：
echo git remote set-url origin git@github.com:sadadsadsawdad/exam-system.git
echo git push -u origin main
echo.

echo 方案 2: 配置代理（如果使用 VPN）
echo ----------------------------------------
echo 如果你使用代理，执行：
echo git config --global http.proxy http://127.0.0.1:7890
echo git config --global https.proxy http://127.0.0.1:7890
echo git push -u origin main
echo.

echo 方案 3: 增加缓冲区大小
echo ----------------------------------------
echo git config --global http.postBuffer 524288000
echo git push -u origin main
echo.

echo 方案 4: 使用 GitHub Desktop（最简单）
echo ----------------------------------------
echo 1. 下载 GitHub Desktop: https://desktop.github.com/
echo 2. 打开项目文件夹
echo 3. 点击 Push origin 按钮
echo.

echo ========================================
echo 请选择一个方案执行
echo ========================================
pause
