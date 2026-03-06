<template>
  <div class="exam-manage-container">
    <div class="page-card">
      <div class="card-header">
        <span class="header-title">考试管理</span>
        <div class="header-actions">
          <el-input
            v-model="searchKeyword"
            size="default"
            placeholder="按考试名称/科目搜索"
            clearable
            class="search-input"
            :prefix-icon="Search"
          />
          <el-button type="primary" @click="openCreate" class="add-btn">
            <el-icon><Plus /></el-icon>
            新增考试
          </el-button>
        </div>
      </div>

      <div class="table-wrapper">
        <el-table :data="filteredExams" style="width: 100%" class="exam-table">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="title" label="考试名称" min-width="120">
            <template #default="scope">
              <span class="exam-title">{{ scope.row.title }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="subject" label="科目" width="100" />
          <el-table-column prop="totalScore" label="总分" width="70" />
          <el-table-column prop="durationMinutes" label="时长(分钟)" width="100" />
          <el-table-column prop="startTime" label="开始时间" width="160" />
          <el-table-column prop="endTime" label="结束时间" width="160" />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="scope">
              <el-tag
                :type="renderStatusType(scope.row.status)"
                size="small"
                effect="dark"
              >
                {{ renderStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="className" label="班级" width="120">
            <template #default="scope">
              <el-tag v-if="scope.row.className" type="warning" size="small">
                {{ scope.row.className }}
              </el-tag>
              <span v-else class="all-class">全部班级</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="320" fixed="right">
            <template #default="scope">
              <el-button type="warning" size="small" plain @click="openUserDialog(scope.row)">指定用户</el-button>
              <el-button type="primary" size="small" plain @click="openEdit(scope.row)">编辑</el-button>
              <el-button 
                v-if="isExamEnded(scope.row)" 
                type="info" 
                size="small" 
                plain 
                @click="processAbsent(scope.row)"
              >处理缺考</el-button>
              <el-button type="danger" size="small" plain @click="handleDelete(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑考试' : '新增考试'" width="650px" class="exam-dialog">
      <el-form :model="form" label-width="100px" class="exam-form">
        <div class="form-section">
          <div class="section-title">
            <el-icon><Document /></el-icon>
            <span>基本信息</span>
          </div>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="考试名称">
                <el-input v-model="form.title" placeholder="请输入考试名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="科目">
                <el-input v-model="form.subject" placeholder="如：C语言" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="总分">
                <el-input-number v-model="form.totalScore" :min="0" :max="1000" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="考试时长">
                <el-input-number v-model="form.durationMinutes" :min="1" :max="600" style="width: 100%" />
                <span class="form-hint">分钟</span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="开始时间">
                <el-date-picker
                  v-model="form.startTime"
                  type="datetime"
                  placeholder="选择开始时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%;"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="结束时间">
                <el-input :value="calcEndTime" disabled placeholder="根据开始时间和时长自动计算" />
                <span class="form-hint">自动计算</span>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="form-section">
          <div class="section-title">
            <el-icon><Setting /></el-icon>
            <span>发布设置</span>
          </div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="状态">
                <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                  <el-option label="草稿" value="DRAFT" />
                  <el-option label="已发布" value="PUBLISHED" />
                  <el-option label="已结束" value="FINISHED" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="指定班级">
                <el-select v-model="form.classId" placeholder="不选则全部可见" clearable style="width: 100%;">
                  <el-option 
                    v-for="clazz in clazzList" 
                    :key="clazz.id" 
                    :label="clazz.name" 
                    :value="clazz.id" 
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="form-section">
          <div class="section-title">
            <el-icon><List /></el-icon>
            <span>随机出题配置</span>
            <span class="section-hint">（总分自动分配为100分）</span>
          </div>
          <el-alert v-if="questionStats.total > 0" type="info" :closable="false" style="margin-bottom: 15px;">
            题库统计：单选题 {{ questionStats.single }} 道，多选题 {{ questionStats.multi }} 道，判断题 {{ questionStats.judge }} 道，编程题 {{ questionStats.program }} 道
          </el-alert>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="单选题">
                <el-input-number v-model="form.singleCount" :min="0" :max="questionStats.single || 50" style="width: 100%" />
                <span class="score-hint">
                  <span v-if="calcScorePerQuestion > 0">每题约 {{ calcSingleScore }} 分</span>
                  <span v-if="questionStats.single > 0" class="stock-hint">（库存 {{ questionStats.single }} 道）</span>
                </span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="多选题">
                <el-input-number v-model="form.multiCount" :min="0" :max="questionStats.multi || 50" style="width: 100%" />
                <span class="score-hint">
                  <span v-if="calcScorePerQuestion > 0">每题约 {{ calcMultiScore }} 分</span>
                  <span v-if="questionStats.multi > 0" class="stock-hint">（库存 {{ questionStats.multi }} 道）</span>
                </span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="判断题">
                <el-input-number v-model="form.judgeCount" :min="0" :max="questionStats.judge || 50" style="width: 100%" />
                <span class="score-hint">
                  <span v-if="calcScorePerQuestion > 0">每题约 {{ calcJudgeScore }} 分</span>
                  <span v-if="questionStats.judge > 0" class="stock-hint">（库存 {{ questionStats.judge }} 道）</span>
                </span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="编程题">
                <el-input-number v-model="form.programCount" :min="0" :max="questionStats.program || 10" style="width: 100%" />
                <span class="score-hint">
                  <span v-if="calcScorePerQuestion > 0">每题约 {{ calcProgramScore }} 分</span>
                  <span v-if="questionStats.program > 0" class="stock-hint">（库存 {{ questionStats.program }} 道）</span>
                </span>
              </el-form-item>
            </el-col>
          </el-row>
          <div v-if="totalQuestionCount > 0" class="question-summary">
            <el-icon><InfoFilled /></el-icon>
            共 {{ totalQuestionCount }} 题，总分 100 分，平均每题 {{ calcScorePerQuestion.toFixed(1) }} 分
          </div>
        </div>
        
        <el-form-item label="备注">
          <el-input type="textarea" v-model="form.description" placeholder="可填写考试说明" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitExam">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 随机出题对话框 -->
    <el-dialog v-model="randomDialogVisible" title="随机生成试卷" width="700px">
      <div class="random-config">
        <el-alert type="info" :closable="false" style="margin-bottom: 20px;">
          题库统计：单选题 {{ questionStats.single }} 道，多选题 {{ questionStats.multi }} 道，判断题 {{ questionStats.judge }} 道，编程题 {{ questionStats.program }} 道
        </el-alert>
        
        <el-form label-width="100px">
          <el-form-item label="单选题数量">
            <el-input-number v-model="randomConfig.singleCount" :min="0" :max="questionStats.single" />
            <span style="margin-left: 10px; color: #999;">（最多 {{ questionStats.single }} 道）</span>
          </el-form-item>
          <el-form-item label="多选题数量">
            <el-input-number v-model="randomConfig.multiCount" :min="0" :max="questionStats.multi" />
            <span style="margin-left: 10px; color: #999;">（最多 {{ questionStats.multi }} 道）</span>
          </el-form-item>
          <el-form-item label="判断题数量">
            <el-input-number v-model="randomConfig.judgeCount" :min="0" :max="questionStats.judge" />
            <span style="margin-left: 10px; color: #999;">（最多 {{ questionStats.judge }} 道）</span>
          </el-form-item>
          <el-form-item label="编程题数量">
            <el-input-number v-model="randomConfig.programCount" :min="0" :max="questionStats.program" />
            <span style="margin-left: 10px; color: #999;">（最多 {{ questionStats.program }} 道）</span>
          </el-form-item>
        </el-form>

        <div v-if="generatedQuestions.length > 0" class="generated-result">
          <el-divider>生成结果：共 {{ generatedQuestions.length }} 题，总分 {{ generatedTotalScore }} 分</el-divider>
          <el-table :data="generatedQuestions" max-height="300" size="small">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="title" label="题目" show-overflow-tooltip />
            <el-table-column prop="type" label="题型" width="80">
              <template #default="scope">
                {{ renderQuestionType(scope.row.type) }}
              </template>
            </el-table-column>
            <el-table-column prop="score" label="分值" width="60" />
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="randomDialogVisible = false">关闭</el-button>
        <el-button type="success" @click="generateRandom" :loading="generating">生成试卷</el-button>
      </template>
    </el-dialog>

    <!-- 指定用户对话框 -->
    <el-dialog v-model="userDialogVisible" title="指定参考用户" width="700px">
      <div class="user-select-container">
        <el-alert type="info" :closable="false" style="margin-bottom: 15px;">
          不选择任何用户表示所有人都可以参加考试；选择用户后，只有被选中的用户可以参加。
        </el-alert>
        
        <div class="user-filter">
          <el-select v-model="filterClassId" placeholder="按班级筛选" clearable style="width: 200px;" @change="filterUsers">
            <el-option label="全部班级" :value="null" />
            <el-option v-for="c in clazzList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
          <el-input v-model="userSearchKeyword" placeholder="搜索用户名" clearable style="width: 200px; margin-left: 10px;" @input="filterUsers" />
        </div>

        <el-checkbox-group v-model="selectedUserIds" class="user-checkbox-group">
          <el-checkbox v-for="user in filteredUserList" :key="user.id" :label="user.id" :value="user.id">
            {{ user.username }} <span v-if="user.className" style="color: #999;">({{ user.className }})</span>
          </el-checkbox>
        </el-checkbox-group>

        <div class="user-select-footer">
          <span>已选择 {{ selectedUserIds.length }} 人</span>
          <div>
            <el-button size="small" @click="selectAllUsers">全选</el-button>
            <el-button size="small" @click="clearAllUsers">清空</el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveExamUsers" :loading="savingUsers">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Document, Setting, List, InfoFilled } from '@element-plus/icons-vue'

const exams = ref([])
const clazzList = ref([])
const searchKeyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)

// 随机出题相关
const randomDialogVisible = ref(false)
const generating = ref(false)
const questionStats = ref({ single: 0, multi: 0, judge: 0, program: 0, total: 0 })
const randomConfig = ref({ singleCount: 5, multiCount: 5, judgeCount: 5, programCount: 1 })
const generatedQuestions = ref([])
const generatedTotalScore = ref(0)

// 指定用户相关
const userDialogVisible = ref(false)
const currentExamId = ref(null)
const allUserList = ref([])
const filteredUserList = ref([])
const selectedUserIds = ref([])
const filterClassId = ref(null)
const userSearchKeyword = ref('')
const savingUsers = ref(false)

const form = ref({
  id: null,
  title: '',
  description: '',
  subject: '',
  totalScore: 100,
  durationMinutes: 90,
  startTime: '',
  endTime: '',
  status: 'DRAFT',
  classId: null,
  singleCount: 5,
  multiCount: 5,
  judgeCount: 5,
  programCount: 1
})

// 前端过滤后的考试列表
const filteredExams = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase()
  if (!kw) return exams.value
  return exams.value.filter(exam => {
    const title = (exam.title || '').toLowerCase()
    const subject = (exam.subject || '').toLowerCase()
    return title.includes(kw) || subject.includes(kw)
  })
})

