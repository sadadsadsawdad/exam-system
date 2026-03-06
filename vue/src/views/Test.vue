<template>
  <div class="test-page">
    <!-- 顶部固定栏 -->
    <div class="exam-topbar">
      <div class="topbar-left">
        <div class="exam-logo">
          <el-icon><EditPen /></el-icon>
        </div>
        <div class="exam-title-info">
          <h1>{{ examTitle }}</h1>
          <div class="exam-meta">
            <span><el-icon><Clock /></el-icon> {{ examDuration }}分钟</span>
            <span><el-icon><Trophy /></el-icon> {{ examTotalScore }}分</span>
          </div>
        </div>
      </div>
      <div class="topbar-center">
        <div class="timer-box" :class="{ 'timer-warning': remainingSeconds < 300, 'timer-danger': remainingSeconds < 60 }">
          <el-icon class="timer-icon"><AlarmClock /></el-icon>
          <span class="timer-text">{{ formatTime(remainingSeconds) }}</span>
        </div>
      </div>
      <div class="topbar-right">
        <div v-if="switchCount > 0" class="switch-warning-badge" :class="{ 'danger': switchCount >= 7 }">
          <el-icon><Warning /></el-icon>
          切屏 {{ switchCount }}/10
        </div>
        <el-button type="primary" @click="submitExam" class="submit-btn">
          <el-icon><Upload /></el-icon>
          交卷
        </el-button>
      </div>
    </div>

    <!-- 主体内容区 -->
    <div class="exam-main">
      <!-- 左侧导航面板 -->
      <div class="nav-sidebar">
        <div class="nav-card">
          <div class="nav-header">
            <span class="nav-title">答题卡</span>
            <span class="nav-progress">{{ answeredCount }}/{{ allQuestions.length }}</span>
          </div>
          
          <!-- 进度环 -->
          <div class="progress-ring-container">
            <el-progress type="circle" :percentage="progressPercent" :width="100" :stroke-width="8">
              <template #default>
                <span class="progress-inner">{{ progressPercent }}%</span>
              </template>
            </el-progress>
          </div>

          <!-- 题目网格 -->
          <div class="question-grid">
            <div
              v-for="(q, index) in allQuestions"
              :key="'nav-' + index"
              class="grid-item"
              :class="{
                'answered': isQuestionAnswered(index),
                'current': currentQuestionIndex === index,
                'type-single': q.type === 1,
                'type-multi': q.type === 2,
                'type-judge': q.type === 3,
                'type-code': q.type === 4
              }"
              @click="scrollToQuestion(index)"
            >
              {{ index + 1 }}
            </div>
          </div>

          <!-- 图例 -->
          <div class="nav-legend">
            <div class="legend-row">
              <span class="legend-dot answered"></span>
              <span>已答</span>
            </div>
            <div class="legend-row">
              <span class="legend-dot"></span>
              <span>未答</span>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="nav-actions">
          <el-button @click="saveAnswersDraft" class="action-btn save-btn">
            <el-icon><DocumentCopy /></el-icon>
            暂存答案
          </el-button>
        </div>
      </div>

      <!-- 右侧答题区 -->
      <div class="question-area">
        <div class="questions-container">
          <div
            v-for="(q, index) in allQuestions"
            :key="q.id || 'q-' + index"
            :ref="el => questionRefs[index] = el"
            class="question-card"
            :class="{ 'unanswered': !isQuestionAnswered(index) }"
          >
            <!-- 题目头部 -->
            <div class="question-header">
              <div class="question-number">
                <span class="num">{{ index + 1 }}</span>
              </div>
              <div class="question-type-badge" :class="'type-' + q.type">
                {{ getTypeName(q.type) }}
              </div>
              <div class="question-score">
                <el-icon><Medal /></el-icon>
                {{ q.score || 0 }}分
              </div>
              <div v-if="isQuestionAnswered(index)" class="answered-badge">
                <el-icon><CircleCheck /></el-icon>
                已答
              </div>
            </div>

            <!-- 题目内容 -->
            <div class="question-content">
              <div class="question-text">{{ q.title || '试题' }}</div>
              
              <!-- 单选题 -->
              <div v-if="q.type === 1" class="options-list">
                <div
                  v-for="opt in ['A', 'B', 'C', 'D']"
                  :key="opt"
                  class="option-item"
                  :class="{ 'selected': singleAnswers[index] === opt }"
                  @click="singleAnswers[index] = opt"
                >
                  <span class="option-letter">{{ opt }}</span>
                  <span class="option-text">{{ q['option' + opt] || '选项' + opt }}</span>
                </div>
              </div>

              <!-- 多选题 -->
              <div v-else-if="q.type === 2" class="options-list multi">
                <div class="multi-tip">（可多选）</div>
                <div
                  v-for="opt in ['A', 'B', 'C', 'D']"
                  :key="opt"
                  class="option-item"
                  :class="{ 'selected': multiAnswers[index]?.includes(opt) }"
                  @click="toggleMultiOption(index, opt)"
                >
                  <span class="option-checkbox">
                    <el-icon v-if="multiAnswers[index]?.includes(opt)"><Check /></el-icon>
                  </span>
                  <span class="option-letter">{{ opt }}</span>
                  <span class="option-text">{{ q['option' + opt] || '选项' + opt }}</span>
                </div>
              </div>

              <!-- 判断题 -->
              <div v-else-if="q.type === 3" class="judge-options">
                <div
                  class="judge-item"
                  :class="{ 'selected': judgeAnswers[index] === '1' }"
                  @click="judgeAnswers[index] = '1'"
                >
                  <el-icon class="judge-icon correct"><CircleCheck /></el-icon>
                  <span>正确</span>
                </div>
                <div
                  class="judge-item"
                  :class="{ 'selected': judgeAnswers[index] === '0' }"
                  @click="judgeAnswers[index] = '0'"
                >
                  <el-icon class="judge-icon wrong"><CircleClose /></el-icon>
                  <span>错误</span>
                </div>
              </div>

              <!-- 编程题 -->
              <div v-else-if="q.type === 4" class="code-editor-section">
                <div class="code-header">
                  <span class="code-label">代码编辑器</span>
                  <el-button
                    type="success"
                    size="small"
                    :loading="codeRunning[index]"
                    @click="runCode(index)"
                  >
                    <el-icon><VideoPlay /></el-icon>
                    运行代码
                  </el-button>
                </div>
                <el-input
                  type="textarea"
                  v-model="codeAnswers[index]"
                  :rows="12"
                  placeholder="// 请在此编写C语言代码"
                  class="code-textarea"
                />
                <div class="code-input-section">
                  <span class="input-label">程序输入（可选）</span>
                  <el-input
                    type="textarea"
                    v-model="codeInputs[index]"
                    :rows="2"
                    placeholder="程序运行时的标准输入"
                  />
                </div>
                <div v-if="codeOutputs[index]" class="code-output-section">
                  <span class="output-label">运行结果</span>
                  <pre class="code-output" :class="{ 'error': isCodeError(codeOutputs[index]) }">{{ codeOutputs[index] }}</pre>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 切屏警告弹窗 -->
    <el-dialog
      v-model="switchWarningVisible"
      width="420px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      class="warning-dialog"
    >
      <div class="warning-modal">
        <div class="warning-icon-wrap">
          <el-icon><WarningFilled /></el-icon>
        </div>
        <h3>检测到切屏行为</h3>
        <p class="warning-desc">您已离开考试页面 <strong>{{ switchCount }}</strong> 次</p>
        <p class="warning-hint">超过10次将自动交卷，请专心答题</p>
        <el-button type="primary" size="large" @click="switchWarningVisible = false">
          继续考试
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  WarningFilled, EditPen, Clock, Trophy, AlarmClock, Warning, Upload,
  DocumentCopy, Medal, CircleCheck, CircleClose, VideoPlay, Check
} from '@element-plus/icons-vue'
import { examState } from '@/router'

