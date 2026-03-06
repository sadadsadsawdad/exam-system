@echo off
echo 正在推送代码到 GitHub...
echo.

REM 配置 Git 使用默认编辑器为记事本（避免 Vim）
git config --global core.editor "notepad"

REM 检查是否有未提交的更改
git add .
git commit -m "更新部署文件" 2>nul

REM 推送到 GitHub
echo 开始推送...
git push -u origin main

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo 成功推送到 GitHub！
    echo ========================================
    echo.
    echo 你的仓库地址：
    echo https://github.com/sadadsadsawdad/exam-system
    echo.
    echo 下一步：
    echo 1. 访问 https://railway.app/ 部署后端
    echo 2. 访问 https://vercel.com/ 部署前端
    echo.
    echo 详细步骤请查看：开始部署.md
    echo ========================================
) else (
    echo.
    echo 推送失败，请检查网络连接或 GitHub 权限
)

pause