// 计算题目总数和每题分数
const totalQuestionCount = computed(() => {
  return (form.value.singleCount || 0) + (form.value.multiCount || 0) + 
         (form.value.judgeCount || 0) + (form.value.programCount || 0)
})

const calcScorePerQuestion = computed(() => {
  if (totalQuestionCount.value === 0) return 0
  return 100 / totalQuestionCount.value
})

const calcSingleScore = computed(() => {
  return Math.round(calcScorePerQuestion.value)
})

const calcMultiScore = computed(() => {
  return Math.round(calcScorePerQuestion.value)
})

const calcJudgeScore = computed(() => {
  return Math.round(calcScorePerQuestion.value)
})

const calcProgramScore = computed(() => {
  return Math.round(calcScorePerQuestion.value)
})

// 计算结束时间
const calcEndTime = computed(() => {
  if (!form.value.startTime || !form.value.durationMinutes) return ''
  try {
    const startDate = new Date(form.value.startTime)
    const endDate = new Date(startDate.getTime() + form.value.durationMinutes * 60 * 1000)
    const pad = (n) => n.toString().padStart(2, '0')
    return `${endDate.getFullYear()}-${pad(endDate.getMonth() + 1)}-${pad(endDate.getDate())} ${pad(endDate.getHours())}:${pad(endDate.getMinutes())}:${pad(endDate.getSeconds())}`
  } catch (e) {
    return ''
  }
})