// 切屏检测相关
const switchCount = ref(0)
const switchWarningVisible = ref(false)
const MAX_SWITCH_COUNT = 10

// 各题型答案数组
const singleAnswers = ref([])
const multiAnswers = ref([])
const judgeAnswers = ref([])

// 编程题代码及输入输出
const codeAnswers = ref([])
const codeInputs = ref([])
const codeOutputs = ref([])
const codeRunning = ref([])

// 所有题目列表
const allQuestions = ref([])

// 题目导航相关
const questionRefs = ref([])
const currentQuestionIndex = ref(0)

// 获取题型名称
const getTypeName = (type) => {
  const names = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '编程题' }
  return names[type] || '未知'
}

// 判断代码输出是否为错误
const isCodeError = (output) => {
  if (!output) return false
  return output.startsWith('[编译失败]') || output.startsWith('[运行超时]') || output.startsWith('运行失败:')
}

// 多选题切换选项
const toggleMultiOption = (index, opt) => {
  if (!Array.isArray(multiAnswers.value[index])) {
    multiAnswers.value[index] = []
  }
  const arr = multiAnswers.value[index]
  const idx = arr.indexOf(opt)
  if (idx > -1) {
    arr.splice(idx, 1)
  } else {
    arr.push(opt)
    arr.sort()
  }
}

