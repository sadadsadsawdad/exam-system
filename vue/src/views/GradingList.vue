<template>
  <div class="grading-list-container">
    <div class="page-card">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <el-icon class="header-icon"><EditPen /></el-icon>
          <span class="header-title">答卷管理</span>
          <span class="header-subtitle">共 {{ filteredSheets.length }} 份答卷</span>
        </div>
        <div class="header-right">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索学生或考试"
            clearable
            class="search-input"
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select
            v-model="clazzFilter"
            placeholder="选择班级"
            clearable
            class="filter-select"
            @change="handleSearch"
          >
            <el-option
              v-for="clazz in clazzList"
              :key="clazz.id"
              :label="clazz.name"
              :value="clazz.id"
            />
          </el-select>
          <el-select
            v-model="statusFilter"
            placeholder="批改状态"
            clearable
            class="filter-select"
            @change="handleSearch"
          >
            <el-option label="待批改" value="PENDING" />
            <el-option label="已批改" value="GRADED" />
          </el-select>
          <el-select
            v-model="examFilter"
            placeholder="选择考试"
            clearable
            class="filter-select"
            @change="handleSearch"
          >
            <el-option
              v-for="exam in examList"
              :key="exam.id"
              :label="exam.title"
              :value="exam.id"
            />
          </el-select>
        </div>
      </div>

      <!-- 表格区域 -->
      <div class="table-wrapper">
        <el-table :data="paginatedSheets" class="grading-table" stripe>
          <el-table-column prop="username" label="学生姓名" width="100" align="center" />
          <el-table-column prop="className" label="班级" width="120" align="center">
            <template #default="scope">
              <el-tag size="small" type="info">{{ scope.row.className || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="examTitle" label="考试名称" min-width="150">
            <template #default="scope">
              <span class="exam-title-text">{{ scope.row.examTitle }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="submitTime" label="提交时间" width="170" align="center" />
          <el-table-column label="得分" width="100" align="center">
            <template #default="scope">
              <span v-if="scope.row.score !== null" class="score-text">
                {{ scope.row.score }} / {{ scope.row.totalScore }}
              </span>
              <span v-else class="score-pending">待批改</span>
            </template>
          </el-table-column>
          <el-table-column prop="gradingStatus" label="批改状态" width="100" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.gradingStatus === 'PENDING'" type="warning" size="small">待批改</el-tag>
              <el-tag v-else-if="scope.row.gradingStatus === 'GRADED'" type="success" size="small">已批改</el-tag>
              <el-tag v-else type="info" size="small">{{ scope.row.gradingStatus || '未知' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="gradedBy" label="批改人" width="100" align="center">
            <template #default="scope">
              <span v-if="scope.row.gradedBy">{{ scope.row.gradedBy }}</span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="scope">
              <el-button
                v-if="scope.row.gradingStatus === 'PENDING'"
                type="warning"
                size="small"
                @click="openGradingDialog(scope.row)"
              >
                <el-icon><Edit /></el-icon> 批改
              </el-button>
              <el-button
                v-else-if="scope.row.gradingStatus === 'GRADED'"
                type="info"
                size="small"
                plain
                @click="openGradingDialog(scope.row)"
              >
                <el-icon><View /></el-icon> 查看
              </el-button>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredSheets.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 批改对话框 -->
    <el-dialog
      v-model="gradingDialogVisible"
      title="批改答卷"
      width="900px"
      @close="closeGradingDialog"
    >
      <div v-if="currentSheet" class="grading-dialog-content">
        <div class="sheet-info">
          <p><strong>学生：</strong>{{ currentSheet.username }}</p>
          <p><strong>考试：</strong>{{ currentSheet.examTitle }}</p>
          <p><strong>提交时间：</strong>{{ currentSheet.submitTime }}</p>
        </div>

        <el-divider />

        <div v-if="answerDetails.length > 0" class="answer-list">
          <div
            v-for="(answer, index) in answerDetails"
            :key="index"
            class="answer-item"
          >
            <div class="answer-header">
              <span class="question-title">{{ index + 1 }}. {{ answer.questionTitle }}</span>
              <span class="question-type">{{ getQuestionTypeName(answer.questionType) }}</span>
              <span class="question-score">{{ answer.questionScore }}分</span>
            </div>

            <div class="answer-content">
              <div class="answer-row">
                <strong>学生答案：</strong>
                <span v-if="answer.questionType === 4" class="code-answer">
                  <pre>{{ answer.userAnswer }}</pre>
                </span>
                <span v-else>{{ answer.userAnswer || '未作答' }}</span>
              </div>

              <div class="answer-row">
                <strong>正确答案：</strong>
                <span>{{ answer.correctAnswer || '-' }}</span>
              </div>

              <!-- 客观题显示自动判分结果 -->
              <div v-if="answer.questionType !== 4" class="answer-row">
                <strong>判分结果：</strong>
                <el-tag v-if="answer.isCorrect" type="success">正确 ({{ answer.earnedScore }}分)</el-tag>
                <el-tag v-else type="danger">错误 (0分)</el-tag>
              </div>

              <!-- 主观题（编程题）提供分数输入 -->
              <div v-if="answer.questionType === 4" class="grading-input">
                <el-form-item label="得分">
                  <el-input-number
                    v-model="answer.manualScore"
                    :min="0"
                    :max="answer.questionScore"
                    size="small"
                  />
                </el-form-item>
                <el-form-item label="评语">
                  <el-input
                    v-model="answer.comment"
                    type="textarea"
                    :rows="2"
                    placeholder="请输入评语"
                  />
                </el-form-item>
              </div>
            </div>
          </div>
        </div>
        <div v-else style="color: #999; text-align: center; padding: 20px;">
          暂无答题详情
        </div>
      </div>

      <template #footer>
        <el-button @click="gradingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveGrading" :loading="saving">保存批改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { EditPen, Search, Edit, View } from '@element-plus/icons-vue'

const answerSheets = ref([])
const filteredSheets = ref([])
const searchKeyword = ref('')
const statusFilter = ref('')
const examFilter = ref(null)
const clazzFilter = ref(null)
const examList = ref([])
const clazzList = ref([])
const gradingDialogVisible = ref(false)
const currentSheet = ref(null)
const answerDetails = ref([])
const saving = ref(false)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 计算分页后的数据
const paginatedSheets = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredSheets.value.slice(start, end)
})

// 加载答卷列表
const loadAnswerSheets = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/exam-results/grading')
    if (!res.ok) {
      throw new Error('获取答卷列表失败')
    }
    const result = await res.json()
    answerSheets.value = result.data || []
    filteredSheets.value = answerSheets.value
  } catch (e) {
    ElMessage.error(e.message || '获取答卷列表失败')
  }
}

// 加载考试列表
const loadExamList = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/exams')
    if (res.ok) {
      const result = await res.json()
      examList.value = result.data || result || []
    }
  } catch (e) {
    console.error('获取考试列表失败', e)
  }
}

