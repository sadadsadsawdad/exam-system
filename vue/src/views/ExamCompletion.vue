<template>
  <div class="exam-completion-container">
    <!-- 顶部统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-number">{{ gradedCount }}</div>
          <div class="stat-label">已批改</div>
        </div>
        <el-icon class="stat-icon" color="#67c23a"><CircleCheck /></el-icon>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-number">{{ passRate }}%</div>
          <div class="stat-label">及格率</div>
        </div>
        <el-icon class="stat-icon" color="#409eff"><TrendCharts /></el-icon>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-number">{{ excellentRate }}%</div>
          <div class="stat-label">优秀率</div>
        </div>
        <el-icon class="stat-icon" color="#e6a23c"><Trophy /></el-icon>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-number">{{ averageScore }}</div>
          <div class="stat-label">平均分</div>
        </div>
        <el-icon class="stat-icon" color="#667eea"><DataAnalysis /></el-icon>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-number">{{ maxScore }}</div>
          <div class="stat-label">最高分</div>
        </div>
        <el-icon class="stat-icon" color="#f56c6c"><Top /></el-icon>
      </el-card>
    </div>

    <div class="page-card">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <el-icon class="header-icon"><TrendCharts /></el-icon>
          <span class="header-title">成绩分析</span>
          <span class="header-subtitle">共 {{ filteredSheets.length }} 条记录</span>
        </div>
        <div class="header-right">
          <el-select v-model="examFilter" placeholder="选择考试" clearable class="filter-select" @change="handleSearch">
            <el-option v-for="exam in examList" :key="exam.id" :label="exam.title" :value="exam.id" />
          </el-select>
          <el-radio-group v-model="viewMode" size="small" class="view-mode-switch">
            <el-radio-button label="chart">图表</el-radio-button>
            <el-radio-button label="table">表格</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- 图表视图 -->
      <div v-if="viewMode === 'chart'" class="chart-view">
        <div class="chart-row">
          <!-- 班级平均分柱状图 -->
          <div class="chart-section">
            <div class="chart-title">
              <el-icon><Histogram /></el-icon>
              各班级平均分对比
            </div>
            <div ref="classAvgChartRef" class="chart-container"></div>
          </div>
          <!-- 成绩分段饼图 -->
          <div class="chart-section">
            <div class="chart-title">
              <el-icon><PieChart /></el-icon>
              成绩分段统计
            </div>
            <div ref="scoreRangeChartRef" class="chart-container"></div>
          </div>
        </div>

        <!-- 班级成绩排名 -->
        <div class="chart-section">
          <div class="chart-title">
            <el-icon><User /></el-icon>
            班级成绩排名
            <el-select v-model="selectedClassId" placeholder="选择班级" size="small" class="class-select" @change="updateStudentChart">
              <el-option v-for="clazz in clazzList" :key="clazz.id" :label="clazz.name" :value="clazz.id" />
            </el-select>
          </div>
          <div v-if="selectedClassId" class="score-table-container">
            <el-table :data="classStudentScores" stripe style="width: 100%" max-height="300">
              <el-table-column type="index" label="排名" width="70" align="center" />
              <el-table-column prop="username" label="学生姓名" min-width="120" align="center" />
              <el-table-column prop="score" label="成绩" width="100" align="center">
                <template #default="scope">
                  <span :class="getScoreClass(scope.row.score, scope.row.totalScore)">{{ scope.row.score }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="totalScore" label="满分" width="80" align="center" />
              <el-table-column label="得分率" width="100" align="center">
                <template #default="scope">
                  <el-progress :percentage="Math.round((scope.row.score / scope.row.totalScore) * 100)" :stroke-width="8" />
                </template>
              </el-table-column>
              <el-table-column label="等级" width="80" align="center">
                <template #default="scope">
                  <el-tag :type="getGradeType(scope.row.score, scope.row.totalScore)" size="small">
                    {{ getGrade(scope.row.score, scope.row.totalScore) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div v-else class="no-class-tip">
            <el-icon><InfoFilled /></el-icon>
            请选择一个班级查看成绩排名
          </div>
        </div>
      </div>

      <!-- 表格视图 -->
      <div v-else class="table-view">
        <div class="filter-bar">
          <el-select v-model="clazzFilter" placeholder="选择班级" clearable size="small" @change="handleSearch">
            <el-option v-for="clazz in clazzList" :key="clazz.id" :label="clazz.name" :value="clazz.id" />
          </el-select>
          <el-select v-model="statusFilter" placeholder="批改状态" clearable size="small" @change="handleSearch">
            <el-option label="已批改" value="GRADED" />
            <el-option label="待批改" value="PENDING" />
          </el-select>
        </div>
        <div class="table-wrapper">
          <el-table :data="paginatedSheets" class="completion-table" stripe>
            <el-table-column prop="id" label="ID" width="70" align="center" />
            <el-table-column prop="examTitle" label="考试名称" min-width="150">
              <template #default="scope">
                <span class="exam-title-text">{{ scope.row.examTitle }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="username" label="学生姓名" width="100" align="center" />
            <el-table-column prop="className" label="班级" width="120" align="center">
              <template #default="scope">
                <el-tag size="small" type="info">{{ scope.row.className || '-' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="得分" width="100" align="center">
              <template #default="scope">
                <span class="score-text" :class="getScoreClass(scope.row.score, scope.row.totalScore)">
                  {{ scope.row.score ?? '-' }} / {{ scope.row.totalScore || 0 }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="批改状态" width="100" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.gradingStatus === 'GRADED' ? 'success' : 'warning'" size="small">
                  {{ scope.row.gradingStatus === 'GRADED' ? '已批改' : '待批改' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="submitTime" label="提交时间" width="170" align="center" />
            <el-table-column label="操作" width="100" align="center" fixed="right">
              <template #default="scope">
                <el-button type="primary" link size="small" @click="viewDetail(scope.row)">
                  <el-icon><View /></el-icon> 详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-container">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 50]" :total="filteredSheets.length" layout="total, sizes, prev, pager, next" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
          </div>
        </div>
      </div>
    </div>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="答卷详情" width="600px" class="detail-dialog">
      <div v-if="currentSheet" class="detail-content">
        <div class="detail-info-grid">
          <div class="info-item"><span class="info-label">学生姓名</span><span class="info-value">{{ currentSheet.username }}</span></div>
          <div class="info-item"><span class="info-label">考试名称</span><span class="info-value">{{ currentSheet.examTitle }}</span></div>
          <div class="info-item"><span class="info-label">得分</span><span class="info-value score-highlight">{{ currentSheet.score }} / {{ currentSheet.totalScore }}</span></div>
          <div class="info-item"><span class="info-label">提交时间</span><span class="info-value">{{ currentSheet.submitTime }}</span></div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { TrendCharts, View, Histogram, User, PieChart, DataAnalysis, InfoFilled, CircleCheck, Top, Trophy } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const answerSheets = ref([])
const filteredSheets = ref([])
const clazzFilter = ref(null)
const examFilter = ref(null)
const statusFilter = ref('')
const clazzList = ref([])
const examList = ref([])
const detailDialogVisible = ref(false)
const currentSheet = ref(null)
const viewMode = ref('chart')
const selectedClassId = ref('')

const classAvgChartRef = ref(null)
const scoreRangeChartRef = ref(null)
let classAvgChart = null
let scoreRangeChart = null

const currentPage = ref(1)
const pageSize = ref(10)

// 统计数据
const gradedCount = computed(() => filteredSheets.value.filter(s => s.gradingStatus === 'GRADED' || s.status === 'ABSENT').length)
const pendingCount = computed(() => filteredSheets.value.filter(s => s.gradingStatus === 'PENDING' && s.status !== 'ABSENT').length)

const averageScore = computed(() => {
  // 包含所有有成绩的记录（已批改和缺考）
  const withScore = filteredSheets.value.filter(s => s.score != null && (s.gradingStatus === 'GRADED' || s.status === 'ABSENT'))
  if (withScore.length === 0) return '-'
  return (withScore.reduce((sum, s) => sum + (s.score || 0), 0) / withScore.length).toFixed(1)
})

const maxScore = computed(() => {
  const withScore = filteredSheets.value.filter(s => s.score != null && (s.gradingStatus === 'GRADED' || s.status === 'ABSENT'))
  if (withScore.length === 0) return '-'
  return Math.max(...withScore.map(s => s.score))
})

const minScore = computed(() => {
  const withScore = filteredSheets.value.filter(s => s.score != null && (s.gradingStatus === 'GRADED' || s.status === 'ABSENT'))
  if (withScore.length === 0) return '-'
  return Math.min(...withScore.map(s => s.score))
})

// 及格率
const passRate = computed(() => {
  const withScore = filteredSheets.value.filter(s => s.score != null && (s.gradingStatus === 'GRADED' || s.status === 'ABSENT'))
  if (withScore.length === 0) return 0
  const passCount = withScore.filter(s => (s.score / (s.totalScore || 100)) >= 0.6).length
  return Math.round((passCount / withScore.length) * 100)
})

// 优秀率
const excellentRate = computed(() => {
  const withScore = filteredSheets.value.filter(s => s.score != null && (s.gradingStatus === 'GRADED' || s.status === 'ABSENT'))
  if (withScore.length === 0) return 0
  const excellentCount = withScore.filter(s => (s.score / (s.totalScore || 100)) >= 0.9).length
  return Math.round((excellentCount / withScore.length) * 100)
})

const classStudentScores = computed(() => {
  if (!selectedClassId.value) return []
  // 显示所有有成绩的记录（包括已批改和缺考的）
  return filteredSheets.value
    .filter(s => s.classId === selectedClassId.value && s.score != null)
    .sort((a, b) => b.score - a.score)
})

const paginatedSheets = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredSheets.value.slice(start, start + pageSize.value)
})

const loadAnswerSheets = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/exam-results/grading')
    if (!res.ok) throw new Error('获取答卷列表失败')
    const result = await res.json()
    answerSheets.value = result.data || []
    filteredSheets.value = answerSheets.value
  } catch (e) {
    ElMessage.error(e.message || '获取答卷列表失败')
  }
}

const loadClazzList = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/clazz')
    if (res.ok) {
      const result = await res.json()
      clazzList.value = result.data || result || []
      // 默认选中第一个班级
      if (clazzList.value.length > 0 && !selectedClassId.value) {
        selectedClassId.value = clazzList.value[0].id
      }
    }
  } catch (e) {
    console.error('获取班级列表失败', e)
  }
}

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

const filterSheets = () => {
  let filtered = answerSheets.value
  if (clazzFilter.value) filtered = filtered.filter(s => s.classId === clazzFilter.value)
  if (examFilter.value) filtered = filtered.filter(s => s.examId === examFilter.value)
  if (statusFilter.value) filtered = filtered.filter(s => s.gradingStatus === statusFilter.value)
  filteredSheets.value = filtered
  if (viewMode.value === 'chart') nextTick(() => initCharts())
}

const getScoreClass = (score, totalScore) => {
  if (score == null || totalScore == null || totalScore === 0) return ''
  const ratio = score / totalScore
  if (ratio >= 0.9) return 'score-excellent'
  if (ratio >= 0.6) return 'score-pass'
  return 'score-fail'
}

const getGrade = (score, totalScore) => {
  if (score == null || totalScore == null || totalScore === 0) return '-'
  const ratio = score / totalScore
  if (ratio >= 0.9) return '优秀'
  if (ratio >= 0.8) return '良好'
  if (ratio >= 0.6) return '及格'
  return '不及格'
}

const getGradeType = (score, totalScore) => {
  if (score == null || totalScore == null || totalScore === 0) return 'info'
  const ratio = score / totalScore
  if (ratio >= 0.9) return 'success'
  if (ratio >= 0.8) return ''
  if (ratio >= 0.6) return 'warning'
  return 'danger'
}

const handleSearch = () => { currentPage.value = 1; filterSheets() }
const handleSizeChange = (val) => { pageSize.value = val; currentPage.value = 1 }
const handleCurrentChange = (val) => { currentPage.value = val }
const viewDetail = (sheet) => { currentSheet.value = sheet; detailDialogVisible.value = true }
const updateStudentChart = () => {}

const initCharts = () => {
  initClassAvgChart()
  initScoreRangeChart()
}

const initClassAvgChart = () => {
  if (!classAvgChartRef.value) return
  if (classAvgChart) classAvgChart.dispose()
  classAvgChart = echarts.init(classAvgChartRef.value)
  
  const classScores = {}
  // 包含所有有成绩的记录
  const withScore = filteredSheets.value.filter(s => s.score != null && (s.gradingStatus === 'GRADED' || s.status === 'ABSENT'))
  withScore.forEach(sheet => {
    const className = sheet.className || '未分班'
    if (!classScores[className]) classScores[className] = { total: 0, count: 0 }
    classScores[className].total += sheet.score
    classScores[className].count++
  })
  
  const classNames = Object.keys(classScores)
  const avgScores = classNames.map(name => (classScores[name].total / classScores[name].count).toFixed(1))
  
  classAvgChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: classNames, axisLabel: { interval: 0, rotate: classNames.length > 5 ? 30 : 0 } },
    yAxis: { type: 'value', name: '平均分' },
    series: [{
      name: '平均分', type: 'bar', data: avgScores,
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#667eea' }, { offset: 1, color: '#764ba2' }]) },
      label: { show: true, position: 'top', formatter: '{c}分' }
    }]
  })
}