// 判断题目是否已作答
const isQuestionAnswered = (index) => {
  const q = allQuestions.value[index]
  if (!q) return false
  if (q.type === 1) return singleAnswers.value[index] && singleAnswers.value[index].trim() !== ''
  if (q.type === 2) return Array.isArray(multiAnswers.value[index]) && multiAnswers.value[index].length > 0
  if (q.type === 3) return judgeAnswers.value[index] !== '' && judgeAnswers.value[index] !== undefined
  if (q.type === 4) return codeAnswers.value[index] && codeAnswers.value[index].trim() !== ''
  return false
}

// 已答题数量
const answeredCount = computed(() => {
  let count = 0
  for (let i = 0; i < allQuestions.value.length; i++) {
    if (isQuestionAnswered(i)) count++
  }
  return count
})

// 答题进度百分比
const progressPercent = computed(() => {
  if (allQuestions.value.length === 0) return 0
  return Math.round((answeredCount.value / allQuestions.value.length) * 100)
})

// 获取未答题目列表
const getUnansweredQuestions = () => {
  const unanswered = []
  for (let i = 0; i < allQuestions.value.length; i++) {
    if (!isQuestionAnswered(i)) unanswered.push(i + 1)
  }
  return unanswered
}

// 滚动到指定题目
const scrollToQuestion = (index) => {
  currentQuestionIndex.value = index
  const el = questionRefs.value[index]
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

// 答案暂存 - key 包含 examId 和 userId，确保不同用户的题目独立
const getStorageKey = () => {
  const userStr = sessionStorage.getItem('exam_user')
  let userId = 'anonymous'
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      userId = user.id || 'anonymous'
    } catch (e) {}
  }
  return `exam_draft_${examId.value}_${userId}`
}

const saveAnswersToStorage = () => {
  if (!examId.value) return
  const draft = {
    questions: allQuestions.value, // 保存题目，防止刷新后随机题目变化
    singleAnswers: singleAnswers.value,
    multiAnswers: multiAnswers.value,
    judgeAnswers: judgeAnswers.value,
    codeAnswers: codeAnswers.value,
    codeInputs: codeInputs.value,
    remainingSeconds: remainingSeconds.value,
    examDuration: Number(examDuration.value) || 120, // 保存考试时长
    savedAt: new Date().toISOString()
  }
  localStorage.setItem(getStorageKey(), JSON.stringify(draft))
  console.log('[答案保存] 已保存，题目:', allQuestions.value.length, '道，剩余时间:', remainingSeconds.value, '秒')
}

