<template>
  <div class="exam-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-icon class="header-icon"><Document /></el-icon>
        <span class="header-title">在线考试</span>
        <span class="header-subtitle">共 {{ exams.length }} 场考试</span>
      </div>
    </div>

    <!-- 考试卡片列表 -->
    <div class="exam-grid">
      <div 
        v-for="exam in exams" 
        :key="exam.id" 
        class="exam-card"
        :class="{ 'participated': hasParticipated(exam.id), 'disabled': !canEnterExam(exam) }"
      >
        <div class="exam-card-header">
          <div class="exam-subject">{{ exam.subject || '综合' }}</div>
          <el-tag 
            :type="hasParticipated(exam.id) ? 'success' : (canEnterExam(exam) ? 'primary' : 'info')"
            size="small"
            class="exam-status-tag"
          >
            {{ hasParticipated(exam.id) ? '已完成' : (canEnterExam(exam) ? '可参加' : '已结束') }}
          </el-tag>
        </div>
        
        <h3 class="exam-title">{{ exam.title }}</h3>
        
        <div class="exam-info-grid">
          <div class="info-item">
            <el-icon><Clock /></el-icon>
            <span>{{ exam.durationMinutes }}分钟</span>
          </div>
          <div class="info-item">
            <el-icon><Trophy /></el-icon>
            <span>{{ exam.totalScore }}分</span>
          </div>
        </div>
        
        <div class="exam-time">
          <div class="time-row">
            <span class="time-label">开始时间</span>
            <span class="time-value">{{ formatTime(exam.startTime) }}</span>
          </div>
          <div class="time-row">
            <span class="time-label">结束时间</span>
            <span class="time-value">{{ formatTime(exam.endTime) }}</span>
          </div>
        </div>
        
        <el-button
          :type="hasParticipated(exam.id) ? 'info' : 'primary'"
          class="enter-btn"
          :disabled="!canEnterExam(exam) || hasParticipated(exam.id)"
          @click="enterExam(exam)"
        >
          <el-icon v-if="!hasParticipated(exam.id)"><Right /></el-icon>
          {{ hasParticipated(exam.id) ? '已完成考试' : '进入考试' }}
        </el-button>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="exams.length === 0" class="empty-state">
      <el-icon class="empty-icon"><Document /></el-icon>
      <p>暂无可参加的考试</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Clock, Trophy, Right } from '@element-plus/icons-vue'

const exams = ref([])
// 已参加考试的 examId 映射
const participatedMap = ref({})
const router = useRouter()

const fetchExams = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/exams')
    if (!res.ok) {
      throw new Error('获取考试列表失败')
    }
    exams.value = await res.json()
  } catch (e) {
    ElMessage.error(e.message || '获取考试列表失败')
  }
}

// 加载当前用户的考试成绩，构建已参加考试映射
const loadParticipated = async () => {
  const userStr = sessionStorage.getItem('exam_user')
  if (!userStr) return
  let user = null
  try {
    user = JSON.parse(userStr)
  } catch (e) {
    return
  }
  if (!user || !user.id) return

  try {
    const res = await fetch(`http://localhost:8081/api/exam-results?userId=${user.id}`)
    if (!res.ok) return
    const list = await res.json()
    const map = {}
    if (Array.isArray(list)) {
      list.forEach(r => {
        if (r && r.examId != null) {
          map[r.examId] = true
        }
      })
    }
    participatedMap.value = map
  } catch (e) {
    // 忽略错误，保持默认未参加
  }
}

const hasParticipated = (examId) => {
  if (examId == null) return false
  return !!participatedMap.value[examId]
}

// 判断当前用户是否可以进入该考试：
// 1）如果状态为已结束(FINISHED)，不允许进入；
// 2）如果设置了结束时间且当前时间晚于结束时间，不允许进入；
// 3）其他情况允许进入（包含已发布 / 草稿等）。
const canEnterExam = (row) => {
  if (!row) return false
  if (row.status === 'FINISHED') {
    return false
  }
  if (row.endTime) {
    const now = new Date().getTime()
    const end = new Date(row.endTime).getTime()
    if (!Number.isNaN(end) && now > end) {
      return false
    }
  }
  return true
}

const enterExam = (row) => {
  if (hasParticipated(row.id)) {
    ElMessage.warning('该考试已参加，不能重复作答')
    return
  }
  if (!canEnterExam(row)) {
    ElMessage.warning('当前考试未开放或已结束，不能进入作答')
    return
  }
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


const formatTime = (time) => {
  if (!time) return '未设置'
  return time.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  fetchExams()
  loadParticipated()
})
</script>

<style scoped>
.exam-list-container {
  min-height: calc(100vh - 70px);
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px 32px;
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.3);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 28px;
  color: #fff;
}

.header-title {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
}

.header-subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.15);
  padding: 4px 12px;
  border-radius: 12px;
}

/* 考试卡片网格 */
.exam-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.exam-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.exam-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.12);
  border-color: #667eea;
}

.exam-card.participated {
  background: linear-gradient(135deg, #f0fff4 0%, #e8f5e9 100%);
  border-color: #67c23a;
}

.exam-card.disabled {
  opacity: 0.7;
}

.exam-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.exam-subject {
  font-size: 12px;
  color: #667eea;
  font-weight: 600;
  background: rgba(102, 126, 234, 0.1);
  padding: 4px 10px;
  border-radius: 6px;
}

.exam-title {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 16px 0;
  line-height: 1.4;
}

.exam-info-grid {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px dashed #eee;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.info-item .el-icon {
  color: #667eea;
}

.exam-time {
  margin-bottom: 16px;
}

.time-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
}

.time-label {
  font-size: 12px;
  color: #909399;
}

.time-value {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.enter-btn {
  width: 100%;
  height: 44px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 15px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #909399;
}

.empty-icon {
  font-size: 64px;
  color: #ddd;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
}
</style>
