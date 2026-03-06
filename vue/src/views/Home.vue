<template>
  <div class="dashboard">
    <div class="welcome-section">
      <el-card class="welcome-card" shadow="never">
        <div class="welcome-content">
          <div class="welcome-text">
            <div class="welcome-title">你好，{{ username }} 👋</div>
            <div class="welcome-subtitle">今天你有 <span class="highlight">{{ todayExamCount }}</span> 场考试待参加，祝你考试顺利，金榜题名！</div>
          </div>
          <div class="welcome-illustration">
            <div class="campus-badge">
              <span class="campus-icon">🎓</span>
              <span class="campus-text">校园在线考试</span>
            </div>
            <div class="campus-decor">
              <span class="campus-dot"></span>
              <span class="campus-dot"></span>
              <span class="campus-dot"></span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <el-row :gutter="20" class="stats-section">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon bg-primary">
              <Document />
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ pendingExamCount }}</div>
              <div class="stat-label">待参加考试</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon bg-success">
              <Check />
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ finishedExamCount }}</div>
              <div class="stat-label">已完成考试</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon bg-warning">
              <Clock />
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ ongoingExamCount }}</div>
              <div class="stat-label">未完成考试</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon bg-info">
              <TrendCharts />
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ avgScore }}</div>
              <div class="stat-label">平均分</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="recent-section">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近考试</span>
            </div>
          </template>
          <el-table :data="recentExams" style="width: 100%">
            <el-table-column prop="title" label="考试名称" />
            <el-table-column prop="startTime" label="考试时间" />
            <el-table-column prop="durationMinutes" label="考试时长(分钟)" />
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template #default="scope">
                <el-button
                  size="small"
                  type="primary"
                  v-if="scope.row.status === 'PUBLISHED' || scope.row.status === '未开始'"
                  @click="enterExam(scope.row)"
                >
                  参加考试
                </el-button>
                <el-button
                  size="small"
                  v-else
                  @click="goToHistory"
                >
                  查看结果
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" icon="Document" @click="goToTest">在线考试</el-button>
            <el-button type="success" icon="TrendCharts" @click="goToScore">成绩查询</el-button>
            <el-button type="info" icon="Clock" @click="goToHistory">历史记录</el-button>
          </div>
        </el-card>
        
        <el-card v-if="lastResult" style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>最近成绩</span>
            </div>
          </template>
          <div class="last-score">
            <div class="last-score-title">{{ lastResult.examTitle || '最近一次考试' }}</div>
            <div class="last-score-row">
              <span>得分：</span>
              <span class="last-score-value">{{ lastResult.score }}</span>
            </div>
            <div class="last-score-row">
              <span>状态：</span>
              <el-tag :type="getStatusType('已完成')" size="small">已完成</el-tag>
            </div>
            <div class="last-score-row time">
              <span>时间：</span>
              <span>{{ lastResult.submitTime }}</span>
            </div>
            <div class="last-score-footer">
              <el-button type="primary" link size="small" @click="goToHistory">查看全部成绩</el-button>
            </div>
          </div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>考试须知</span>
            </div>
          </template>
          <ul class="notice-list">
            <li>考试开始前请确保网络稳定</li>
            <li>考试过程中请勿刷新或关闭页面</li>
            <li>考试结束后系统将自动提交试卷</li>
            <li>如有疑问请联系管理员</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Document,
  Check,
  Clock,
  TrendCharts
} from '@element-plus/icons-vue'

// 用户信息和统计数据
const username = ref('同学')
const todayExamCount = ref(0)
const pendingExamCount = ref(0)
const finishedExamCount = ref(0)
const ongoingExamCount = ref(0)
const avgScore = ref(0)

// 最近考试数据（从后端考试列表中截取前几条）
const recentExams = ref([])
// 最近一次成绩
const lastResult = ref(null)

const router = useRouter()