const loadAnswersFromStorage = () => {
  console.log('[答案恢复] 开始恢复，examId:', examId.value)
  if (!examId.value) {
    console.log('[答案恢复] examId为空，跳过恢复')
    return { hasAnswers: false, hasQuestions: false }
  }
  const storageKey = getStorageKey()
  const draftStr = localStorage.getItem(storageKey)
  console.log('[答案恢复] storageKey:', storageKey, '数据存在:', !!draftStr)
  if (!draftStr) return { hasAnswers: false, hasQuestions: false }
  try {
    const draft = JSON.parse(draftStr)
    const savedAt = new Date(draft.savedAt).getTime()
    const now = new Date().getTime()
    const elapsedMinutes = Math.floor((now - savedAt) / 60000)
    console.log('[答案恢复] 保存时间:', draft.savedAt, '已过去:', elapsedMinutes, '分钟')
    
    // 使用保存的考试时长，而不是当前的 examDuration（可能还没初始化）
    const savedDuration = draft.examDuration || Number(examDuration.value) || 120
    const maxDuration = savedDuration * 60 * 1000
    console.log('[答案恢复] 考试时长:', savedDuration, '分钟，最大有效期:', maxDuration / 60000, '分钟')
    
    if (now - savedAt > maxDuration) {
      console.log('[答案恢复] 数据已过期，清除')
      localStorage.removeItem(storageKey)
      return { hasAnswers: false, hasQuestions: false }
    }
    
    // 恢复题目（如果存在）
    let hasQuestions = false
    if (draft.questions && Array.isArray(draft.questions) && draft.questions.length > 0) {
      allQuestions.value = draft.questions
      hasQuestions = true
      console.log('[答案恢复] 恢复题目:', draft.questions.length, '道')
    }
    
    // 恢复答案
    if (draft.singleAnswers) {
      singleAnswers.value = draft.singleAnswers
      console.log('[答案恢复] 恢复单选答案:', draft.singleAnswers.filter(a => a).length, '个')
    }
    if (draft.multiAnswers) {
      multiAnswers.value = draft.multiAnswers
      console.log('[答案恢复] 恢复多选答案:', draft.multiAnswers.filter(a => a && a.length).length, '个')
    }
    if (draft.judgeAnswers) {
      judgeAnswers.value = draft.judgeAnswers
      console.log('[答案恢复] 恢复判断答案:', draft.judgeAnswers.filter(a => a !== '' && a !== undefined).length, '个')
    }
    if (draft.codeAnswers) {
      codeAnswers.value = draft.codeAnswers
      console.log('[答案恢复] 恢复代码答案:', draft.codeAnswers.filter(a => a).length, '个')
    }
    if (draft.codeInputs) codeInputs.value = draft.codeInputs
    
    // 恢复剩余时间
    if (draft.remainingSeconds && draft.remainingSeconds > 0) {
      const elapsed = Math.floor((now - savedAt) / 1000)
      const actualRemaining = draft.remainingSeconds - elapsed
      console.log('[答案恢复] 保存时剩余:', draft.remainingSeconds, '秒，已过去:', elapsed, '秒，实际剩余:', actualRemaining, '秒')
      if (actualRemaining > 0) {
        remainingSeconds.value = actualRemaining
      }
    }
    return { hasAnswers: true, hasQuestions }
  } catch (e) {
    console.error('[答案恢复] 解析失败:', e)
    localStorage.removeItem(getStorageKey())
    return { hasAnswers: false, hasQuestions: false }
  }
}

const clearStoredAnswers = () => {
  if (!examId.value) return
  localStorage.removeItem(getStorageKey())
}

const saveAnswersDraft = () => {
  saveAnswersToStorage()
  ElMessage.success('答案已暂存')
}

const router = useRouter()
const route = useRoute()

const examId = ref(route.query.examId || null)
const examTitle = ref(route.query.title || '考试')
const examDuration = ref(route.query.duration || 120)
const examTotalScore = ref(route.query.totalScore || 100)

const remainingSeconds = ref(0)
let countdownTimer = null