const initScoreRangeChart = () => {
  if (!scoreRangeChartRef.value) return
  if (scoreRangeChart) scoreRangeChart.dispose()
  scoreRangeChart = echarts.init(scoreRangeChartRef.value)
  
  // 包含所有有成绩的记录
  const withScore = filteredSheets.value.filter(s => s.score != null && (s.gradingStatus === 'GRADED' || s.status === 'ABSENT'))
  let excellent = 0, good = 0, pass = 0, fail = 0
  withScore.forEach(s => {
    const ratio = s.score / (s.totalScore || 100)
    if (ratio >= 0.9) excellent++
    else if (ratio >= 0.8) good++
    else if (ratio >= 0.6) pass++
    else fail++
  })
  
  scoreRangeChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
    legend: { orient: 'vertical', right: '5%', top: 'center' },
    series: [{
      name: '成绩分布', type: 'pie', radius: ['40%', '70%'], center: ['40%', '50%'],
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: [
        { value: excellent, name: '优秀(≥90%)', itemStyle: { color: '#67c23a' } },
        { value: good, name: '良好(80-89%)', itemStyle: { color: '#409eff' } },
        { value: pass, name: '及格(60-79%)', itemStyle: { color: '#e6a23c' } },
        { value: fail, name: '不及格(<60%)', itemStyle: { color: '#f56c6c' } }
      ]
    }]
  })
}

