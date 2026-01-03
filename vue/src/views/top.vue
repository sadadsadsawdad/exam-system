<template>
  <div class="layout-container">
    <!-- 头部 -->
    <div class="header">
      <div class="header-content">
        <div class="logo">
          <div class="logo-mark">📚</div>
          <div class="logo-text">
            <div class="logo-title">在线考试系统</div>
            <div class="logo-subtitle">Campus Examination</div>
          </div>
        </div>
        <div class="menu-container">
          <el-menu
            :default-active="activeMenu"
            class="menu"
            mode="horizontal"
            :ellipsis="false"
            @select="handleMenuSelect"
          >
            <el-menu-item index="/top/home">
              <el-icon><House /></el-icon>
              <span>主页</span>
            </el-menu-item>
            <el-menu-item index="/top/exams">
              <el-icon><Document /></el-icon>
              <span>在线考试</span>
            </el-menu-item>
            <el-menu-item index="/top/history">
              <el-icon><Clock /></el-icon>
              <span>考试记录</span>
            </el-menu-item>
          </el-menu>
        </div>
        <div class="user-info">
          <!-- 通知图标 -->
          <el-popover placement="bottom" :width="350" trigger="click" @show="fetchNotifications">
            <template #reference>
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
                <el-button :icon="Bell" circle size="small" style="margin-right: 15px;" />
              </el-badge>
            </template>
            <div class="notification-panel">
              <div class="notification-header">
                <span>消息通知</span>
                <el-button type="primary" link size="small" @click="markAllRead" v-if="unreadCount > 0">全部已读</el-button>
              </div>
              <div class="notification-list" v-if="notifications.length > 0">
                <div 
                  v-for="n in notifications" 
                  :key="n.id" 
                  :class="['notification-item', { unread: n.isRead === 0 }]"
                  @click="handleNotificationClick(n)"
                >
                  <div class="notification-title">{{ n.title }}</div>
                  <div class="notification-content">{{ n.content }}</div>
                  <div class="notification-time">{{ n.createTime }}</div>
                </div>
              </div>
              <div v-else class="notification-empty">暂无通知</div>
            </div>
          </el-popover>

          <el-dropdown @command="handleDropdownCommand">
            <span class="el-dropdown-link">
              <el-avatar :size="32" :src="userAvatar" class="user-avatar-small">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username-text">{{ displayName }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
    <!-- 头部结束 -->

    <!-- 主体内容 -->
    <div class="main-container">
      <!-- 右侧内容区 -->
      <div class="content">
        <el-carousel
          v-if="isHome"
          height="300px"
          style="margin-bottom: 20px; width: 100%;"
        >
          <el-carousel-item>
            <div style="display: flex; justify-content: center; align-items: center; height: 100%;">
              <img src="@/assets/img/2.jpg" alt="轮播图1" style="width: 100%; height: 100%; object-fit: cover;"/>
            </div>
          </el-carousel-item>
          <el-carousel-item>
            <div style="display: flex; justify-content: center; align-items: center; height: 100%;">
              <img src="@/assets/img/1.jpg" alt="轮播图2" style="width: 100%; height: 100%; object-fit: cover;"/>
            </div>
          </el-carousel-item>
          <el-carousel-item>
            <div style="display: flex; justify-content: center; align-items: center; height: 100%;">
              <img src="@/assets/img/3.jpg" alt="轮播图3" style="width: 100%; height: 100%; object-fit: cover;"/>
            </div>
          </el-carousel-item>
        </el-carousel>
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'

import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'

