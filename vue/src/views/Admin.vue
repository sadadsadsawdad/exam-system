<template>
  <div class="admin-layout">
    <aside class="admin-sider">
      <div class="admin-logo">
        <div class="logo-icon">🎓</div>
        <div class="logo-text">考试管理系统</div>
      </div>
      <ul class="admin-menu">
        <li :class="['menu-item', activeMenu === 'home' ? 'active' : '']" @click="selectMenu('home')">
          <el-icon class="menu-icon"><House /></el-icon>
          <span>主页</span>
        </li>
        <li :class="['menu-item', activeMenu === 'user' ? 'active' : '']" @click="selectMenu('user')">
          <el-icon class="menu-icon"><User /></el-icon>
          <span>用户管理</span>
        </li>
        <li :class="['menu-item', activeMenu === 'clazz' ? 'active' : '']" @click="selectMenu('clazz')">
          <el-icon class="menu-icon"><School /></el-icon>
          <span>班级管理</span>
        </li>
        <li :class="['menu-item', activeMenu === 'exam' ? 'active' : '']" @click="selectMenu('exam')">
          <el-icon class="menu-icon"><Document /></el-icon>
          <span>考试管理</span>
        </li>
        <li :class="['menu-item', activeMenu === 'bank' ? 'active' : '']" @click="selectMenu('bank')">
          <el-icon class="menu-icon"><Collection /></el-icon>
          <span>题库管理</span>
        </li>
        <li :class="['menu-item', activeMenu === 'score' ? 'active' : '']" @click="selectMenu('score')">
          <el-icon class="menu-icon"><DataAnalysis /></el-icon>
          <span>成绩分析</span>
        </li>
        <li :class="['menu-item', activeMenu === 'grading' ? 'active' : '']" @click="selectMenu('grading')">
          <el-icon class="menu-icon"><ChatLineSquare /></el-icon>
          <span>答卷管理</span>
        </li>
        <li class="menu-group">
          <div :class="['menu-item', 'has-submenu', messageMenuOpen ? 'open' : '', (activeMenu === 'message' || activeMenu === 'messageList') ? 'active' : '']" @click="messageMenuOpen = !messageMenuOpen">
            <el-icon class="menu-icon"><Message /></el-icon>
            <span>消息管理</span>
            <el-icon class="submenu-arrow"><ArrowDown v-if="!messageMenuOpen"/><ArrowUp v-else/></el-icon>
          </div>
          <ul v-show="messageMenuOpen" class="submenu">
            <li :class="['submenu-item', activeMenu === 'messageList' ? 'active' : '']" @click.stop="selectMenu('messageList')">
              <span>消息列表</span>
            </li>
            <li :class="['submenu-item', activeMenu === 'message' ? 'active' : '']" @click.stop="selectMenu('message')">
              <span>消息发送</span>
            </li>
          </ul>
        </li>
      </ul>
    </aside>
    <main class="admin-content">
      <header class="admin-header">
        <div class="admin-header-left">
        </div>
        <div class="admin-header-right">
          <el-dropdown @command="handleDropdownCommand" trigger="click">
            <span class="admin-dropdown-link">
              <el-avatar :size="32" :src="userAvatar" class="admin-avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username">{{ displayName }}</span>
              <el-icon class="el-icon--right"><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown">
                <div class="dropdown-header">
                  <span class="user-role">管理员</span>
                </div>
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
      </header>


      <!-- 主页仪表板内容 -->
      <template v-if="activeMenu === 'home'">
        <section class="admin-dashboard">
          <div class="admin-card blue">
            <div class="card-icon">🏫</div>
            <div class="card-content">
              <div class="card-title">班级总数</div>
              <div class="card-number">{{ clazzTotal }}</div>
            </div>
          </div>
          <div class="admin-card green">
            <div class="card-icon">👥</div>
            <div class="card-content">
              <div class="card-title">班级总人数</div>
              <div class="card-number">{{ studentTotal }}</div>
            </div>
          </div>
          <div class="admin-card orange">
            <div class="card-icon">📝</div>
            <div class="card-content">
              <div class="card-title">试题总数</div>
              <div class="card-number">{{ questionTotal }}</div>
            </div>
          </div>
          <div class="admin-card red">
            <div class="card-icon">📋</div>
            <div class="card-content">
              <div class="card-title">题目总数</div>
              <div class="card-number">{{ examTotal }}</div>
            </div>
          </div>
        </section>

        <section class="admin-charts">
          <div class="chart-card">
            <h3>班级人数分布</h3>
            <div ref="clazzChartRef" class="chart-container"></div>
          </div>
          <div class="chart-card">
            <h3>题目难度分布</h3>
            <div ref="difficultyChartRef" class="chart-container"></div>
          </div>
        </section>
      </template>

      <!-- 用户管理内容 -->
      <template v-else-if="activeMenu === 'user'">
        <section class="admin-section">
          <UserManage />
        </section>
      </template>

      <!-- 考试管理内容 -->
      <section v-else-if="activeMenu === 'exam'" class="admin-section">
        <ExamManage />
      </section>

      <!-- 题库管理内容 -->
      <section v-else-if="activeMenu === 'bank'" class="admin-section">
        <QuestionBank />
      </section>

      <!-- 班级管理内容 -->
      <section v-else-if="activeMenu === 'clazz'" class="admin-section">
        <ClazzManage />
      </section>

      <!-- 成绩分析内容 -->
      <section v-else-if="activeMenu === 'score'" class="admin-section">
        <ExamCompletion />
      </section>

      <!-- 批改列表内容 -->
      <section v-else-if="activeMenu === 'grading'" class="admin-section">
        <GradingList />
      </section>

      <!-- 消息发送内容 -->
      <section v-else-if="activeMenu === 'message'" class="admin-section">
        <MessageSend />
      </section>

      <!-- 消息列表内容 -->
      <section v-else-if="activeMenu === 'messageList'" class="admin-section">
        <MessageList />
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { 
  User, ArrowDown, ArrowUp, SwitchButton, House, School, Document, 
  Collection, DataAnalysis, ChatLineSquare, Message, FullScreen, 
  Bell, CaretBottom 
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import QuestionBank from './QuestionBank.vue'
import ExamManage from './ExamManage.vue'
import AdminExamResult from './AdminExamResult.vue'
import UserManage from './UserManage.vue'
import ClazzManage from './ClazzManage.vue'
import GradingList from './GradingList.vue'
import ExamCompletion from './ExamCompletion.vue'
import MessageSend from './MessageSend.vue'
import MessageList from './MessageList.vue'

const router = useRouter()
const displayName = ref('管理员')
const userAvatar = ref('')
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const clazzChartRef = ref(null)
const difficultyChartRef = ref(null)
const activeMenu = ref('home')
const messageMenuOpen = ref(false)
const clazzTotal = ref(0)
const studentTotal = ref(0)
const questionTotal = ref(0)
const examTotal = ref(0)
const questions = ref([])
const clazzData = ref([])
const difficultyData = ref([])

// ECharts 实例引用，避免重复初始化
let qChartInstance = null
let pChartInstance = null

const initCharts = () => {
  // 班级人数分布
  if (clazzChartRef.value) {
    if (qChartInstance) {
      qChartInstance.dispose()
    }
    qChartInstance = echarts.init(clazzChartRef.value)
    qChartInstance.setOption({
      tooltip: { 
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c}人 ({d}%)'
      },
      legend: { 
        orient: 'vertical',
        left: 'left',
        top: 'middle'
      },
      series: [
        {
          name: '班级人数',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['65%', '50%'],
          data: clazzData.value,
          emphasis: {
            itemStyle: { 
              shadowBlur: 10, 
              shadowOffsetX: 0, 
              shadowColor: 'rgba(0, 0, 0, 0.5)' 
            }
          },
          label: {
            show: false
          },
          labelLine: {
            show: false
          }
        }
      ],
      color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de']
    })
  }

  // 题目难度分布
  if (difficultyChartRef.value) {
    if (pChartInstance) {
      pChartInstance.dispose()
    }
    pChartInstance = echarts.init(difficultyChartRef.value)
    pChartInstance.setOption({
      tooltip: { 
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c}题 ({d}%)'
      },
      legend: { 
        orient: 'vertical',
        left: 'left',
        top: 'middle'
      },
      series: [
        {
          name: '难度分布',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['65%', '50%'],
          data: difficultyData.value,
          emphasis: {
            itemStyle: { 
              shadowBlur: 10, 
              shadowOffsetX: 0, 
              shadowColor: 'rgba(0, 0, 0, 0.5)' 
            }
          },
          label: {
            show: false
          },
          labelLine: {
            show: false
          }
        }
      ],
      color: ['#91cc75', '#fac858', '#ee6666']
    })
  }
}

