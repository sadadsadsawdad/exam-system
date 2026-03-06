<template>
  <div class="exam-history-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-icon class="header-icon"><Clock /></el-icon>
        <div class="header-text">
          <span class="header-title">考试记录</span>
          <span class="header-subtitle">查看您的历史考试成绩与答题详情</span>
        </div>
      </div>
      <div class="header-stats">
        <div class="stat-item">
          <span class="stat-value">{{ results.length }}</span>
          <span class="stat-label">总考试</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">{{ gradedCount }}</span>
          <span class="stat-label">已批改</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">{{ averageScore }}</span>
          <span class="stat-label">平均分</span>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-radio-group v-model="filterStatus" size="default">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="graded">已批改</el-radio-button>
        <el-radio-button label="pending">待批改</el-radio-button>
        <el-radio-button label="absent">缺考</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 考试记录列表 -->
    <div class="history-list" v-if="filteredResults.length > 0">
      <div 
        v-for="item in filteredResults" 
        :key="item.id" 
        :class="['history-card', getCardClass(item)]"
      >
        <div class="card-status-bar"></div>
        <div class="card-content">
          <div class="card-main">
            <div class="exam-info">
              <h3 class="exam-title">{{ item.examTitle }}</h3>
              <div class="exam-meta">
                <span class="meta-item">
                  <el-icon><Calendar /></el-icon>
                  {{ formatDate(item.submitTime) }}
                </span>
                <el-tag 
                  :type="getStatusTagType(item)" 
                  size="small"
                  effect="light"
                  round
                >
                  {{ getStatusText(item) }}
                </el-tag>
              </div>
            </div>
            
            <div class="score-section" v-if="item.status !== 'ABSENT'">
              <template v-if="item.gradingStatus === 'GRADED'">
                <div class="score-circle" :class="getScoreClass(item.score, item.totalScore)">
                  <span class="score-num">{{ item.score }}</span>
                  <span class="score-total" v-if="item.totalScore">/{{ item.totalScore }}</span>
                </div>
                <div class="score-rate" v-if="item.totalScore">
                  <div class="rate-bar">
                    <div 
                      class="rate-fill" 
                      :style="{ width: Math.round((item.score / item.totalScore) * 100) + '%' }"
                      :class="getScoreClass(item.score, item.totalScore)"
                    ></div>
                  </div>
                  <span class="rate-text">{{ Math.round((item.score / item.totalScore) * 100) }}%</span>
                </div>
              </template>
              <template v-else>
                <div class="pending-score">
                  <el-icon class="pending-icon"><Loading /></el-icon>
                  <span>等待批改</span>
                </div>
              </template>
            </div>
            
            <div class="absent-section" v-else>
              <el-icon class="absent-icon"><WarningFilled /></el-icon>
              <span>未参加考试</span>
            </div>
          </div>
          
          <div class="card-actions">
            <el-button
              v-if="item.gradingStatus === 'GRADED' && item.status !== 'ABSENT'"
              type="primary"
              @click="showDetail(item)"
            >
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
            <el-button
              v-else-if="item.status === 'ABSENT'"
              type="info"
              disabled
            >
              缺考
            </el-button>
            <el-button
              v-else
              type="warning"
              plain
              disabled
            >
              <el-icon><Clock /></el-icon>
              待批改
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-else>
      <el-empty description="暂无考试记录">
        <template #image>
          <el-icon class="empty-icon"><Document /></el-icon>
        </template>
      </el-empty>
    </div>

    <!-- 试卷详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title=""
      width="900px"
      top="5vh"
      class="detail-dialog"
    >
      <template #header>
        <div class="dialog-header">
          <div class="dialog-title">
            <el-icon><Document /></el-icon>
            <span>试卷详情</span>
          </div>
        </div>
      </template>
      
      <div class="detail-header">
        <div class="detail-exam-name">{{ currentResult.examTitle }}</div>
        <div class="detail-stats">
          <div class="detail-stat">
            <span class="detail-stat-label">得分</span>
            <span class="detail-stat-value score-highlight">
              {{ currentResult.score }}
              <span v-if="currentResult.totalScore" class="total-score">/ {{ currentResult.totalScore }}</span>
            </span>
          </div>
          <div class="detail-stat">
            <span class="detail-stat-label">得分率</span>
            <span class="detail-stat-value" v-if="currentResult.totalScore">
              {{ Math.round((currentResult.score / currentResult.totalScore) * 100) }}%
            </span>
          </div>
          <div class="detail-stat">
            <span class="detail-stat-label">交卷时间</span>
            <span class="detail-stat-value">{{ currentResult.submitTime }}</span>
          </div>
        </div>
      </div>
      
      <div class="answer-list" v-if="currentAnswerDetails.length">
        <div 
          v-for="(item, index) in currentAnswerDetails" 
          :key="index"
          :class="['answer-item', item.isCorrect ? 'correct' : 'wrong']"
        >
          <div class="question-header">
            <div class="question-left">
              <span class="question-num">{{ index + 1 }}</span>
              <el-tag size="small" type="info" effect="plain">{{ getTypeName(item.questionType) }}</el-tag>
            </div>
            <div class="question-right">
              <span class="question-score">
                <el-icon v-if="item.isCorrect" class="score-icon correct"><CircleCheckFilled /></el-icon>
                <el-icon v-else class="score-icon wrong"><CircleCloseFilled /></el-icon>
                {{ item.earnedScore }} / {{ item.questionScore }} 分
              </span>
            </div>
          </div>
          
          <div class="question-title">{{ item.questionTitle }}</div>
          
          <!-- 选择题选项 -->
          <div v-if="item.options" class="question-options">
            <div 
              v-for="(opt, key) in item.options" 
              :key="key" 
              :class="[
                'option-item',
                { 'user-selected': item.userAnswer && item.userAnswer.includes(key) },
                { 'correct-answer': item.correctAnswer && item.correctAnswer.includes(key) },
                { 'wrong-selected': item.userAnswer && item.userAnswer.includes(key) && !item.correctAnswer.includes(key) }
              ]"
            >
              <span class="option-key">{{ key }}</span>
              <span class="option-text">{{ opt }}</span>
              <el-icon v-if="item.correctAnswer && item.correctAnswer.includes(key)" class="option-icon correct"><Check /></el-icon>
              <el-icon v-if="item.userAnswer && item.userAnswer.includes(key) && !item.correctAnswer.includes(key)" class="option-icon wrong"><Close /></el-icon>
            </div>
          </div>
          
          <!-- 判断题 -->
          <div v-else-if="item.questionType === 3" class="judge-answer">
            <div class="answer-row">
              <span class="answer-label">你的答案</span>
              <span :class="['answer-value', item.isCorrect ? 'correct' : 'wrong']">
                {{ item.userAnswer || '未作答' }}
              </span>
            </div>
            <div class="answer-row">
              <span class="answer-label">正确答案</span>
              <span class="answer-value correct">{{ item.correctAnswer }}</span>
            </div>
          </div>
          
          <!-- 编程题 -->
          <div v-else-if="item.questionType === 4" class="code-answer">
            <div class="code-section">
              <div class="code-label">
                <el-icon><Document /></el-icon>
                你的代码
              </div>
              <pre class="code-block">{{ item.userAnswer || '未作答' }}</pre>
            </div>
            <div class="code-result">
              <div class="result-row">
                <span class="result-label">期望输出</span>
                <code class="result-value">{{ item.sampleOutput }}</code>
              </div>
              <div class="result-row">
                <span class="result-label">实际输出</span>
                <code :class="['result-value', item.isCorrect ? 'correct' : 'wrong']">
                  {{ item.userOutput || '无输出' }}
                </code>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="no-detail">
        <el-empty description="暂无答题详情数据" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Check, Close, Clock, Calendar, View, Document, Loading,
  WarningFilled, CircleCheckFilled, CircleCloseFilled
} from '@element-plus/icons-vue'