// 判断考试是否已结束
const isExamEnded = (exam) => {
  if (exam.status === 'FINISHED') return true
  if (!exam.endTime) return false
  try {
    const endDate = new Date(exam.endTime)
    return Date.now() > endDate.getTime()
  } catch (e) {
    return false
  }
}

// 自动结束考试并处理缺考
const autoFinishExam = async (exam) => {
  try {
    // 1. 更新考试状态为已结束
    const updateRes = await fetch('http://localhost:8081/api/exams', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ...exam, status: 'FINISHED' })
    })
    
    if (updateRes.ok) {
      // 2. 处理缺考
      await fetch(`http://localhost:8081/api/exams/${exam.id}/process-absent`, {
        method: 'POST'
      })
      
      // 更新本地状态
      exam.status = 'FINISHED'
      console.log(`考试"${exam.title}"已自动结束并处理缺考`)
    }
  } catch (e) {
    console.error('自动结束考试失败', e)
  }
}

const fetchExams = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/exams')
    if (!res.ok) {
      throw new Error('获取考试列表失败')
    }
    exams.value = await res.json()
    
    // 自动处理已过期但状态还是"已发布"的考试
    for (const exam of exams.value) {
      if (exam.status === 'PUBLISHED' && isExamEnded(exam)) {
        await autoFinishExam(exam)
      }
    }
  } catch (e) {
    ElMessage.error(e.message || '获取考试列表失败')
  }
}

