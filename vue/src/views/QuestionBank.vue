<template>
  <div class="question-bank-container">
    <div class="page-card">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <el-icon class="header-icon"><Document /></el-icon>
          <span class="header-title">题库管理</span>
          <span class="header-subtitle">共 {{ questions.length }} 道题目</span>
        </div>
        <div class="header-right">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索题目标题或科目"
            clearable
            class="search-input"
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select v-model="typeFilter" placeholder="题型筛选" clearable class="filter-select" @change="handleSearch">
            <el-option label="单选题" :value="1" />
            <el-option label="多选题" :value="2" />
            <el-option label="判断题" :value="3" />
            <el-option label="编程题" :value="4" />
          </el-select>
          <el-button type="primary" class="add-btn" @click="openCreate">
            <el-icon><Plus /></el-icon>
            新增题目
          </el-button>
        </div>
      </div>

      <!-- 表格区域 -->
      <div class="table-wrapper">
        <el-table :data="filteredQuestions" class="question-table" stripe>
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="title" label="题目标题" min-width="200">
            <template #default="scope">
              <div class="title-cell">
                <span class="question-title-text">{{ scope.row.title }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="subject" label="科目" width="120" align="center">
            <template #default="scope">
              <el-tag size="small" type="info">{{ scope.row.subject || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="题型" width="100" align="center">
            <template #default="scope">
              <el-tag :type="getTypeTagType(scope.row.type)" size="small">
                {{ renderType(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="difficulty" label="难度" width="90" align="center">
            <template #default="scope">
              <span :class="['difficulty-badge', getDifficultyClass(scope.row.difficulty)]">
                {{ renderDifficulty(scope.row.difficulty) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="分值" width="80" align="center">
            <template #default="scope">
              <span class="score-text">{{ scope.row.score }}分</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center" fixed="right">
            <template #default="scope">
              <el-button type="primary" link size="small" @click="openEdit(scope.row)">
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
              <el-button type="danger" link size="small" @click="handleDelete(scope.row.id)">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑题目' : '新增题目'" width="650px" class="question-dialog">
      <el-form :model="form" label-width="90px" class="question-form">
        <!-- 基本信息 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><InfoFilled /></el-icon>
            基本信息
          </div>
          <el-row :gutter="16">
            <el-col :span="24">
              <el-form-item label="题目标题">
                <el-input v-model="form.title" placeholder="请输入题目标题" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="题干内容">
                <el-input type="textarea" v-model="form.content" placeholder="请输入题目内容" :rows="3" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="题型">
                <el-select v-model="form.type" placeholder="选择题型" style="width: 100%">
                  <el-option label="单选题" :value="1" />
                  <el-option label="多选题" :value="2" />
                  <el-option label="判断题" :value="3" />
                  <el-option label="编程题" :value="4" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="难度">
                <el-select v-model="form.difficulty" placeholder="选择难度" style="width: 100%">
                  <el-option label="简单" :value="1" />
                  <el-option label="中等" :value="2" />
                  <el-option label="困难" :value="3" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="分值">
                <el-input-number v-model="form.score" :min="1" :max="100" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="科目">
                <el-input v-model="form.subject" placeholder="如：高等数学" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 选择题选项 -->
        <div class="form-section" v-if="form.type === 1 || form.type === 2">
          <div class="section-title">
            <el-icon><List /></el-icon>
            选项设置
          </div>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="选项A">
                <el-input v-model="form.optionA" placeholder="请输入选项A内容" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="选项B">
                <el-input v-model="form.optionB" placeholder="请输入选项B内容" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="选项C">
                <el-input v-model="form.optionC" placeholder="请输入选项C内容" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="选项D">
                <el-input v-model="form.optionD" placeholder="请输入选项D内容" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="正确答案">
                <el-input v-model="form.correctOption" placeholder="单选填A/B/C/D，多选如ABCD" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 判断题答案 -->
        <div class="form-section" v-if="form.type === 3">
          <div class="section-title">
            <el-icon><Check /></el-icon>
            答案设置
          </div>
          <el-form-item label="正确答案">
            <el-radio-group v-model="form.judgeAnswer">
              <el-radio :label="1">正确</el-radio>
              <el-radio :label="0">错误</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>

        <!-- 编程题字段 -->
        <div class="form-section" v-if="form.type === 4">
          <div class="section-title">
            <el-icon><Monitor /></el-icon>
            编程题设置
          </div>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="输入描述">
                <el-input type="textarea" v-model="form.inputDesc" placeholder="描述输入格式" :rows="2" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="输出描述">
                <el-input type="textarea" v-model="form.outputDesc" placeholder="描述输出格式" :rows="2" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="样例输入">
                <el-input type="textarea" v-model="form.sampleInput" placeholder="用于自动评测的输入数据" :rows="3" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="样例输出">
                <el-input type="textarea" v-model="form.sampleOutput" placeholder="期望的正确输出" :rows="3" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitQuestion">
            <el-icon><Check /></el-icon>
            {{ isEdit ? '保存修改' : '确认添加' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Search, Plus, Edit, Delete, InfoFilled, List, Check, Monitor } from '@element-plus/icons-vue'

const questions = ref([])
const searchKeyword = ref('')
const typeFilter = ref(null)

// 筛选后的题目列表
const filteredQuestions = computed(() => {
  let result = questions.value
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    result = result.filter(q => 
      (q.title && q.title.toLowerCase().includes(kw)) ||
      (q.subject && q.subject.toLowerCase().includes(kw))
    )
  }
  if (typeFilter.value !== null && typeFilter.value !== '') {
    result = result.filter(q => q.type === typeFilter.value)
  }
  return result
})

const handleSearch = () => {
  // 搜索时自动触发computed重新计算
}
const dialogVisible = ref(false)
const isEdit = ref(false)

const form = ref({
  id: null,
  title: '',
  content: '',
  type: 1,
  score: 1,
  difficulty: 2,
  subject: '',
  optionA: '',
  optionB: '',
  optionC: '',
  optionD: '',
  correctOption: '',
  judgeAnswer: 1,
  inputDesc: '',
  outputDesc: '',
  sampleInput: '',
  sampleOutput: ''
})

const fetchQuestions = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/questions')
    if (!res.ok) {
      throw new Error('获取题目失败')
    }
    questions.value = await res.json()
  } catch (e) {
    ElMessage.error(e.message || '获取题目失败')
  }
}

const submitQuestion = async () => {
  try {
    const url = 'http://localhost:8081/api/questions'
    const method = isEdit.value ? 'PUT' : 'POST'
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form.value)
    })
    if (!res.ok) {
      throw new Error('新增题目失败')
    }
    ElMessage.success(isEdit.value ? '更新题目成功' : '新增题目成功')
    dialogVisible.value = false
    await fetchQuestions()
  } catch (e) {
    ElMessage.error(e.message || (isEdit.value ? '更新题目失败' : '新增题目失败'))
  }
}

const handleDelete = async (id) => {
  ElMessageBox.confirm('确定要删除该题目吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await fetch(`http://localhost:8081/api/questions/${id}`, {
        method: 'DELETE'
      })
      if (!res.ok) {
        throw new Error('删除题目失败')
      }
      ElMessage.success('删除题目成功')
      await fetchQuestions()
    } catch (e) {
      ElMessage.error(e.message || '删除题目失败')
    }
  }).catch(() => {})
}

