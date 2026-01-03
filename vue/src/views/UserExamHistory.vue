<template>
  <div class="exam-history-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-icon class="header-icon"><Clock /></el-icon>
        <span class="header-title">考试记录</span>
        <span class="header-subtitle">共 {{ results.length }} 条记录</span>
      </div>
    </div>

    <el-card class="history-card">
      <el-table :data="results" style="width: 100%">
        <el-table-column prop="examTitle" label="考试名称" />
        <el-table-column label="得分" width="120">
          <template #default="scope">
            <span v-if="scope.row.gradingStatus === 'GRADED'" class="score-text">
              {{ scope.row.score }}<span v-if="scope.row.totalScore">/{{ scope.row.totalScore }}</span>
            </span>
            <span v-else class="score-pending">待批改</span>
          </template>
        </el-table-column>
        <el-table-column label="批改状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.gradingStatus === 'GRADED' ? 'success' : 'warning'" size="small">
              {{ scope.row.gradingStatus === 'GRADED' ? '已批改' : '待批改' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="交卷时间" width="170" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button
              v-if="scope.row.gradingStatus === 'GRADED'"
              type="primary"
              link
              size="small"
              @click="showDetail(scope.row)"
            >
              查看详情
            </el-button>
            <span v-else class="waiting-tip">等待教师批改</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 试卷详情对话框 -->
      <el-dialog
        v-model="detailDialogVisible"
        title="试卷详情"
        width="900px"
        top="5vh"
      >
        <div class="detail-header">
          <div class="detail-info">
            <span><strong>考试名称：</strong>{{ currentResult.examTitle }}</span>
            <span><strong>得分：</strong>
              <span class="score-highlight">{{ currentResult.score }}</span>
              <span v-if="currentResult.totalScore"> / {{ currentResult.totalScore }}</span>
            </span>
            <span><strong>交卷时间：</strong>{{ currentResult.submitTime }}</span>
          </div>
        </div>
        
        <div class="answer-list" v-if="currentAnswerDetails.length">
          <div 
            v-for="(item, index) in currentAnswerDetails" 
            :key="index"
            :class="['answer-item', item.isCorrect ? 'correct' : 'wrong']"
          >
            <div class="question-header">
              <span class="question-num">第 {{ index + 1 }} 题</span>
              <el-tag :type="item.isCorrect ? 'success' : 'danger'" size="small">
                {{ item.isCorrect ? '正确' : '错误' }}
              </el-tag>
              <span class="question-score">
                得分: {{ item.earnedScore }} / {{ item.questionScore }}
              </span>
              <el-tag size="small" type="info">{{ getTypeName(item.questionType) }}</el-tag>
            </div>
            
            <div class="question-title">{{ item.questionTitle }}</div>
            
            <!-- 选择题选项 -->
            <div v-if="item.options" class="question-options">
              <div v-for="(opt, key) in item.options" :key="key" class="option-item">
                <span 
                  :class="{
                    'user-selected': item.userAnswer && item.userAnswer.includes(key),
                    'correct-answer': item.correctAnswer && item.correctAnswer.includes(key)
                  }"
                >
                  {{ key }}. {{ opt }}
                </span>
                <el-icon v-if="item.correctAnswer && item.correctAnswer.includes(key)" class="correct-icon"><Check /></el-icon>
                <el-icon v-if="item.userAnswer && item.userAnswer.includes(key) && !item.correctAnswer.includes(key)" class="wrong-icon"><Close /></el-icon>
              </div>
            </div>
            
            <!-- 判断题 -->
            <div v-else-if="item.questionType === 3" class="judge-answer">
              <div>
                <span class="label">你的答案：</span>
                <span :class="item.isCorrect ? 'text-success' : 'text-danger'">{{ item.userAnswer || '未作答' }}</span>
              </div>
              <div>
                <span class="label">正确答案：</span>
                <span class="text-success">{{ item.correctAnswer }}</span>
              </div>
            </div>
            
            <!-- 编程题 -->
            <div v-else-if="item.questionType === 4" class="code-answer">
              <div class="code-section">
                <div class="code-label">你的代码：</div>
                <pre class="code-block">{{ item.userAnswer || '未作答' }}</pre>
              </div>
              <div class="code-result">
                <div><span class="label">期望输出：</span>{{ item.sampleOutput }}</div>
                <div><span class="label">实际输出：</span>
                  <span :class="item.isCorrect ? 'text-success' : 'text-danger'">{{ item.userOutput || '无' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="no-detail">
          <el-empty description="暂无答题详情数据" />
        </div>
      </el-dialog>

      <!-- 编程代码对话框 -->
      <el-dialog
        v-model="codeDialogVisible"
        title="编程题代码"
        width="700px"
      >
        <div v-if="currentCodes.length">
          <div
            v-for="(code, index) in currentCodes"
            :key="index"
            class="code-item"
          >
            <div class="code-item-title">题目 {{ index + 1 }}</div>
            <pre class="code-view">{{ code }}</pre>
          </div>
        </div>
        <div v-else style="color: #999;">本次考试未保存编程题代码。</div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Close, Clock } from '@element-plus/icons-vue'

const results = ref([])
const codeDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentCodes = ref([])
const currentResult = ref({})
const currentAnswerDetails = ref([])

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
  if (!userId) {
    return
  }

  try {
    const res = await fetch(`http://localhost:8081/api/exam-results?userId=${userId}`)
    if (!res.ok) {
      throw new Error('获取考试记录失败')
    }
    results.value = await res.json()
  } catch (e) {
    ElMessage.error(e.message || '获取考试记录失败')
  }
}

// 显示试卷详情
const showDetail = (row) => {
  currentResult.value = row
  currentAnswerDetails.value = []
  
  if (row && row.answerDetail) {
    try {
      const details = JSON.parse(row.answerDetail)
      if (Array.isArray(details)) {
        currentAnswerDetails.value = details
      }
    } catch (e) {
      console.error('解析答题详情失败', e)
    }
  }
  
  detailDialogVisible.value = true
}

// 获取题型名称
const getTypeName = (type) => {
  const types = {
    1: '单选题',
    2: '多选题',
    3: '判断题',
    4: '编程题'
  }
  return types[type] || '未知'
}

const showCode = (row) => {
  currentCodes.value = []
  if (row && row.codeAnswer) {
    try {
      const arr = JSON.parse(row.codeAnswer)
      if (Array.isArray(arr)) {
        // 只显示非空代码，避免很多空白行
        currentCodes.value = arr.map(c => (c || '').trim()).filter(c => c)
      }
    } catch (e) {
      // 解析失败时，直接当作一段文本显示
      currentCodes.value = [String(row.codeAnswer)]
    }
  }
  codeDialogVisible.value = true
}

const statusType = (status) => {
  if (status === 'FINISHED') return 'success'
  if (status === 'FAILED') return 'danger'
  return 'info'
}

onMounted(() => {
  loadResults()
})
</script>

<style scoped>
.exam-history-container {
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

.history-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.history-card :deep(.el-card__body) {
  padding: 20px;
}

.score-text {
  font-weight: 600;
  color: #409eff;
}

.score-pending {
  color: #e6a23c;
  font-weight: 500;
}

.waiting-tip {
  color: #909399;
  font-size: 13px;
}

.detail-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  color: #fff;
}

.detail-info {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
}

.detail-info span {
  font-size: 14px;
}

.score-highlight {
  font-size: 24px;
  font-weight: bold;
  color: #ffd700;
}

.answer-list {
  max-height: 60vh;
  overflow-y: auto;
}

.answer-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  transition: all 0.3s;
}