const formatTime = (seconds) => {
  if (seconds <= 0) return '00:00:00'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

const startCountdown = () => {
  if (remainingSeconds.value <= 0) {
    remainingSeconds.value = Number(examDuration.value) * 60
  }
  countdownTimer = setInterval(() => {
    if (remainingSeconds.value > 0) {
      remainingSeconds.value--
      if (remainingSeconds.value % 30 === 0) saveAnswersToStorage()
      if (remainingSeconds.value === 300) ElMessage.warning('注意：考试还剩5分钟！')
      if (remainingSeconds.value === 60) ElMessage.warning('注意：考试还剩1分钟！')
    } else {
      clearInterval(countdownTimer)
      autoSubmitExam()
    }
  }, 1000)
}

const autoSubmitExam = async () => {
  ElMessage.warning('考试时间到，系统自动交卷！')
  await submitExam(true)
}

const checkAlreadyParticipated = async () => {
  const userStr = sessionStorage.getItem('exam_user')
  if (!userStr) return false
  let user = null
  try { user = JSON.parse(userStr) } catch (e) { return false }
  if (!user || !user.id || !examId.value) return false
  try {
    const res = await fetch(`http://localhost:8081/api/exam-results?userId=${user.id}`)
    if (!res.ok) return false
    const list = await res.json()
    if (!Array.isArray(list)) return false
    const targetId = Number(examId.value)
    return list.some(r => r && Number(r.examId) === targetId)
  } catch (e) { return false }
}

const loadQuestions = async () => {
  console.log('[加载题目] 开始加载，examId:', examId.value)
  // 先尝试从 localStorage 恢复题目和答案
  const { hasAnswers, hasQuestions } = loadAnswersFromStorage()
  console.log('[加载题目] 恢复结果 - hasAnswers:', hasAnswers, 'hasQuestions:', hasQuestions)
  
  // 如果已经从 localStorage 恢复了题目，则不需要重新获取
  if (hasQuestions) {
    // 初始化答案数组（如果没有从 storage 恢复）
    const len = allQuestions.value.length
    if (!singleAnswers.value.length) singleAnswers.value = new Array(len).fill('')
    if (!multiAnswers.value.length) multiAnswers.value = Array.from({ length: len }, () => [])
    if (!judgeAnswers.value.length) judgeAnswers.value = new Array(len).fill('')
    if (!codeAnswers.value.length) codeAnswers.value = new Array(len).fill('')
    if (!codeInputs.value.length) codeInputs.value = new Array(len).fill('')
    codeOutputs.value = new Array(len).fill('')
    codeRunning.value = new Array(len).fill(false)
    console.log('[加载题目] 从localStorage恢复成功，题目数:', len)
    ElMessage.success('已恢复上次的答题进度')
    return
  }
  
  // 没有保存的题目，从后端获取新题目
  console.log('[加载题目] 没有保存的题目，从后端获取')
  try {
    const res = await fetch(`http://localhost:8081/api/exams/${examId.value}/questions`)
    if (res.ok) {
      const data = await res.json()
      if (data.error) { ElMessage.error(data.error); return }
      let list = data.questions || []
      if (Array.isArray(list)) {
        list.sort((a, b) => (a?.type || 0) - (b?.type || 0))
        allQuestions.value = list
        singleAnswers.value = new Array(list.length).fill('')
        multiAnswers.value = Array.from({ length: list.length }, () => [])
        judgeAnswers.value = new Array(list.length).fill('')
        codeAnswers.value = new Array(list.length).fill('')
        codeInputs.value = new Array(list.length).fill('')
        codeOutputs.value = new Array(list.length).fill('')
        codeRunning.value = new Array(list.length).fill(false)
        console.log('[加载题目] 从后端获取成功，题目数:', list.length)
        // 立即保存题目到 localStorage，确保刷新后题目不变
        saveAnswersToStorage()
      }
    }
  } catch (e) { 
    console.error('[加载题目] 加载失败:', e)
    ElMessage.error('加载题目失败') 
  }
}

const hasSubmitted = ref(false)

const handleBeforeUnload = (e) => {
  if (!hasSubmitted.value) {
    e.preventDefault()
    e.returnValue = '考试进行中，确定要离开吗？'
    return e.returnValue
  }
}

const handleVisibilityChange = () => {
  if (hasSubmitted.value) return
  if (document.hidden) {
    switchCount.value++
    if (switchCount.value >= MAX_SWITCH_COUNT) {
      ElMessage.error('切屏次数超过限制，系统自动交卷！')
      submitExam(true)
    }
  } else {
    if (switchCount.value > 0 && switchCount.value < MAX_SWITCH_COUNT) {
      switchWarningVisible.value = true
    }
  }
}

onMounted(async () => {
  const participated = await checkAlreadyParticipated()
  if (participated) {
    ElMessage.warning('您已参加过本场考试')
    router.push('/top/history')
    return
  }
  examState.isInExam = true
  examState.examId = examId.value
  window.addEventListener('beforeunload', handleBeforeUnload)
  document.addEventListener('visibilitychange', handleVisibilityChange)
  loadQuestions()
  startCountdown()
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null }
})