const fetchClazzList = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/clazz')
    if (res.ok) {
      const result = await res.json()
      clazzList.value = result.data || result || []
    }
  } catch (e) {
    console.error('获取班级列表失败', e)
  }
}

const submitExam = async () => {
  try {
    // 自动计算结束时间：开始时间 + 考试时长
    const submitData = { ...form.value }
    if (submitData.startTime && submitData.durationMinutes) {
      const startDate = new Date(submitData.startTime)
      const endDate = new Date(startDate.getTime() + submitData.durationMinutes * 60 * 1000)
      // 格式化为 YYYY-MM-DD HH:mm:ss
      const pad = (n) => n.toString().padStart(2, '0')
      submitData.endTime = `${endDate.getFullYear()}-${pad(endDate.getMonth() + 1)}-${pad(endDate.getDate())} ${pad(endDate.getHours())}:${pad(endDate.getMinutes())}:${pad(endDate.getSeconds())}`
    }
    
    const url = 'http://localhost:8081/api/exams'
    const method = isEdit.value ? 'PUT' : 'POST'
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(submitData)
    })
    if (!res.ok) {
      throw new Error(isEdit.value ? '更新考试失败' : '新增考试失败')
    }
    ElMessage.success(isEdit.value ? '更新考试成功' : '新增考试成功')
    dialogVisible.value = false
    await fetchExams()
  } catch (e) {
    ElMessage.error(e.message || (isEdit.value ? '更新考试失败' : '新增考试失败'))
  }
}

const handleDelete = async (id) => {
  ElMessageBox.confirm('确定要删除该考试吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await fetch(`http://localhost:8081/api/exams/${id}`, {
        method: 'DELETE'
      })
      if (!res.ok) {
        throw new Error('删除考试失败')
      }
      ElMessage.success('删除考试成功')
      await fetchExams()
    } catch (e) {
      ElMessage.error(e.message || '删除考试失败')
    }
  }).catch(() => {})
}

// 获取题库统计
const fetchQuestionStats = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/questions/stats')
    if (res.ok) {
      questionStats.value = await res.json()
    }
  } catch (e) {
    console.error('获取题库统计失败', e)
  }
}