// 加载班级列表
const loadClazzList = async () => {
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

// 筛选答卷
const filterSheets = () => {
  let filtered = answerSheets.value

  // 按关键词筛选
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(sheet => {
      const username = (sheet.username || '').toLowerCase()
      const examTitle = (sheet.examTitle || '').toLowerCase()
      return username.includes(kw) || examTitle.includes(kw)
    })
  }

  // 按状态筛选
  if (statusFilter.value) {
    filtered = filtered.filter(sheet => sheet.gradingStatus === statusFilter.value)
  }

  // 按考试筛选
  if (examFilter.value) {
    filtered = filtered.filter(sheet => sheet.examId === examFilter.value)
  }

  // 按班级筛选
  if (clazzFilter.value) {
    filtered = filtered.filter(sheet => sheet.classId === clazzFilter.value)
  }

  filteredSheets.value = filtered
}

// 搜索时重置页码
const handleSearch = () => {
  currentPage.value = 1
  filterSheets()
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

// 页码改变
const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 打开批改对话框
const openGradingDialog = async (sheet) => {
  currentSheet.value = sheet
  gradingDialogVisible.value = true

  // 获取答卷详情
  try {
    const res = await fetch(`http://localhost:8081/api/exam-results/${sheet.id}/detail`)
    if (!res.ok) {
      throw new Error('获取答卷详情失败')
    }
    const result = await res.json()
    const detail = result.data

    // 解析答题详情
    if (detail.answerDetail) {
      let parsed = JSON.parse(detail.answerDetail)
      // 兼容两种格式：直接数组或包含answerDetails的对象
      if (Array.isArray(parsed)) {
        answerDetails.value = parsed
      } else if (parsed.answerDetails) {
        answerDetails.value = parsed.answerDetails
      } else {
        answerDetails.value = []
      }
      
      // 为主观题初始化手动评分字段
      answerDetails.value.forEach(answer => {
        if (answer.questionType === 4) {
          answer.manualScore = answer.earnedScore || 0
          answer.comment = ''
        }
      })
    }
  } catch (e) {
    ElMessage.error(e.message || '获取答卷详情失败')
  }
}

// 关闭批改对话框
const closeGradingDialog = () => {
  currentSheet.value = null
  answerDetails.value = []
}

// 保存批改
const saveGrading = async () => {
  saving.value = true
  try {
    // 验证分数
    for (const answer of answerDetails.value) {
      if (answer.questionType === 4) {
        if (answer.manualScore > answer.questionScore) {
          ElMessage.error(`题目"${answer.questionTitle}"的得分不能超过满分${answer.questionScore}分`)
          saving.value = false
          return
        }
        if (answer.manualScore < 0) {
          ElMessage.error(`题目"${answer.questionTitle}"的得分不能小于0分`)
          saving.value = false
          return
        }
      }
    }

    // 获取当前管理员用户名
    let gradedBy = 'admin'
    const userStr = sessionStorage.getItem('exam_user')
    if (userStr) {
      try {
        const user = JSON.parse(userStr)
        gradedBy = user.username || 'admin'
      } catch (e) {}
    }

    // 构建批改数据
    const gradingData = {
      sheetId: currentSheet.value.id,
      gradedBy: gradedBy,
      answers: answerDetails.value.map(answer => ({
        questionId: answer.questionId,
        score: answer.questionType === 4 ? (answer.manualScore || 0) : (answer.earnedScore || 0),
        comment: answer.comment || ''
      }))
    }

    const res = await fetch('http://localhost:8081/api/grading', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(gradingData)
    })

    if (!res.ok) {
      const result = await res.json()
      throw new Error(result.message || '保存批改失败')
    }

    ElMessage.success('批改保存成功')
    gradingDialogVisible.value = false
    await loadAnswerSheets()
  } catch (e) {
    ElMessage.error(e.message || '保存批改失败')
  } finally {
    saving.value = false
  }
}