onMounted(async () => {
  const userStr = sessionStorage.getItem('exam_user')
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      displayName.value = user?.username || '管理员'
      userAvatar.value = user?.avatar || defaultAvatar
    } catch (e) {
      displayName.value = '管理员'
      userAvatar.value = defaultAvatar
    }
  }

  // 从后端获取统计数据
  await fetchDashboardData()

  // 初始化图表
  await nextTick()
  initCharts()
})

// 获取仪表板数据
const fetchDashboardData = async () => {
  try {
    // 获取班级数据
    const clazzRes = await fetch('http://localhost:8081/api/clazz')
    if (clazzRes.ok) {
      const clazzList = await clazzRes.json()
      console.log('班级数据:', clazzList)
      if (clazzList.code === 200 && Array.isArray(clazzList.data)) {
        clazzTotal.value = clazzList.data.length
        // 计算每个班级的人数
        clazzData.value = clazzList.data.map(c => ({
          name: c.name,
          value: c.studentCount || 0
        }))
        // 计算总学生人数
        studentTotal.value = clazzList.data.reduce((sum, c) => sum + (c.studentCount || 0), 0)
      }
    }
  } catch (e) {
    console.error('获取班级数据失败', e)
  }

  try {
    // 获取题目数据
    const questionRes = await fetch('http://localhost:8081/api/questions')
    if (questionRes.ok) {
      const questionList = await questionRes.json()
      if (Array.isArray(questionList)) {
        questions.value = questionList
        questionTotal.value = questionList.length
        // 统计难度分布
        const easyCount = questionList.filter(q => q.difficulty === 1).length
        const mediumCount = questionList.filter(q => q.difficulty === 2).length
        const hardCount = questionList.filter(q => q.difficulty === 3).length
        difficultyData.value = [
          { name: '简单', value: easyCount },
          { name: '中等', value: mediumCount },
          { name: '困难', value: hardCount }
        ]
      }
    }
  } catch (e) {
    console.error('获取题目数据失败', e)
  }

  try {
    // 获取考试数据
    const examRes = await fetch('http://localhost:8081/api/exams')
    if (examRes.ok) {
      const examList = await examRes.json()
      if (examList.code === 200 && Array.isArray(examList.data)) {
        examTotal.value = examList.data.length
      } else if (Array.isArray(examList)) {
        examTotal.value = examList.length
      }
    }
  } catch (e) {
    console.error('获取考试数据失败', e)
  }
}