const openEdit = async (row) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
  await fetchQuestionStats()
}

const openCreate = async () => {
  isEdit.value = false
  form.value = {
    id: null,
    title: '',
    description: '',
    subject: '',
    totalScore: 100,
    durationMinutes: 90,
    startTime: '',
    endTime: '',
    status: 'DRAFT',
    classId: null,
    singleCount: 5,
    multiCount: 5,
    judgeCount: 5,
    programCount: 1
  }
  dialogVisible.value = true
  await fetchQuestionStats()
}


const renderStatusText = (status) => {
  if (!status) return '未知'
  switch (status) {
    case 'DRAFT':
      return '草稿'
    case 'PUBLISHED':
      return '已发布'
    case 'FINISHED':
      return '已结束'
    default:
      return status
  }
}

const renderStatusType = (status) => {
  switch (status) {
    case 'DRAFT':
      return 'info'
    case 'PUBLISHED':
      return 'success'
    case 'FINISHED':
      return 'warning'
    default:
      return 'info'
  }
}

const renderQuestionType = (type) => {
  switch (type) {
    case 1: return '单选题'
    case 2: return '多选题'
    case 3: return '判断题'
    case 4: return '编程题'
    default: return '未知'
  }
}

// 打开随机出题对话框
const openRandomDialog = async () => {
  generatedQuestions.value = []
  generatedTotalScore.value = 0
  randomDialogVisible.value = true
  await fetchQuestionStats()
}

// 生成随机试卷
const generateRandom = async () => {
  generating.value = true
  try {
    const { singleCount, multiCount, judgeCount, programCount } = randomConfig.value
    const url = `http://localhost:8081/api/questions/random?singleCount=${singleCount}&multiCount=${multiCount}&judgeCount=${judgeCount}&programCount=${programCount}`
    const res = await fetch(url)
    if (res.ok) {
      const result = await res.json()
      generatedQuestions.value = result.questions || []
      generatedTotalScore.value = result.totalScore || 0
      ElMessage.success(`成功生成 ${result.questionCount} 道题目，总分 ${result.totalScore} 分`)
    } else {
      ElMessage.error('生成试卷失败')
    }
  } catch (e) {
    ElMessage.error('网络错误')
  } finally {
    generating.value = false
  }
}

// 获取所有用户列表
const fetchAllUsers = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/users')
    if (res.ok) {
      const result = await res.json()
      // 只获取学生用户（role !== '2'）
      allUserList.value = (result.data || result || []).filter(u => u.role !== '2')
      filteredUserList.value = allUserList.value
    }
  } catch (e) {
    console.error('获取用户列表失败', e)
  }
}

// 筛选用户
const filterUsers = () => {
  let list = allUserList.value
  if (filterClassId.value) {
    list = list.filter(u => u.classId === filterClassId.value)
  }
  if (userSearchKeyword.value) {
    const kw = userSearchKeyword.value.toLowerCase()
    list = list.filter(u => (u.username || '').toLowerCase().includes(kw))
  }
  filteredUserList.value = list
}

// 打开指定用户对话框
const openUserDialog = async (exam) => {
  currentExamId.value = exam.id
  selectedUserIds.value = []
  filterClassId.value = null
  userSearchKeyword.value = ''
  userDialogVisible.value = true
  
  // 获取所有用户
  await fetchAllUsers()
  
  // 获取已指定的用户
  try {
    const res = await fetch(`http://localhost:8081/api/exams/${exam.id}/userIds`)
    if (res.ok) {
      selectedUserIds.value = await res.json()
    }
  } catch (e) {
    console.error('获取考试用户失败', e)
  }
}

// 全选用户
const selectAllUsers = () => {
  selectedUserIds.value = filteredUserList.value.map(u => u.id)
}

// 清空选择
const clearAllUsers = () => {
  selectedUserIds.value = []
}

