<template>
  <div class="test-container">
    <!-- 切屏警告弹窗 -->
    <el-dialog
      v-model="switchWarningVisible"
      title="切屏警告"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      class="switch-warning-dialog"
    >
      <div class="warning-content">
        <el-icon class="warning-icon"><WarningFilled /></el-icon>
        <p class="warning-text">检测到您离开了考试页面！</p>
        <p class="warning-count">已切屏 <span class="count-num">{{ switchCount }}</span> 次，超过10次将自动交卷</p>
        <p class="warning-tip">请专心答题，不要切换到其他应用</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="switchWarningVisible = false">我知道了，继续考试</el-button>
      </template>
    </el-dialog>

    <el-card>
      <template #header>
        <div class="exam-header">
          <h2>{{ examTitle }}</h2>
          <div class="exam-info">
            <span>考试时长：{{ examDuration }}分钟</span>
            <span>总分：{{ examTotalScore }}分</span>
            <span class="time-display" :class="{ 'time-warning': remainingSeconds < 300 }">
              剩余时间：<span class="time-counter">{{ formatTime(remainingSeconds) }}</span>
            </span>
            <span v-if="switchCount > 0" class="switch-count" :class="{ 'switch-danger': switchCount >= 7 }">
              切屏次数：{{ switchCount }}/10
            </span>
          </div>
        </div>
      </template>
      
      <div class="exam-content">
        <div
          v-for="(q, index) in allQuestions"
          :key="q.id || 'q-' + index"
          class="question"
        >
          <div class="question-title">
            <strong>{{ index + 1 }}. </strong>
            <span>{{ q.title || '试题' }}</span>
            <span
              v-if="q.type === 2"
              class="multi-tip-title"
            >
              （本题可选择多个选项）
            </span>
          </div>
          <div class="question-options">
            <!-- 单选题：type = 1 -->
            <el-radio-group v-if="q.type === 1" v-model="singleAnswers[index]">
              <el-radio label="A" size="large">A. {{ q.optionA || '选项A' }}</el-radio>
              <el-radio label="B" size="large">B. {{ q.optionB || '选项B' }}</el-radio>
              <el-radio label="C" size="large">C. {{ q.optionC || '选项C' }}</el-radio>
              <el-radio label="D" size="large">D. {{ q.optionD || '选项D' }}</el-radio>
            </el-radio-group>

            <!-- 多选题：type = 2 -->
            <div v-else-if="q.type === 2" class="multi-question-block">
              <el-checkbox-group v-model="multiAnswers[index]">
                <el-checkbox label="A" size="large">A. {{ q.optionA || '选项A' }}</el-checkbox>
                <el-checkbox label="B" size="large">B. {{ q.optionB || '选项B' }}</el-checkbox>
                <el-checkbox label="C" size="large">C. {{ q.optionC || '选项C' }}</el-checkbox>
                <el-checkbox label="D" size="large">D. {{ q.optionD || '选项D' }}</el-checkbox>
              </el-checkbox-group>
            </div>

            <!-- 判断题：type = 3 -->
            <el-radio-group v-else-if="q.type === 3" v-model="judgeAnswers[index]">
              <el-radio label="1" size="large">正确</el-radio>
              <el-radio label="0" size="large">错误</el-radio>
            </el-radio-group>

            <!-- 编程题：type = 4 -->
            <div v-else-if="q.type === 4" class="code-question">
              <el-form label-width="80px">
                <el-form-item label="代码">
                  <el-input
                    type="textarea"
                    v-model="codeAnswers[index]"
                    :rows="8"
                    placeholder="请在此编写C语言代码"
                  />
                </el-form-item>
                <el-form-item label="输入">
                  <el-input
                    type="textarea"
                    v-model="codeInputs[index]"
                    :rows="3"
                    placeholder="程序运行时的标准输入，可留空"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button
                    type="primary"
                    size="small"
                    :loading="codeRunning[index]"
                    @click="runCode(index)"
                  >
                    运行代码
                  </el-button>
                </el-form-item>
                <el-form-item label="输出" v-if="codeOutputs[index]">
                  <pre
                    class="code-output"
                    :class="{
                      'code-error': codeOutputs[index].startsWith('[编译失败]')
                        || codeOutputs[index].startsWith('[运行超时]')
                        || codeOutputs[index].startsWith('运行失败:'),
                      'code-normal': codeOutputs[index].startsWith('[程序输出]')
                    }"
                  >
{{ codeOutputs[index] }}
                  </pre>
                </el-form-item>
              </el-form>
            </div>

            <!-- 其他类型暂不渲染选项 -->
          </div>
        </div>
        
        <div class="submit-section">
          <el-button type="primary" size="large" @click="submitExam">提交试卷</el-button>
          <el-button size="large">暂存答案</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'