// 监听菜单切换，回到首页时重新渲染图表
watch(activeMenu, async (val) => {
  if (val === 'home') {
    await nextTick()
    initCharts()
  }
})

// 处理下拉菜单命令
const handleDropdownCommand = (command) => {
  if (command === 'profile') {
    router.push('/admin/profile')
  } else if (command === 'logout') {
    handleLogout()
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出当前账号吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    sessionStorage.removeItem('exam_user')
    router.push('/login')
  }).catch(() => {
    // 取消不做处理
  })
}

const selectMenu = (key) => {
  activeMenu.value = key
  if (key === 'home') {
    router.push('/admin')
  }
}

// 获取面包屑名称
const getBreadcrumbName = (menu) => {
  const nameMap = {
    'home': '主页',
    'user': '用户管理',
    'clazz': '班级管理',
    'exam': '考试管理',
    'bank': '题库管理',
    'score': '成绩分析',
    'grading': '批改列表',
    'completion': '试卷完成',
    'message': '消息发送',
    'messageList': '消息列表'
  }
  return nameMap[menu] || '主页'
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

/* 侧边栏样式 */
.admin-sider {
  width: 220px;
  background: linear-gradient(180deg, #1a1f36 0%, #252d4a 100%);
  color: #fff;
  padding: 0;
  box-shadow: 4px 0 15px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 10;
}

.admin-logo {
  display: flex;
  align-items: center;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-icon {
  font-size: 28px;
  margin-right: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  font-size: 17px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 0.5px;
}

.admin-menu {
  list-style: none;
  margin: 0;
  padding: 12px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  cursor: pointer;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
  margin: 2px 8px;
  border-radius: 8px;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.menu-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-left-color: transparent;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.menu-icon {
  margin-right: 12px;
  font-size: 18px;
  flex-shrink: 0;
}

.menu-group {
  list-style: none;
}

.menu-item.has-submenu {
  justify-content: flex-start;
}

.submenu-arrow {
  margin-left: auto;
  font-size: 12px;
  transition: transform 0.3s;
}

.submenu {
  list-style: none;
  margin: 0;
  padding: 0;
  background: rgba(0, 0, 0, 0.15);
  border-radius: 0 0 8px 8px;
  margin: 0 8px;
  overflow: hidden;
}

.submenu-item {
  display: flex;
  align-items: center;
  padding: 12px 20px 12px 50px;
  cursor: pointer;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  transition: all 0.3s;
  position: relative;
}

.submenu-item::before {
  content: '';
  position: absolute;
  left: 30px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transition: all 0.3s;
}

.submenu-item:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
}

.submenu-item:hover::before {
  background: #667eea;
}

.submenu-item.active {
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

.submenu-item.active::before {
  background: #667eea;
  box-shadow: 0 0 8px rgba(102, 126, 234, 0.6);
}

/* 主内容区 */
.admin-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 头部样式 */
.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 0 24px;
  height: 64px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 5;
}

.admin-header-left {
  display: flex;
  align-items: center;
}

.breadcrumb {
  display: flex;
  align-items: center;
}

.admin-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-right: 16px;
  padding-right: 16px;
  border-right: 1px solid #e8e8e8;
}

.action-icon {
  font-size: 18px;
  color: #666;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.3s;
}

.action-icon:hover {
  background: #f5f5f5;
  color: #667eea;
}

.action-badge {
  cursor: pointer;
}

.admin-dropdown-link {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: all 0.3s;
}

.admin-dropdown-link:hover {
  background: #f5f7fa;
}

.admin-avatar {
  border: 2px solid #e8e8e8;
  transition: border-color 0.3s;
}

.admin-dropdown-link:hover .admin-avatar {
  border-color: #667eea;
}

.username {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.dropdown-header {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 4px;
}

.user-role {
  font-size: 12px;
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
}

/* 仪表板卡片 */
.admin-dashboard {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  padding: 24px;
}

.admin-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.admin-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
}

.admin-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

.admin-card.blue::before {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
}

.admin-card.green::before {
  background: linear-gradient(90deg, #11998e 0%, #38ef7d 100%);
}

.admin-card.orange::before {
  background: linear-gradient(90deg, #f093fb 0%, #f5576c 100%);
}

.admin-card.red::before {
  background: linear-gradient(90deg, #eb3349 0%, #f45c43 100%);
}

.card-icon {
  font-size: 36px;
  width: 70px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
}

.admin-card.blue .card-icon {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
}

.admin-card.green .card-icon {
  background: linear-gradient(135deg, rgba(17, 153, 142, 0.15) 0%, rgba(56, 239, 125, 0.15) 100%);
}

.admin-card.orange .card-icon {
  background: linear-gradient(135deg, rgba(240, 147, 251, 0.15) 0%, rgba(245, 87, 108, 0.15) 100%);
}

.admin-card.red .card-icon {
  background: linear-gradient(135deg, rgba(235, 51, 73, 0.15) 0%, rgba(244, 92, 67, 0.15) 100%);
}

.card-content {
  flex: 1;
}

.card-title {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 8px;
  font-weight: 500;
}

.card-number {
  font-size: 36px;
  font-weight: 700;
  color: #262626;
  margin: 0;
  line-height: 1;
}

/* 图表区域 */
.admin-charts {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
  gap: 20px;
  padding: 0 24px 24px;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.chart-card h3 {
  margin: 0 0 20px;
  font-size: 16px;
  color: #262626;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-card h3::before {
  content: '';
  width: 4px;
  height: 18px;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
}

.chart-container {
  width: 100%;
  height: 300px;
}

/* 内容区域 */
.admin-section {
  margin: 24px;
  background: #fff;
  border-radius: 12px;
  padding: 0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  flex: 1;
  overflow: auto;
}

.admin-section h2 {
  margin: 0 0 16px;
  font-size: 18px;
  color: #262626;
  font-weight: 600;
}

/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* 滚动条美化 */
.admin-sider::-webkit-scrollbar,
.admin-section::-webkit-scrollbar {
  width: 6px;
}

.admin-sider::-webkit-scrollbar-thumb,
.admin-section::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.admin-sider::-webkit-scrollbar-track,
.admin-section::-webkit-scrollbar-track {
  background: transparent;
}
</style>