// 保存考试用户
const saveExamUsers = async () => {
  savingUsers.value = true
  try {
    const res = await fetch(`http://localhost:8081/api/exams/${currentExamId.value}/users`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(selectedUserIds.value)
    })
    if (res.ok) {
      ElMessage.success('保存成功')
      userDialogVisible.value = false
    } else {
      ElMessage.error('保存失败')
    }
  } catch (e) {
    ElMessage.error('网络错误')
  } finally {
    savingUsers.value = false
  }
}

onMounted(() => {
  fetchExams()
  fetchClazzList()
})

// 处理缺考（手动触发）
const processAbsent = async (exam) => {
  try {
    await ElMessageBox.confirm(
      `确定要为考试"${exam.title}"处理缺考吗？\n系统将为未参加考试的学生生成0分缺考记录。`,
      '处理缺考',
      { type: 'warning' }
    )
    
    const res = await fetch(`http://localhost:8081/api/exams/${exam.id}/process-absent`, {
      method: 'POST'
    })
    const result = await res.json()
    
    if (res.ok && result.code === 200) {
      ElMessage.success(result.data || '处理完成')
      // 刷新考试列表
      await fetchExams()
    } else {
      ElMessage.error(result.message || '处理失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('处理缺考错误:', e)
      ElMessage.error('处理缺考失败')
    }
  }
}
</script>

<style scoped>
.exam-manage-container {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
}

.page-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.card-header {
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-bottom: none;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 240px;
}

.search-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.9);
  border: none;
  box-shadow: none;
}

.search-input :deep(.el-input__wrapper:hover),
.search-input :deep(.el-input__wrapper.is-focus) {
  background: #fff;
}

.add-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
}

.add-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
}

.table-wrapper {
  flex: 1;
  padding: 16px;
  overflow: auto;
}

.exam-table {
  border-radius: 8px;
  overflow: hidden;
}

.exam-table :deep(.el-table__header th) {
  background: #f8f9fa;
  color: #333;
  font-weight: 500;
  font-size: 13px;
}

.exam-table :deep(.el-table__row) {
  transition: all 0.2s;
}

.exam-table :deep(.el-table__row:hover > td) {
  background: #f0f5ff !important;
}

.exam-title {
  font-weight: 500;
  color: #333;
}

.all-class {
  color: #999;
  font-size: 12px;
}

.random-config {
  padding: 10px 0;
}

.generated-result {
  margin-top: 20px;
}

.user-select-container {
  max-height: 500px;
}

.user-filter {
  display: flex;
  margin-bottom: 15px;
}

.user-checkbox-group {
  display: flex;
  flex-wrap: wrap;
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #eee;
  padding: 10px;
  border-radius: 8px;
  background: #fafafa;
}

.user-checkbox-group .el-checkbox {
  width: 200px;
  margin: 5px 10px;
}

.user-select-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}

/* 考试对话框样式 */
.exam-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  margin-right: 0;
  padding: 16px 20px;
}

.exam-dialog :deep(.el-dialog__title) {
  color: #fff;
  font-weight: 600;
}

.exam-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #fff;
}

.exam-dialog :deep(.el-dialog__body) {
  padding: 20px;
  max-height: 65vh;
  overflow-y: auto;
}

.exam-form .form-section {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.exam-form .section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e8e8e8;
}

.exam-form .section-title .el-icon {
  color: #667eea;
  font-size: 16px;
}

.exam-form .section-hint {
  font-size: 12px;
  color: #999;
  font-weight: 400;
  margin-left: 4px;
}

.exam-form .form-hint {
  font-size: 12px;
  color: #999;
  margin-left: 8px;
}

.exam-form .score-hint {
  display: block;
  font-size: 12px;
  color: #67c23a;
  margin-top: 4px;
}

.exam-form .stock-hint {
  color: #909399;
  margin-left: 5px;
}

.exam-form .question-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  padding: 12px 16px;
  border-radius: 8px;
  color: #667eea;
  font-size: 14px;
  font-weight: 500;
  margin-top: 12px;
}

.exam-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.exam-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
}

/* 滚动条美化 */
.table-wrapper::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.table-wrapper::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 3px;
}

.table-wrapper::-webkit-scrollbar-track {
  background: transparent;
}
</style>