import { examState } from '@/router'

// 切屏检测相关
const switchCount = ref(0)
const switchWarningVisible = ref(false)
const MAX_SWITCH_COUNT = 10


// 各题型答案数组（仅前端占位）
const singleAnswers = ref([])
const multiAnswers = ref([])
const judgeAnswers = ref([])

// 编程题代码及输入输出
const codeAnswers = ref([])
const codeInputs = ref([])
const codeOutputs = ref([])
const codeRunning = ref([]) // 记录每道题是否正在运行

// 所有题目列表
const allQuestions = ref([])

const router = useRouter()
const route = useRoute()

// 从路由参数中读取考试信息
const examId = ref(route.query.examId || null)
const examTitle = ref(route.query.title || '考试')
const examDuration = ref(route.query.duration || 120)
const examTotalScore = ref(route.query.totalScore || 100)

// 倒计时相关
const remainingSeconds = ref(0)
let countdownTimer = null

// 格式化时间显示
const formatTime = (seconds) => {
  if (seconds <= 0) return '00:00:00'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

// 开始倒计时
const startCountdown = () => {
  // 初始化剩余时间（分钟转秒）
  remainingSeconds.value = Number(examDuration.value) * 60
  
  countdownTimer = setInterval(() => {
    if (remainingSeconds.value > 0) {
      remainingSeconds.value--
      
      // 剩余5分钟时提醒
      if (remainingSeconds.value === 300) {
        ElMessage.warning('注意：考试还剩5分钟，请尽快完成！')
      }
      // 剩余1分钟时提醒
      if (remainingSeconds.value === 60) {
        ElMessage.warning('注意：考试还剩1分钟！')
      }
    } else {
      // 时间到，自动交卷
      clearInterval(countdownTimer)
      autoSubmitExam()
    }
  }, 1000)
}

// 自动交卷
const autoSubmitExam = async () => {
  ElMessage.warning('考试时间到，系统自动交卷！')
  await submitExam(true)
}

// 检查当前用户是否已参加过本场考试
const checkAlreadyParticipated = async () => {
  const userStr = sessionStorage.getItem('exam_user')
  if (!userStr) return false
  let user = null
  try {
    user = JSON.parse(userStr)
  } catch (e) {
    return false
  }
  if (!user || !user.id || !examId.value) return false

  try {
    const res = await fetch(`http://localhost:8081/api/exam-results?userId=${user.id}`)
    if (!res.ok) return false
    const list = await res.json()
    if (!Array.isArray(list)) return false
    const targetId = Number(examId.value)
    return list.some(r => r && Number(r.examId) === targetId)
  } catch (e) {
    return false
  }
}

// 根据考试配置随机加载题目（每个学生抽到的题目不同）
const loadQuestions = async () => {
  try {
    // 调用考试随机出题接口
    const res = await fetch(`http://localhost:8081/api/exams/${examId.value}/questions`)
    if (res.ok) {
      const data = await res.json()
      if (data.error) {
        ElMessage.error(data.error)
        return
      }
      
      let list = data.questions || []
      if (Array.isArray(list)) {
        // 按题型排序：1 单选，2 多选，3 判断，4 编程
        list.sort((a, b) => {
          const ta = a && a.type ? a.type : 0
          const tb = b && b.type ? b.type : 0
          return ta - tb
        })

        allQuestions.value = list

        // 初始化答案数组长度（统一按总题数）
        singleAnswers.value = new Array(list.length).fill('')
        // 多选题需要为每个题目创建独立的数组，避免共享引用
        multiAnswers.value = Array.from({ length: list.length }, () => [])
        judgeAnswers.value = new Array(list.length).fill('')
        codeAnswers.value = new Array(list.length).fill('')
        codeInputs.value = new Array(list.length).fill('')
        codeOutputs.value = new Array(list.length).fill('')
        codeRunning.value = new Array(list.length).fill(false)
      }
    }
  } catch (e) {
    ElMessage.error('加载题目失败')
  }
}

// 是否已交卷
const hasSubmitted = ref(false)

// 浏览器关闭/刷新拦截
const handleBeforeUnload = (e) => {
  if (!hasSubmitted.value) {
    e.preventDefault()
    e.returnValue = '考试进行中，确定要离开吗？离开后答题进度将丢失！'
    return e.returnValue
  }
}

// 切屏检测 - 页面可见性变化（只在用户真正切换页面时触发）
const handleVisibilityChange = () => {
  if (hasSubmitted.value) return
  
  if (document.hidden) {
    // 页面被隐藏（切换到其他标签页或应用）
    switchCount.value++
    
    if (switchCount.value >= MAX_SWITCH_COUNT) {
      // 超过限制，自动交卷
      ElMessage.error('切屏次数超过限制，系统自动交卷！')
      submitExam(true)
    }
  } else {
    // 页面重新可见，显示警告弹窗
    if (switchCount.value > 0 && switchCount.value < MAX_SWITCH_COUNT) {
      switchWarningVisible.value = true
    }
  }
}


onMounted(async () => {
  const participated = await checkAlreadyParticipated()
  if (participated) {
    ElMessage.warning('您已参加过本场考试，不能重复作答')
    router.push('/top/history')
    return
  }
  
  // 设置考试状态
  examState.isInExam = true
  examState.examId = examId.value
  
  // 添加浏览器关闭/刷新拦截
  window.addEventListener('beforeunload', handleBeforeUnload)
  
  // 添加切屏检测（仅监听页面可见性变化，避免误判）
  document.addEventListener('visibilitychange', handleVisibilityChange)
  
  loadQuestions()
  
  // 开始倒计时
  startCountdown()
})

// 组件卸载时移除事件监听和定时器
onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})