.answer-item.correct {
  border-left: 4px solid #67c23a;
  background: #f0f9eb;
}

.answer-item.wrong {
  border-left: 4px solid #f56c6c;
  background: #fef0f0;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.question-num {
  font-weight: 600;
  color: #303133;
}

.question-score {
  margin-left: auto;
  font-size: 13px;
  color: #606266;
}

.question-title {
  font-size: 15px;
  color: #303133;
  margin-bottom: 12px;
  line-height: 1.6;
}

.question-options {
  margin-left: 10px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  margin-bottom: 6px;
  border-radius: 4px;
}

.option-item .user-selected {
  background: #e6f7ff;
  padding: 2px 8px;
  border-radius: 4px;
}

.option-item .correct-answer {
  color: #67c23a;
  font-weight: 500;
}

.correct-icon {
  color: #67c23a;
}

.wrong-icon {
  color: #f56c6c;
}

.judge-answer, .code-result {
  margin-top: 10px;
  font-size: 14px;
}

.judge-answer > div, .code-result > div {
  margin-bottom: 6px;
}

.label {
  color: #909399;
  margin-right: 8px;
}

.text-success {
  color: #67c23a;
  font-weight: 500;
}

.text-danger {
  color: #f56c6c;
  font-weight: 500;
}

.code-section {
  margin-top: 10px;
}

.code-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.code-block {
  max-height: 200px;
  overflow: auto;
  font-family: Consolas, 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
  background-color: #1e1e1e;
  color: #e0e0e0;
  padding: 10px 12px;
  border-radius: 4px;
  white-space: pre-wrap;
}

.no-detail {
  padding: 40px 0;
}

.code-item {
  margin-bottom: 16px;
}

.code-item-title {
  font-weight: 600;
  margin-bottom: 6px;
}

.code-view {
  max-height: 260px;
  overflow: auto;
  font-family: Consolas, 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  background-color: #1e1e1e;
  color: #e0e0e0;
  padding: 10px 12px;
  border-radius: 4px;
  border: 1px solid #333;
  white-space: pre-wrap;
}
</style>