onBeforeRouteLeave((to, from, next) => {
  if (hasSubmitted.value) { next(); return }
  ElMessageBox.confirm('考试进行中，确定要离开吗？', '提示', {
    confirmButtonText: '继续考试',
    cancelButtonText: '强制离开',
    type: 'warning',
    distinguishCancelAndClose: true
  }).then(() => next(false)).catch((action) => {
    if (action === 'cancel') {
      examState.isInExam = false
      examState.examId = null
      next()
    } else { next(false) }
  })
})

const runCode = async (index) => {
  const code = codeAnswers.value[index]
  const input = codeInputs.value[index]
  if (!code || !code.trim()) { ElMessage.warning('请先输入代码'); return }
  try {
    codeRunning.value[index] = true
    const res = await fetch('http://localhost:8081/api/compile-c', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code, input })
    })
    codeOutputs.value[index] = await res.text()
  } catch (e) {
    codeOutputs.value[index] = '运行失败: ' + (e.message || '')
  } finally { codeRunning.value[index] = false }
}

const submitExam = async (isAutoSubmit = false) => {
  if (!isAutoSubmit) {
    const unanswered = getUnansweredQuestions()
    if (unanswered.length > 0) {
      const unansweredStr = unanswered.length <= 5 
        ? unanswered.join('、') 
        : unanswered.slice(0, 5).join('、') + ` 等${unanswered.length}题`
      try {
        await ElMessageBox.confirm(
          `您还有 ${unanswered.length} 道题未作答（第 ${unansweredStr} 题），确定要交卷吗？`,
          '提交确认', { confirmButtonText: '确定交卷', cancelButtonText: '继续答题', type: 'warning' }
        )
      } catch {
        if (unanswered.length > 0) scrollToQuestion(unanswered[0] - 1)
        return
      }
    } else {
      try {
        await ElMessageBox.confirm('您已完成所有题目，确定要交卷吗？', '提交确认', {
          confirmButtonText: '确定交卷', cancelButtonText: '再检查一下', type: 'info'
        })
      } catch { return }
    }
  }

  if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null }
  
  const totalScore = Number(examTotalScore.value) || 100
  const answers = []
  for (let index = 0; index < allQuestions.value.length; index++) {
    const q = allQuestions.value[index]
    if (!q || !q.type) continue
    const answer = { questionId: q.id, score: q.score || 0 }
    if (q.type === 1) answer.userAnswer = (singleAnswers.value[index] || '').trim()
    else if (q.type === 2) {
      const userArr = Array.isArray(multiAnswers.value[index]) ? multiAnswers.value[index] : []
      answer.userAnswer = userArr.join('')
    }
    else if (q.type === 3) answer.userAnswer = (judgeAnswers.value[index] ?? '').toString().trim()
    else if (q.type === 4) answer.userCode = codeAnswers.value[index] || ''
    answers.push(answer)
  }

  const userStr = sessionStorage.getItem('exam_user')
  let user = null
  if (userStr) { try { user = JSON.parse(userStr) } catch (e) { user = null } }

  const payload = {
    userId: user?.id || null,
    examId: examId.value ? Number(examId.value) : null,
    username: user?.username || '匿名用户',
    examTitle: examTitle.value,
    totalScore,
    answers,
    isAutoSubmit
  }

  try {
    const res = await fetch('http://localhost:8081/api/exam-results/submit', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    const result = await res.json()
    if (!res.ok || result.error) throw new Error(result.error || '提交失败')
    hasSubmitted.value = true
    examState.hasSubmitted = true
    clearStoredAnswers()
    if (result.needsGrading) ElMessage.success('交卷成功！需等待教师批改')
    else ElMessage.success(`交卷成功！得分：${result.score}/${result.totalScore}`)
    router.push('/top/history')
  } catch (e) { ElMessage.error(e.message || '提交失败') }
}
</script>

<style scoped>
.test-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