// 获取题型名称
const getQuestionTypeName = (type) => {
  switch (type) {
    case 1: return '单选题'
    case 2: return '多选题'
    case 3: return '判断题'
    case 4: return '编程题'
    default: return '未知'
  }
}

onMounted(() => {
  loadAnswerSheets()
  loadExamList()
  loadClazzList()
})
</script>

<style scoped>
.grading-list-container {
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
  width: 180px;
}

.search-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.9);
  border: none;
  box-shadow: none;
}

.filter-select {
  width: 130px;
}

.filter-select :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.9);
  border: none;
  box-shadow: none;
}

/* 表格区域 */
.table-wrapper {
  flex: 1;
  padding: 16px 20px;
  overflow: auto;
}

.grading-table {
  border-radius: 8px;
  overflow: hidden;
}

.grading-table :deep(.el-table__header th) {
  background: #f8f9fa;
  color: #333;
  font-weight: 500;
}

.grading-table :deep(.el-table__row:hover > td) {
  background: #f0f5ff !important;
}

.exam-title-text {
  font-weight: 500;
  color: #333;
}

.score-text {
  color: #667eea;
  font-weight: 600;
}

.score-pending {
  color: #e6a23c;
  font-weight: 500;
}

.text-muted {
  color: #c0c4cc;
  font-size: 13px;
}

/* 分页容器 */
.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
}

/* 对话框样式 */
.grading-dialog-content {
  max-height: 600px;
  overflow-y: auto;
}

.sheet-info {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
}

.sheet-info p {
  margin: 5px 0;
}

.answer-list {
  margin-top: 20px;
}

.answer-item {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
}

.answer-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.question-title {
  flex: 1;
  font-weight: 600;
  font-size: 14px;
}

.question-type {
  margin-right: 10px;
  padding: 2px 8px;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 3px;
  font-size: 12px;
}

.question-score {
  color: #f56c6c;
  font-weight: 600;
}

.answer-content {
  margin-top: 10px;
}

.answer-row {
  margin-bottom: 10px;
  line-height: 1.6;
}

.code-answer pre {
  background: #1e1e1e;
  color: #e0e0e0;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  font-family: Consolas, 'Courier New', monospace;
  font-size: 13px;
}

.grading-input {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #dcdfe6;
}
</style>