// 路由离开守卫
onBeforeRouteLeave((to, from, next) => {
  // 已交卷，允许离开
  if (hasSubmitted.value) {
    next()
    return
  }
  
  // 未交卷时弹出确认框
  ElMessageBox.confirm(
    '考试进行中，确定要离开吗？离开后答题进度将丢失！',
    '提示',
    {
      confirmButtonText: '继续考试',
      cancelButtonText: '强制离开',
      type: 'warning',
      distinguishCancelAndClose: true
    }
  ).then(() => {
    // 点击"继续考试"，不离开
    next(false)
  }).catch((action) => {
    if (action === 'cancel') {
      // 点击"强制离开"，允许离开
      examState.isInExam = false
      examState.examId = null
      next()
    } else {
      // 点击关闭按钮，不离开
      next(false)
    }
  })
})

const runCode = async (index) => {
  const code = codeAnswers.value[index]
  const input = codeInputs.value[index]
  if (!code || !code.trim()) {
    ElMessage.warning('请先输入C语言代码')
    return
  }
  try {
    codeRunning.value[index] = true
    const res = await fetch('http://localhost:8081/api/compile-c', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code, input })
    })
    const text = await res.text()
    codeOutputs.value[index] = text
  } catch (e) {
    codeOutputs.value[index] = '运行失败: ' + (e.message || '')
  } finally {
    codeRunning.value[index] = false
  }
}