/* 顶部栏 */
.exam-topbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 70px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  z-index: 1000;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.exam-logo {
  width: 45px;
  height: 45px;
  background: rgba(255,255,255,0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}

.exam-title-info h1 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.exam-meta {
  display: flex;
  gap: 15px;
  margin-top: 4px;
}

.exam-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: rgba(255,255,255,0.85);
}

.topbar-center {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.timer-box {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(255,255,255,0.15);
  padding: 10px 25px;
  border-radius: 30px;
  backdrop-filter: blur(10px);
}

.timer-icon {
  font-size: 22px;
  color: #fff;
}

.timer-text {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  font-family: 'Consolas', monospace;
  letter-spacing: 2px;
}

.timer-box.timer-warning {
  background: rgba(230, 162, 60, 0.3);
  animation: pulse 1s infinite;
}

.timer-box.timer-danger {
  background: rgba(245, 108, 108, 0.4);
  animation: pulse 0.5s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.02); }
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.switch-warning-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(230, 162, 60, 0.3);
  color: #fff;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.switch-warning-badge.danger {
  background: rgba(245, 108, 108, 0.4);
  animation: pulse 1s infinite;
}

.submit-btn {
  height: 40px;
  padding: 0 25px;
  border-radius: 20px;
  font-weight: 600;
  background: #fff;
  color: #667eea;
  border: none;
}

.submit-btn:hover {
  background: #f0f0f0;
}

/* 主体区域 */
.exam-main {
  display: flex;
  padding-top: 90px;
  min-height: calc(100vh - 70px);
}

/* 左侧导航 */
.nav-sidebar {
  width: 280px;
  padding: 20px;
  position: fixed;
  top: 90px;
  left: 0;
  bottom: 0;
  overflow-y: auto;
}

.nav-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}

.nav-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.nav-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.nav-progress {
  font-size: 14px;
  color: #667eea;
  font-weight: 600;
}

.progress-ring-container {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.progress-inner {
  font-size: 20px;
  font-weight: 700;
  color: #667eea;
}

.question-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
  margin-bottom: 20px;
}

.grid-item {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  background: #f5f5f5;
  color: #999;
  border: 2px solid transparent;
}

.grid-item:hover {
  background: #e8e8e8;
  transform: scale(1.05);
}