watch(viewMode, (newVal) => { if (newVal === 'chart') nextTick(() => initCharts()) })
watch(filteredSheets, () => { if (viewMode.value === 'chart') nextTick(() => initCharts()) })

onMounted(async () => {
  await loadAnswerSheets()
  await loadClazzList()
  await loadExamList()
  nextTick(() => { if (viewMode.value === 'chart') initCharts() })
})
</script>

<style scoped>
.exam-completion-container { padding: 20px; height: 100%; box-sizing: border-box; }

/* 统计卡片 */
.stats-cards { display: flex; gap: 16px; margin-bottom: 20px; }
.stat-card { flex: 1; }
.stat-card :deep(.el-card__body) { display: flex; justify-content: space-between; align-items: center; padding: 16px; }
.stat-content { display: flex; flex-direction: column; }
.stat-number { font-size: 28px; font-weight: bold; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
.stat-icon { font-size: 36px; opacity: 0.8; }

.page-card { background: #fff; border-radius: 12px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08); height: calc(100vh - 280px); min-height: 400px; display: flex; flex-direction: column; overflow: hidden; }

.page-header { padding: 16px 24px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px; }
.header-left { display: flex; align-items: center; gap: 12px; }
.header-icon { font-size: 24px; color: #fff; }
.header-title { font-size: 18px; font-weight: 600; color: #fff; }
.header-subtitle { font-size: 13px; color: rgba(255, 255, 255, 0.8); background: rgba(255, 255, 255, 0.15); padding: 4px 12px; border-radius: 12px; }
.header-right { display: flex; align-items: center; gap: 12px; }
.filter-select { width: 140px; }
.filter-select :deep(.el-input__wrapper) { background: rgba(255, 255, 255, 0.9); border: none; box-shadow: none; }
.view-mode-switch :deep(.el-radio-button__inner) { background: rgba(255, 255, 255, 0.9); border: none; color: #666; }
.view-mode-switch :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) { background: #fff; color: #667eea; box-shadow: none; }

.chart-view { flex: 1; padding: 20px; overflow: auto; display: flex; flex-direction: column; gap: 20px; }
.chart-row { display: flex; gap: 20px; }
.chart-row .chart-section { flex: 1; }
.chart-section { background: #fff; border-radius: 10px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06); padding: 16px; }
.chart-title { display: flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 600; color: #333; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid #eee; }
.chart-title .el-icon { color: #667eea; }
.class-select { margin-left: auto; width: 150px; }
.chart-container { height: 250px; }
.score-table-container { max-height: 300px; overflow: auto; }
.no-class-tip { display: flex; align-items: center; justify-content: center; gap: 8px; height: 200px; color: #909399; font-size: 14px; }
.no-class-tip .el-icon { font-size: 18px; }

.score-excellent { color: #67c23a; font-weight: 600; }
.score-pass { color: #409eff; font-weight: 600; }
.score-fail { color: #f56c6c; font-weight: 600; }

.table-view { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.filter-bar { padding: 12px 20px; background: #f8f9fa; display: flex; gap: 12px; border-bottom: 1px solid #eee; }
.table-wrapper { flex: 1; padding: 16px 20px; overflow: auto; }
.completion-table { border-radius: 8px; overflow: hidden; }
.completion-table :deep(.el-table__header th) { background: #f8f9fa; color: #333; font-weight: 500; }
.completion-table :deep(.el-table__row:hover > td) { background: #f0f5ff !important; }
.exam-title-text { font-weight: 500; color: #333; }
.score-text { font-weight: 600; }
.pagination-container { display: flex; justify-content: flex-end; padding: 16px 0; border-top: 1px solid #ebeef5; margin-top: 12px; }

.detail-dialog :deep(.el-dialog__header) { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 16px 20px; margin: 0; }
.detail-dialog :deep(.el-dialog__title) { color: #fff; font-weight: 600; }
.detail-dialog :deep(.el-dialog__headerbtn .el-dialog__close) { color: #fff; }
.detail-content { padding: 10px; }
.detail-info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%); padding: 20px; border-radius: 8px; }
.info-item { display: flex; flex-direction: column; gap: 4px; }
.info-label { font-size: 12px; color: #909399; }
.info-value { font-size: 15px; color: #333; font-weight: 500; }
.score-highlight { color: #667eea; font-size: 20px; font-weight: 700; }
</style>