import {
  House,
  Document,
  Clock,
  Bell,
  User,
  ArrowDown,
  SwitchButton
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const activeMenu = ref('')
const displayName = ref('未登录')
const userId = ref(null)
const userAvatar = ref('')

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 通知相关
const notifications = ref([])
const unreadCount = ref(0)

// 是否处于首页，用于控制轮播图只在首页显示
const isHome = computed(() => route.path === '/top/home')

// 获取通知列表
const fetchNotifications = async () => {
  if (!userId.value) return
  try {
    const res = await fetch(`http://localhost:8081/api/notifications/user/${userId.value}`)
    if (res.ok) {
      notifications.value = await res.json()
    }
  } catch (e) {
    console.error('获取通知失败', e)
  }
}

// 获取未读数量
const fetchUnreadCount = async () => {
  if (!userId.value) return
  try {
    const res = await fetch(`http://localhost:8081/api/notifications/user/${userId.value}/unread-count`)
    if (res.ok) {
      const data = await res.json()
      unreadCount.value = data.count || 0
    }
  } catch (e) {
    console.error('获取未读数量失败', e)
  }
}

// 标记全部已读
const markAllRead = async () => {
  if (!userId.value) return
  try {
    await fetch(`http://localhost:8081/api/notifications/user/${userId.value}/read-all`, { method: 'PUT' })
    unreadCount.value = 0
    notifications.value.forEach(n => n.isRead = 1)
  } catch (e) {
    console.error('标记已读失败', e)
  }
}

// 点击通知
const handleNotificationClick = async (n) => {
  if (n.isRead === 0) {
    try {
      await fetch(`http://localhost:8081/api/notifications/${n.id}/read`, { method: 'PUT' })
      n.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (e) {
      console.error('标记已读失败', e)
    }
  }
  // 如果是考试通知，跳转到考试列表
  if (n.type === 'EXAM' && n.relatedId) {
    router.push('/top/exams')
  }
}

// 监听路由变化，设置当前激活的菜单项，并从本地读取用户名
onMounted(() => {
  activeMenu.value = route.path

  const userStr = sessionStorage.getItem('exam_user')
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      displayName.value = user?.username || '未登录'
      userId.value = user?.id || null
      userAvatar.value = user?.avatar || defaultAvatar
      // 获取未读通知数量
      fetchUnreadCount()
    } catch (e) {
      displayName.value = '未登录'
      userAvatar.value = defaultAvatar
    }
  }
})

// 处理菜单选择事件
const handleMenuSelect = (index) => {
  activeMenu.value = index
  router.push(index)
}

// 处理下拉菜单命令
const handleDropdownCommand = (command) => {
  if (command === 'profile') {
    router.push('/top/profile')
  } else if (command === 'logout') {
    handleLogout()
  }
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出当前账号吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    sessionStorage.removeItem('exam_user')
    router.push('/login')
  }).catch(() => {})
}

</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%);
}

.header {
  height: 70px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
  position: relative;
  z-index: 100;
}

.header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  pointer-events: none;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 40px;
  position: relative;
  z-index: 1;
}

.logo {
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo-mark {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.logo-title {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 1px;
}

.logo-subtitle {
  font-size: 11px;
  opacity: 0.85;
  letter-spacing: 0.5px;
}

.menu-container {
  flex: 1;
  display: flex;
  justify-content: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
}

.main-container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  width: 200px;
  border-right: 1px solid #e6e6e6;
  height: calc(100vh - 70px);
  overflow-y: auto;
}

.menu {
  border: 0;
  height: 100%;
  background: transparent;
}

.content {
  flex: 1;
  width: 0;
  background: linear-gradient(180deg, #f8f9fc 0%, #eef1f5 100%);
  padding: 24px 32px;
  overflow-y: auto;
}

.el-menu-item {
  height: 50px;
  line-height: 50px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 15px;
  font-weight: 500;
  padding: 0 24px;
  position: relative;
  overflow: visible;
  text-overflow: clip;
  border-radius: 8px;
  margin: 0 8px;
  transition: all 0.3s ease;
}

.el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.15);
  color: #ffffff;
  transform: translateY(-1px);
}

.el-menu-item.is-active {
  background-color: rgba(255, 255, 255, 0.2);
  color: #ffffff;
  font-weight: 600;
}

.el-menu-item.is-active::after {
  content: "";
  position: absolute;
  left: 20px;
  right: 20px;
  bottom: 6px;
  height: 3px;
  border-radius: 999px;
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(255, 255, 255, 0.4);
}

.notification-badge {
  cursor: pointer;
}

.notification-panel {
  max-height: 400px;
  overflow-y: auto;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
  margin-bottom: 10px;
  font-weight: bold;
}

.notification-list {
  max-height: 350px;
  overflow-y: auto;
}

.notification-item {
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.notification-item:hover {
  background: #f5f5f5;
}

.notification-item.unread {
  background: #ecf5ff;
  border-left: 3px solid #409eff;
}

.notification-title {
  font-weight: 500;
  color: #333;
  margin-bottom: 5px;
}

.notification-content {
  font-size: 12px;
  color: #666;
  margin-bottom: 5px;
}

.notification-time {
  font-size: 11px;
  color: #999;
}

.notification-empty {
  text-align: center;
  padding: 30px;
  color: #999;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  cursor: pointer;
  outline: none;
}

.user-avatar-small {
  border: 2px solid rgba(255, 255, 255, 0.8);
}

.username-text {
  font-size: 14px;
  font-weight: 500;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.el-dropdown-menu .el-dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>