const results = ref([])
const detailDialogVisible = ref(false)
const currentResult = ref({})
const currentAnswerDetails = ref([])
const filterStatus = ref('all')

// 计算属性
const gradedCount = computed(() => {
  return results.value.filter(r => r.gradingStatus === 'GRADED' && r.status !== 'ABSENT').length
})

const averageScore = computed(() => {
  const graded = results.value.filter(r => r.gradingStatus === 'GRADED' && r.status !== 'ABSENT')
  if (graded.length === 0) return '-'
  const total = graded.reduce((sum, r) => sum + (r.score || 0), 0)
  return Math.round(total / graded.length)
})

const filteredResults = computed(() => {
  if (filterStatus.value === 'all') return results.value
  if (filterStatus.value === 'graded') return results.value.filter(r => r.gradingStatus === 'GRADED' && r.status !== 'ABSENT')
  if (filterStatus.value === 'pending') return results.value.filter(r => r.gradingStatus !== 'GRADED' && r.status !== 'ABSENT')
  if (filterStatus.value === 'absent') return results.value.filter(r => r.status === 'ABSENT')
  return results.value
})

const loadResults = async () => {
  const userStr = sessionStorage.getItem('exam_user')
  let userId = null
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      userId = user && user.id ? user.id : null
    } catch (e) {
      userId = null
    }
  }
  if (!userId) return

  try {
    const res = await fetch(`http://localhost:8081/api/exam-results?userId=${userId}`)
    if (!res.ok) throw new Error('获取考试记录失败')
    let data = await res.json()
    if (Array.isArray(data)) {
      data.sort((a, b) => {
        const timeA = a.submitTime ? new Date(a.submitTime).getTime() : 0
        const timeB = b.submitTime ? new Date(b.submitTime).getTime() : 0
        return timeB - timeA
      })
    }
    results.value = data
  } catch (e) {
    ElMessage.error(e.message || '获取考试记录失败')
  }
}