const openEdit = (row) => {
  isEdit.value = true
  form.value = {
    id: row.id,
    title: row.title,
    content: row.content,
    type: row.type,
    score: row.score,
    difficulty: row.difficulty,
    subject: row.subject,
    optionA: row.optionA,
    optionB: row.optionB,
    optionC: row.optionC,
    optionD: row.optionD,
    correctOption: row.correctOption,
    judgeAnswer: row.judgeAnswer ?? 1,
    inputDesc: row.inputDesc || '',
    outputDesc: row.outputDesc || '',
    sampleInput: row.sampleInput || '',
    sampleOutput: row.sampleOutput || ''
  }
  dialogVisible.value = true
}

const openCreate = () => {
  isEdit.value = false
  form.value = {
    id: null,
    title: '',
    content: '',
    type: 1,
    score: 1,
    difficulty: 2,
    subject: '',
    optionA: '',
    optionB: '',
    optionC: '',
    optionD: '',
    correctOption: '',
    judgeAnswer: 1,
    inputDesc: '',
    outputDesc: '',
    sampleInput: '',
    sampleOutput: ''
  }
  dialogVisible.value = true
}

const renderType = (t) => {
  switch (t) {
    case 1: return '单选题'
    case 2: return '多选题'
    case 3: return '判断题'
    case 4: return '编程题'
    default: return '未知'
  }
}

const getTypeTagType = (t) => {
  switch (t) {
    case 1: return 'primary'
    case 2: return 'success'
    case 3: return 'warning'
    case 4: return 'danger'
    default: return 'info'
  }
}

const renderDifficulty = (d) => {
  switch (d) {
    case 1: return '简单'
    case 2: return '中等'
    case 3: return '困难'
    default: return '未知'
  }
}

const getDifficultyClass = (d) => {
  switch (d) {
    case 1: return 'easy'
    case 2: return 'medium'
    case 3: return 'hard'
    default: return ''
  }
}

onMounted(() => {
  fetchQuestions()
})
</script>

<style scoped>
.question-bank-container {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
}

.page-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  height: calc(100vh - 180px);
  min-height: 500px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 页面头部 */
.page-header {
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 24px;
  color: #fff;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.header-subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.15);
  padding: 4px 12px;
  border-radius: 12px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 200px;
}

.search-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.9);
  border: none;
  box-shadow: none;
}

.filter-select {
  width: 120px;
}

.filter-select :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.9);
  border: none;
  box-shadow: none;
}

.add-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  transition: all 0.3s;
}

.add-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
}

/* 表格区域 */
.table-wrapper {
  flex: 1;
  padding: 16px 20px;
  overflow: auto;
}

.question-table {
  border-radius: 8px;
  overflow: hidden;
}

.question-table :deep(.el-table__header th) {
  background: #f8f9fa;
  color: #333;
  font-weight: 500;
}

.question-table :deep(.el-table__row:hover > td) {
  background: #f0f5ff !important;
}

.title-cell {
  display: flex;
  align-items: center;
}

.question-title-text {
  font-weight: 500;
  color: #333;
}

.score-text {
  color: #667eea;
  font-weight: 600;
}

/* 难度标签 */
.difficulty-badge {
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

.difficulty-badge.easy {
  background: #e8f5e9;
  color: #4caf50;
}

.difficulty-badge.medium {
  background: #fff3e0;
  color: #ff9800;
}

.difficulty-badge.hard {
  background: #ffebee;
  color: #f44336;
}

/* 对话框样式 */
.question-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px 20px;
  margin: 0;
}

.question-dialog :deep(.el-dialog__title) {
  color: #fff;
  font-weight: 600;
}

.question-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #fff;
}

.question-dialog :deep(.el-dialog__body) {
  padding: 20px;
  max-height: 60vh;
  overflow-y: auto;
}

.form-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #e0e0e0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
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