const submitExam = async (isAutoSubmit = false) => {
  // 停止倒计时
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  
  // 收集用户答案，发送到后端进行评分
  const totalScore = Number(examTotalScore.value) || 100
  const answers = []
  
  for (let index = 0; index < allQuestions.value.length; index++) {
    const q = allQuestions.value[index]
    if (!q || !q.type) continue
    
    const answer = { 
      questionId: q.id,
      score: q.score || 0  // 传递动态调整后的分数
    }
    
    // 单选题
    if (q.type === 1) {
      answer.userAnswer = (singleAnswers.value[index] || '').trim()
    }
    // 多选题
    else if (q.type === 2) {
      const userArr = Array.isArray(multiAnswers.value[index]) ? multiAnswers.value[index] : []
      answer.userAnswer = userArr.join('')
    }
    // 判断题
    else if (q.type === 3) {
      answer.userAnswer = (judgeAnswers.value[index] ?? '').toString().trim()
    }
    // 编程题
    else if (q.type === 4) {
      answer.userCode = codeAnswers.value[index] || ''
    }
    
    answers.push(answer)
  }

  const userStr = sessionStorage.getItem('exam_user')
  let user = null
  if (userStr) {
    try {
      user = JSON.parse(userStr)
    } catch (e) {
      user = null
    }
  }

  const payload = {
    userId: user && user.id ? user.id : null,
    examId: examId.value ? Number(examId.value) : null,
    username: user && user.username ? user.username : '匿名用户',
    examTitle: examTitle.value,
    totalScore,
    answers,
    isAutoSubmit  // 标记是否为自动交卷
  }

  try {
    // 调用后端评分接口
    const res = await fetch('http://localhost:8081/api/exam-results/submit', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    const result = await res.json()
    
    if (!res.ok || result.error) {
      throw new Error(result.error || '提交成绩失败')
    }
    
    hasSubmitted.value = true
    examState.hasSubmitted = true  // 设置交卷标记，允许路由跳转
    
    // 根据是否需要批改显示不同提示
    if (result.needsGrading) {
      ElMessage.success('交卷成功！试卷包含编程题，需等待教师批改后查看成绩')
    } else {
      ElMessage.success(`交卷成功！得分：${result.score}/${result.totalScore}`)
    }
    router.push('/top/history')
  } catch (e) {
    ElMessage.error(e.message || '提交成绩失败')
  }
}
</script>

<style scoped>
/* 试卷容器 - 模拟纸张效果 */
.test-container {
  padding: 20px 40px;
  min-height: calc(100vh - 70px);
  background: #e8e8e8;
}

.test-container :deep(.el-card) {
  max-width: 900px;
  margin: 0 auto;
  border-radius: 0;
  border: none;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.15);
  background: #fff;
}

/* 试卷头部 - 标题区域 */
.test-container :deep(.el-card__header) {
  background: #fff;
  border-bottom: 3px double #333;
  padding: 30px 40px 20px;
}

.exam-header {
  text-align: center;
}

.exam-header h2 {
  margin: 0 0 20px 0;
  color: #000;
  font-size: 26px;
  font-weight: 700;
  font-family: "SimHei", "黑体", sans-serif;
  letter-spacing: 4px;
}

.exam-info {
  display: flex;
  justify-content: center;
  gap: 30px;
  flex-wrap: wrap;
  padding: 15px 0;
  border-top: 1px solid #ddd;
  border-bottom: 1px solid #ddd;
}

.exam-info > span {
  background: transparent;
  padding: 6px 12px;
  color: #333;
  font-size: 14px;
  font-weight: 500;
  border: none;
}

.time-counter {
  color: #e74c3c !important;
  font-weight: 700;
  font-size: 16px;
  font-family: "Consolas", monospace;
}

.time-display {
  transition: all 0.3s;
}

.time-display.time-warning {
  animation: blink 1s infinite;
  color: #e74c3c !important;
}