const showDetail = (row) => {
  currentResult.value = row
  currentAnswerDetails.value = []
  if (row && row.answerDetail) {
    try {
      const details = JSON.parse(row.answerDetail)
      if (Array.isArray(details)) currentAnswerDetails.value = details
    } catch (e) {
      console.error('解析答题详情失败', e)
    }
  }
  detailDialogVisible.value = true
}

const getTypeName = (type) => {
  const types = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '编程题' }
  return types[type] || '未知'
}

const getCardClass = (item) => {
  if (item.status === 'ABSENT') return 'absent'
  if (item.gradingStatus !== 'GRADED') return 'pending'
  const rate = item.totalScore ? item.score / item.totalScore : 0
  if (rate >= 0.9) return 'excellent'
  if (rate >= 0.8) return 'good'
  if (rate >= 0.6) return 'pass'
  return 'fail'
}

const getScoreClass = (score, totalScore) => {
  if (!totalScore) return ''
  const rate = score / totalScore
  if (rate >= 0.9) return 'excellent'
  if (rate >= 0.8) return 'good'
  if (rate >= 0.6) return 'pass'
  return 'fail'
}

const getStatusTagType = (item) => {
  if (item.status === 'ABSENT') return 'danger'
  if (item.gradingStatus === 'GRADED') return 'success'
  return 'warning'
}

const getStatusText = (item) => {
  if (item.status === 'ABSENT') return '缺考'
  if (item.gradingStatus === 'GRADED') return '已批改'
  return '待批改'
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  loadResults()
})
</script>

<style scoped>
.exam-history-container {
  min-height: calc(100vh - 70px);
  padding-bottom: 40px;
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 28px 32px;
  border-radius: 20px;
  margin-bottom: 24px;
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.3);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  font-size: 36px;
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
  padding: 12px;
  border-radius: 14px;
}

.header-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.header-title {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
}

.header-subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
}

.header-stats {
  display: flex;
  align-items: center;
  gap: 24px;
  background: rgba(255, 255, 255, 0.15);
  padding: 16px 28px;
  border-radius: 14px;
  backdrop-filter: blur(10px);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #fff;
}

.stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.stat-divider {
  width: 1px;
  height: 36px;
  background: rgba(255, 255, 255, 0.3);
}

/* 筛选栏 */
.filter-bar {
  margin-bottom: 20px;
}

.filter-bar :deep(.el-radio-button__inner) {
  border-radius: 20px !important;
  padding: 8px 20px;
}

.filter-bar :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 20px !important;
}

.filter-bar :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 20px !important;
}

/* 考试记录列表 */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.history-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: all 0.3s ease;
  display: flex;
}

.history-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

.card-status-bar {
  width: 5px;
  flex-shrink: 0;
}

