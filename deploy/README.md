# 部署脚本说明

本目录包含了在线考试系统的部署配置文件和自动化脚本。

## 文件说明

### 配置文件

- `exam-backend.service` - systemd 服务配置文件（后端）
- `nginx.conf` - Nginx 配置文件（前端 + 反向代理）
- `backup-db.sh` - 数据库自动备份脚本

### 部署脚本

- `setup-server.sh` - 服务器环境初始化脚本
- `deploy-backend.sh` - 后端部署脚本
- `deploy-frontend.sh` - 前端部署脚本
- `quick-deploy.sh` - 一键部署脚本（前后端）

## 快速开始

### 1. 首次部署

#### 步骤 1：配置服务器环境

```bash
# 在服务器上执行
sudo bash deploy/setup-server.sh
```

#### 步骤 2：配置数据库

```bash
# 登录 MySQL
sudo mysql -u root -p

# 创建数据库和用户
CREATE DATABASE exam CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'examuser'@'localhost' IDENTIFIED BY '你的密码';
GRANT ALL PRIVILEGES ON exam.* TO 'examuser'@'localhost';
FLUSH PRIVILEGES;
EXIT;

# 导入初始化 SQL
mysql -u examuser -p exam < springboot/src/main/resources/sql/init.sql
```

#### 步骤 3：修改配置文件

1. 修改 `springboot/src/main/resources/application.properties`
   - 更新数据库密码
   - 确认 GCC 路径

2. 修改 `deploy/nginx.conf`
   - 更新 `server_name` 为你的域名或 IP

#### 步骤 4：安装 systemd 服务

```bash
# 复制服务文件
sudo cp deploy/exam-backend.service /etc/systemd/system/

# 重载 systemd
sudo systemctl daemon-reload

# 启用服务
sudo systemctl enable exam-backend
```

#### 步骤 5：配置 Nginx

```bash
# 复制配置文件
sudo cp deploy/nginx.conf /etc/nginx/sites-available/exam-system

# 创建软链接
sudo ln -s /etc/nginx/sites-available/exam-system /etc/nginx/sites-enabled/

# 测试配置
sudo nginx -t

# 如果测试通过，重启 Nginx
sudo systemctl restart nginx
```

#### 步骤 6：执行部署

```bash
# 一键部署前后端
bash deploy/quick-deploy.sh

# 或者分别部署
bash deploy/deploy-backend.sh  # 部署后端
bash deploy/deploy-frontend.sh  # 部署前端
```

### 2. 更新部署

当代码有更新时，只需重新运行部署脚本：

```bash
# 更新全部
bash deploy/quick-deploy.sh

# 或单独更新
bash deploy/deploy-backend.sh   # 仅更新后端
bash deploy/deploy-frontend.sh  # 仅更新前端
```

### 3. 配置数据库备份

```bash
# 修改备份脚本中的密码
nano deploy/backup-db.sh

# 添加执行权限
chmod +x deploy/backup-db.sh

# 测试备份
bash deploy/backup-db.sh

# 设置定时备份（每天凌晨 2 点）
sudo crontab -e
# 添加以下行：
0 2 * * * /path/to/deploy/backup-db.sh >> /var/log/db-backup.log 2>&1
```

## 常用命令

### 服务管理

```bash
# 查看后端服务状态
sudo systemctl status exam-backend

# 启动/停止/重启后端
sudo systemctl start exam-backend
sudo systemctl stop exam-backend
sudo systemctl restart exam-backend

# 查看后端日志
sudo journalctl -u exam-backend -f

# 重启 Nginx
sudo systemctl restart nginx

# 查看 Nginx 日志
sudo tail -f /var/log/nginx/exam-error.log
sudo tail -f /var/log/nginx/exam-access.log
```

### 故障排查

```bash
# 检查端口占用
sudo netstat -tlnp | grep -E '80|443|8081|3306'

# 检查服务状态
sudo systemctl status exam-backend
sudo systemctl status nginx
sudo systemctl status mysql

# 测试数据库连接
mysql -u examuser -p exam

# 测试 Nginx 配置
sudo nginx -t

# 查看系统资源
htop
df -h
free -h
```

## 注意事项

1. **首次部署前**，请确保：
   - 已修改所有配置文件中的密码和域名
   - 服务器防火墙已开放 80、443 端口
   - 数据库已正确初始化

2. **生产环境建议**：
   - 使用强密码
   - 配置 HTTPS（使用 Let's Encrypt）
   - 定期备份数据库
   - 监控服务器资源

3. **Windows 本地开发**：
   - 这些脚本是为 Linux 服务器设计的
   - Windows 下请参考 `../部署文档.md` 手动部署

## 获取帮助

详细的部署说明请参考项目根目录的 `部署文档.md`。

如遇问题，请检查：
- 服务日志：`sudo journalctl -u exam-backend -n 100`
- Nginx 日志：`/var/log/nginx/exam-error.log`
- 系统资源：`htop`、`df -h`