const loadUserAndExams = async () => {
  // 用户名 & 当前用户ID
  const userStr = sessionStorage.getItem('exam_user')
  let currentUserId = null
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      username.value = user?.username || '同学'
      currentUserId = user && user.id ? user.id : null
    } catch (e) {
      username.value = '同学'
      currentUserId = null
    }
  }

  // 考试数据
  try {
    const res = await fetch('http://localhost:8081/api/exams')
    if (res.ok) {
      const exams = await res.json()
      // 简单按status统计：PUBLISHED 视为待参加
      pendingExamCount.value = exams.filter(e => e.status === 'PUBLISHED').length
      ongoingExamCount.value = exams.filter(e => e.status === 'ONGOING' || e.status === '进行中').length
      todayExamCount.value = pendingExamCount.value

      // 取前 4 条作为最近考试
      recentExams.value = exams.slice(0, 4)
    }
  } catch (e) {
    // 忽略错误，保持初始值
  }

  // 成绩数据：根据当前用户的考试结果统计“已完成考试”和“平均分”
  if (currentUserId) {
    try {
      const res2 = await fetch(`http://localhost:8081/api/exam-results?userId=${currentUserId}`)
      if (res2.ok) {
        const results = await res2.json()
        if (Array.isArray(results) && results.length > 0) {
          finishedExamCount.value = results.length
          const total = results.reduce((sum, r) => sum + (r.score || 0), 0)
          avgScore.value = Math.round((total / results.length) * 10) / 10
          lastResult.value = results[0]
        } else {
          finishedExamCount.value = 0
          avgScore.value = 0
          lastResult.value = null
        }
      }
    } catch (e) {
      // 出错时保持默认 0
    }
  }
}

onMounted(() => {
  loadUserAndExams()
})

// 获取状态标签类型
const getStatusType = (status) => {
  switch (status) {
    case '已完成': return 'success'
    case '未开始':
    case 'PUBLISHED':
      return 'info'
    case '进行中': return 'warning'
    default: return 'info'
  }
}

// 从首页直接进入某场考试
const enterExam = (row) => {
  if (!row) return
  router.push({
    path: '/top/test',
    query: {
      examId: row.id,
      title: row.title,
      duration: row.durationMinutes,
      totalScore: row.totalScore
    }
  })
}

// 导航到不同页面
const goToTest = () => {
  router.push('/top/exams')
}

const goToScore = () => {
  router.push('/top/history')
}

const goToHistory = () => {
  router.push('/top/history')
}
</script>

<style scoped>
.dashboard {
  padding: 0;
  background: transparent;
  min-height: calc(100vh - 70px);
  box-sizing: border-box;
}

.welcome-section {
  margin-bottom: 24px;
}

.welcome-card {
  border-radius: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.3);
  overflow: hidden;
  position: relative;
}

.welcome-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  pointer-events: none;
}

.welcome-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 1;
}

.welcome-text {
  max-width: 65%;
}

.welcome-title {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 10px;
  text-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.welcome-subtitle {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
}

.welcome-subtitle .highlight {
  color: #ffd700;
  font-weight: 700;
  font-size: 18px;
}

.welcome-illustration {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.campus-badge {
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 999px;
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.campus-icon {
  font-size: 24px;
}

.campus-text {
  font-size: 14px;
  color: #fff;
  font-weight: 600;
}

.campus-decor {
  display: flex;
  gap: 8px;
}

.campus-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
}

.campus-dot:first-child {
  background: rgba(255, 255, 255, 0.8);
}

.stats-section {
  margin-bottom: 28px;
}

.stat-card {
  height: 130px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: none;
  transition: all 0.3s ease;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.12);
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 8px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 18px;
  color: #fff;
  font-size: 24px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
}

.bg-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.bg-success {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.bg-warning {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.bg-info {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: 800;
  margin-bottom: 6px;
  color: #303133;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.recent-section {
  margin-top: 4px;
}

.recent-section :deep(.el-card) {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.card-header {
  font-weight: 700;
  font-size: 16px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-header::before {
  content: '';
  width: 4px;
  height: 18px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 2px;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-actions .el-button {
  width: 100%;
  justify-content: flex-start;
  height: 44px;
  border-radius: 10px;
  font-weight: 500;
}

.notice-list {
  padding-left: 20px;
  color: #606266;
  line-height: 2;
}

.notice-list li {
  position: relative;
}

.notice-list li::marker {
  color: #667eea;
}

/* 最近成绩卡片 */
.last-score {
  padding: 8px 0;
}

.last-score-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  font-size: 15px;
}

.last-score-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px dashed #eee;
}

.last-score-row.time {
  border-bottom: none;
  color: #909399;
  font-size: 13px;
}

.last-score-value {
  font-size: 24px;
  font-weight: 700;
  color: #667eea;
}

.last-score-footer {
  margin-top: 12px;
  text-align: center;
}

/* 表格美化 */
:deep(.el-table) {
  border-radius: 10px;
  overflow: hidden;
}

:deep(.el-table th) {
  background: #f8f9fc !important;
  font-weight: 600;
  color: #303133;
}

:deep(.el-table td) {
  padding: 14px 0;
}
</style>