.time-display.time-warning .time-counter {
  font-size: 18px;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

/* 试卷内容区域 */
.exam-content {
  padding: 30px 50px;
  background: #fff;
}

/* 题目样式 - 传统试卷风格 */
.question {
  margin-bottom: 30px;
  padding: 0 0 25px 0;
  background: transparent;
  border: none;
  border-bottom: 1px dashed #ccc;
}

.question:last-of-type {
  border-bottom: none;
}

.question-title {
  margin-bottom: 15px;
  font-size: 15px;
  line-height: 1.8;
  color: #000;
  text-align: justify;
}

.question-title strong {
  display: inline;
  background: transparent;
  color: #000;
  font-size: 15px;
  margin-right: 5px;
  font-weight: 700;
}

/* 选项样式 - 横向排列 */
.question-options {
  margin-left: 25px;
  margin-top: 12px;
}

.question-options :deep(.el-radio-group),
.question-options :deep(.el-checkbox-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.question-options .el-radio,
.question-options .el-checkbox {
  display: inline-flex;
  margin: 0;
  padding: 8px 20px;
  background: #f9f9f9;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  transition: all 0.2s ease;
  min-width: 120px;
}

.question-options .el-radio:hover,
.question-options .el-checkbox:hover {
  background: #f0f0f0;
  border-color: #999;
}

.question-options :deep(.el-radio.is-checked),
.question-options :deep(.el-checkbox.is-checked) {
  background: #e8f4fd;
  border-color: #409eff;
}

.question-options :deep(.el-radio__label),
.question-options :deep(.el-checkbox__label) {
  font-size: 14px;
  color: #333;
}

/* 提交区域 */
.submit-section {
  margin-top: 40px;
  padding: 30px;
  background: #f5f5f5;
  border-top: 2px solid #ddd;
  text-align: center;
}

.submit-section .el-button {
  margin: 0 15px;
  height: 44px;
  padding: 0 50px;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 600;
}

.submit-section .el-button--primary {
  background: #409eff;
  border-color: #409eff;
}

.submit-section .el-button--primary:hover {
  background: #66b1ff;
  border-color: #66b1ff;
}

/* 多选题提示 */
.multi-tip-title {
  color: #e74c3c;
  font-size: 13px;
  margin-left: 5px;
  font-weight: 500;
}

/* 编程题样式 */
.code-question {
  background: #f8f8f8;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin-top: 10px;
}

.code-question :deep(.el-textarea__inner) {
  font-family: 'Consolas', 'Courier New', monospace;
  font-size: 14px;
  border-radius: 4px;
  background: #fff;
}

.code-question :deep(.el-form-item__label) {
  font-weight: 600;
  color: #333;
}

.code-output {
  max-height: 200px;
  overflow: auto;
  font-family: 'Consolas', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  background-color: #1e1e1e;
  color: #d4d4d4;
  padding: 15px;
  border-radius: 4px;
  border: 1px solid #333;
  white-space: pre-wrap;
}

.code-output.code-normal {
  border-color: #28a745;
}

.code-output.code-error {
  border-color: #dc3545;
  color: #f8d7da;
}

/* 切屏次数显示 */
.switch-count {
  color: #e67e22 !important;
  font-weight: 600;
}

.switch-count.switch-danger {
  color: #e74c3c !important;
  animation: blink 1s infinite;
}

/* 切屏警告弹窗 */
.switch-warning-dialog :deep(.el-dialog) {
  border-radius: 8px;
  overflow: hidden;
}

.switch-warning-dialog :deep(.el-dialog__header) {
  background: #e74c3c;
  padding: 15px 20px;
  margin: 0;
}

.switch-warning-dialog :deep(.el-dialog__title) {
  color: #fff;
  font-weight: 700;
  font-size: 16px;
}

.warning-content {
  text-align: center;
  padding: 30px 20px;
}

.warning-icon {
  font-size: 60px;
  color: #e74c3c;
  margin-bottom: 15px;
}

.warning-text {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  margin: 0 0 12px 0;
}

.warning-count {
  font-size: 15px;
  color: #666;
  margin: 0 0 10px 0;
}

.warning-count .count-num {
  color: #e74c3c;
  font-size: 24px;
  font-weight: 800;
}

.warning-tip {
  font-size: 13px;
  color: #999;
  margin: 0;
  padding: 10px 15px;
  background: #f5f5f5;
  border-radius: 4px;
  display: inline-block;
}

/* 响应式 */
@media (max-width: 768px) {
  .test-container {
    padding: 10px;
  }
  
  .exam-content {
    padding: 20px;
  }
  
  .question-options .el-radio,
  .question-options .el-checkbox {
    min-width: 100%;
  }
}
</style>