.grid-item.answered {
  background: linear-gradient(135deg, #67c23a 0%, #5daf34 100%);
  color: #fff;
}

.grid-item.current {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

.nav-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.legend-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #999;
}

.legend-dot {
  width: 14px;
  height: 14px;
  border-radius: 4px;
  background: #f5f5f5;
  border: 1px solid #ddd;
}

.legend-dot.answered {
  background: linear-gradient(135deg, #67c23a 0%, #5daf34 100%);
  border: none;
}

.nav-actions {
  margin-top: 15px;
}

.action-btn {
  width: 100%;
  height: 44px;
  border-radius: 10px;
  font-weight: 500;
}

.save-btn {
  background: #f0f5ff;
  border-color: #d0e0ff;
  color: #667eea;
}

.save-btn:hover {
  background: #e0ebff;
}

/* 右侧答题区 */
.question-area {
  flex: 1;
  margin-left: 300px;
  padding: 20px 30px 40px;
}

.questions-container {
  max-width: 900px;
}

.question-card {
  background: #fff;
  border-radius: 16px;
  padding: 25px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  transition: all 0.3s;
  border: 2px solid transparent;
}

.question-card:hover {
  box-shadow: 0 8px 30px rgba(0,0,0,0.1);
}

.question-card.unanswered {
  border-color: #ffd666;
  background: linear-gradient(135deg, #fffbe6 0%, #fff 100%);
}

.question-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.question-number {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.question-number .num {
  color: #fff;
  font-size: 16px;
  font-weight: 700;
}

.question-type-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.question-type-badge.type-1 { background: #e6f7ff; color: #1890ff; }
.question-type-badge.type-2 { background: #f6ffed; color: #52c41a; }
.question-type-badge.type-3 { background: #fff7e6; color: #fa8c16; }
.question-type-badge.type-4 { background: #f9f0ff; color: #722ed1; }

.question-score {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #f5222d;
  font-weight: 600;
  margin-left: auto;
}

.answered-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: #f6ffed;
  color: #52c41a;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.question-content {
  padding: 0 5px;
}

.question-text {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 20px;
}

/* 选项样式 */
.options-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.options-list.multi .multi-tip {
  font-size: 13px;
  color: #f5222d;
  margin-bottom: 5px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  background: #f8f9fa;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.option-item:hover {
  background: #f0f5ff;
  border-color: #b3c7ff;
}

.option-item.selected {
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  border-color: #1890ff;
}

.option-letter {
  width: 28px;
  height: 28px;
  background: #fff;
  border: 2px solid #d9d9d9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: #666;
  flex-shrink: 0;
}

.option-item.selected .option-letter {
  background: #1890ff;
  border-color: #1890ff;
  color: #fff;
}

.option-checkbox {
  width: 22px;
  height: 22px;
  background: #fff;
  border: 2px solid #d9d9d9;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #fff;
  flex-shrink: 0;
}

.option-item.selected .option-checkbox {
  background: #1890ff;
  border-color: #1890ff;
}

.option-text {
  font-size: 15px;
  color: #333;
  line-height: 1.5;
}

/* 判断题 */
.judge-options {
  display: flex;
  gap: 20px;
}

.judge-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 20px;
  background: #f8f9fa;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 16px;
  font-weight: 500;
}

.judge-item:hover {
  background: #f0f5ff;
}

.judge-item.selected {
  border-width: 3px;
}

.judge-item:first-child.selected {
  background: #f6ffed;
  border-color: #52c41a;
  color: #52c41a;
}

.judge-item:last-child.selected {
  background: #fff2f0;
  border-color: #ff4d4f;
  color: #ff4d4f;
}

.judge-icon {
  font-size: 24px;
}

.judge-icon.correct { color: #52c41a; }
.judge-icon.wrong { color: #ff4d4f; }

/* 编程题 */
.code-editor-section {
  background: #1e1e1e;
  border-radius: 12px;
  padding: 20px;
  overflow: hidden;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.code-label {
  color: #e0e0e0;
  font-size: 14px;
  font-weight: 500;
}

.code-textarea :deep(.el-textarea__inner) {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  line-height: 1.6;
  background: #2d2d2d;
  border: 1px solid #404040;
  border-radius: 8px;
  color: #e0e0e0;
  padding: 15px;
}

.code-textarea :deep(.el-textarea__inner):focus {
  border-color: #667eea;
}

.code-input-section {
  margin-top: 15px;
}

.input-label, .output-label {
  display: block;
  color: #999;
  font-size: 13px;
  margin-bottom: 8px;
}

.code-input-section :deep(.el-textarea__inner) {
  font-family: 'Consolas', monospace;
  background: #2d2d2d;
  border: 1px solid #404040;
  border-radius: 8px;
  color: #e0e0e0;
}

.code-output-section {
  margin-top: 15px;
}

.code-output {
  background: #0d0d0d;
  border: 1px solid #333;
  border-radius: 8px;
  padding: 15px;
  color: #4ec9b0;
  font-family: 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.5;
  max-height: 200px;
  overflow: auto;
  white-space: pre-wrap;
  margin: 0;
}

.code-output.error {
  color: #f48771;
  border-color: #5a3030;
}

/* 警告弹窗 */
.warning-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

.warning-dialog :deep(.el-dialog__header) {
  display: none;
}

.warning-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.warning-modal {
  text-align: center;
  padding: 40px 30px;
}

.warning-icon-wrap {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  font-size: 40px;
  color: #fff;
}

.warning-modal h3 {
  margin: 0 0 15px;
  font-size: 20px;
  color: #333;
}

.warning-desc {
  font-size: 16px;
  color: #666;
  margin: 0 0 10px;
}

.warning-desc strong {
  color: #ff4d4f;
  font-size: 24px;
}

.warning-hint {
  font-size: 14px;
  color: #999;
  margin: 0 0 25px;
}

/* 响应式 */
@media (max-width: 1024px) {
  .nav-sidebar { display: none; }
  .question-area { margin-left: 0; }
}
</style>