.history-card.excellent .card-status-bar { background: linear-gradient(180deg, #67c23a, #85ce61); }
.history-card.good .card-status-bar { background: linear-gradient(180deg, #409eff, #66b1ff); }
.history-card.pass .card-status-bar { background: linear-gradient(180deg, #e6a23c, #ebb563); }
.history-card.fail .card-status-bar { background: linear-gradient(180deg, #f56c6c, #f78989); }
.history-card.pending .card-status-bar { background: linear-gradient(180deg, #909399, #a6a9ad); }
.history-card.absent .card-status-bar { background: linear-gradient(180deg, #909399, #c0c4cc); }

.card-content {
  flex: 1;
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-main {
  display: flex;
  align-items: center;
  gap: 40px;
  flex: 1;
}

.exam-info {
  flex: 1;
}

.exam-title {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.exam-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}

/* 分数区域 */
.score-section {
  display: flex;
  align-items: center;
  gap: 20px;
  min-width: 200px;
}

.score-circle {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border: 3px solid;
}

.score-circle.excellent { border-color: #67c23a; background: #f0f9eb; }
.score-circle.good { border-color: #409eff; background: #ecf5ff; }
.score-circle.pass { border-color: #e6a23c; background: #fdf6ec; }
.score-circle.fail { border-color: #f56c6c; background: #fef0f0; }

.score-num {
  font-size: 22px;
  font-weight: 700;
  line-height: 1;
}

.score-circle.excellent .score-num { color: #67c23a; }
.score-circle.good .score-num { color: #409eff; }
.score-circle.pass .score-num { color: #e6a23c; }
.score-circle.fail .score-num { color: #f56c6c; }

.score-total {
  font-size: 11px;
  color: #909399;
}

.score-rate {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 80px;
}

.rate-bar {
  height: 6px;
  background: #e4e7ed;
  border-radius: 3px;
  overflow: hidden;
}

.rate-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.5s ease;
}

.rate-fill.excellent { background: linear-gradient(90deg, #67c23a, #85ce61); }
.rate-fill.good { background: linear-gradient(90deg, #409eff, #66b1ff); }
.rate-fill.pass { background: linear-gradient(90deg, #e6a23c, #ebb563); }
.rate-fill.fail { background: linear-gradient(90deg, #f56c6c, #f78989); }

.rate-text {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
}

.pending-score, .absent-section {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
  min-width: 120px;
}

.pending-icon {
  animation: spin 1.5s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.absent-icon {
  font-size: 20px;
  color: #f56c6c;
}

.absent-section {
  color: #f56c6c;
}

.card-actions {
  flex-shrink: 0;
}

/* 空状态 */
.empty-state {
  padding: 60px 0;
}

.empty-icon {
  font-size: 80px;
  color: #dcdfe6;
}

/* 详情对话框 */
.detail-dialog :deep(.el-dialog__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  margin: 0;
}

.dialog-header {
  display: flex;
  align-items: center;
}

.dialog-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.detail-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 20px;
  color: #fff;
}

.detail-exam-name {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 16px;
}

.detail-stats {
  display: flex;
  gap: 40px;
}

.detail-stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.detail-stat-value {
  font-size: 15px;
  font-weight: 500;
}

.detail-stat-value.score-highlight {
  font-size: 28px;
  font-weight: 700;
  color: #ffd700;
}

.total-score {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
}

.answer-list {
  max-height: 55vh;
  overflow-y: auto;
  padding-right: 8px;
}

.answer-item {
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  padding: 18px;
  margin-bottom: 14px;
  transition: all 0.3s;
}

.answer-item.correct {
  border-left: 4px solid #67c23a;
  background: linear-gradient(90deg, #f0f9eb 0%, #fff 100%);
}

.answer-item.wrong {
  border-left: 4px solid #f56c6c;
  background: linear-gradient(90deg, #fef0f0 0%, #fff 100%);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.question-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.question-num {
  width: 28px;
  height: 28px;
  background: #667eea;
  color: #fff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
}

.question-score {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
}

.score-icon {
  font-size: 18px;
}

.score-icon.correct { color: #67c23a; }
.score-icon.wrong { color: #f56c6c; }

.question-title {
  font-size: 15px;
  color: #303133;
  margin-bottom: 14px;
  line-height: 1.6;
}

.question-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 8px;
  background: #f5f7fa;
  transition: all 0.2s;
}

.option-item.correct-answer {
  background: #f0f9eb;
  border: 1px solid #67c23a;
}

.option-item.wrong-selected {
  background: #fef0f0;
  border: 1px solid #f56c6c;
}

.option-item.user-selected:not(.wrong-selected) {
  background: #ecf5ff;
  border: 1px solid #409eff;
}

.option-key {
  width: 24px;
  height: 24px;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: #606266;
  flex-shrink: 0;
}

.option-text {
  flex: 1;
  font-size: 14px;
  color: #303133;
}

.option-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.option-icon.correct { color: #67c23a; }
.option-icon.wrong { color: #f56c6c; }

.judge-answer {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 10px;
}

.answer-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.answer-label {
  font-size: 13px;
  color: #909399;
  min-width: 70px;
}

.answer-value {
  font-size: 14px;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 6px;
}

.answer-value.correct {
  color: #67c23a;
  background: #f0f9eb;
}

.answer-value.wrong {
  color: #f56c6c;
  background: #fef0f0;
}

.code-answer {
  margin-top: 12px;
}

.code-section {
  margin-bottom: 14px;
}

.code-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}

.code-block {
  max-height: 200px;
  overflow: auto;
  font-family: 'Fira Code', Consolas, 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 14px 16px;
  border-radius: 8px;
  white-space: pre-wrap;
}

.code-result {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.result-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.result-label {
  font-size: 13px;
  color: #909399;
  min-width: 70px;
}

.result-value {
  font-size: 13px;
  padding: 4px 10px;
  background: #f5f7fa;
  border-radius: 4px;
  font-family: 'Fira Code', Consolas, monospace;
}

.result-value.correct { color: #67c23a; background: #f0f9eb; }
.result-value.wrong { color: #f56c6c; background: #fef0f0; }

.no-detail {
  padding: 40px 0;
}
</style>
