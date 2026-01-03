<template>
  <div class="exam-completion-container">
    <div class="page-card">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <el-icon class="header-icon"><TrendCharts /></el-icon>
          <span class="header-title">成绩分析</span>
          <span class="header-subtitle">共 {{ filteredSheets.length }} 条记录</span>
        </div>
        <div class="header-right">
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
          <el-radio-group v-model="viewMode" size="small" class="view-mode-switch">
            <el-radio-button label="chart">图表</el-radio-button>
            <el-radio-button label="table">表格</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- 图表视图 -->
      <div v-if="viewMode === 'chart'" class="chart-view">
        <!-- 班级平均分柱状图 -->
        <div class="chart-section">
          <div class="chart-title">
            <el-icon><Histogram /></el-icon>
            各班级平均分对比
          </div>
          <div ref="classAvgChartRef" class="chart-container"></div>
        </div>

        <!-- 班级成绩排名 -->
        <div class="chart-section">
          <div class="chart-title">
            <el-icon><User /></el-icon>
            班级成绩排名
            <el-select v-model="selectedClassId" placeholder="选择班级" size="small" class="class-select" @change="updateStudentChart">
              <el-option
                v-for="clazz in clazzList"
                :key="clazz.id"
                :label="clazz.name"
                :value="clazz.id"
              />
            </el-select>
          </div>
          <div v-if="selectedClassId" class="score-table-container">
            <el-table :data="classStudentScores" stripe style="width: 100%" max-height="280">
              <el-table-column type="index" label="排名" width="70" align="center" />
              <el-table-column prop="username" label="学生姓名" min-width="120" align="center" />
              <el-table-column prop="score" label="成绩" width="100" align="center">
                <template #default="scope">
                  <span :class="getScoreClass(scope.row.score, scope.row.totalScore)">
                    {{ scope.row.score }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="totalScore" label="满分" width="80" align="center" />
              <el-table-column label="得分率" width="100" align="center">
                <template #default="scope">
                  <span>{{ ((scope.row.score / scope.row.totalScore) * 100).toFixed(1) }}%</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div v-else class="no-class-tip">
            <el-icon><InfoFilled /></el-icon>
            请选择一个班级查看成绩排名
          </div>
        </div>

        <!-- 成绩分段统计和统计概览 - 同一行 -->
        <div class="chart-row">
          <div class="chart-section half">
            <div class="chart-title">
              <el-icon><PieChart /></el-icon>
              成绩分段统计
            </div>
            <div ref="scoreRangeChartRef" class="chart-container-small"></div>
          </div>

          <!-- 统计数据卡片 -->
          <div class="chart-section half stats-section">
          <div class="chart-title">
            <el-icon><DataAnalysis /></el-icon>
            统计概览
          </div>
          <div class="stats-grid">
            <div class="stats-item">
              <span class="stats-label">答卷总数</span>
              <span class="stats-value blue">{{ filteredSheets.length }}</span>
            </div>
            <div class="stats-item">
              <span class="stats-label">已批改</span>
              <span class="stats-value green">{{ gradedCount }}</span>
            </div>
            <div class="stats-item">
              <span class="stats-label">待批改</span>
              <span class="stats-value orange">{{ pendingCount }}</span>
            </div>
            <div class="stats-item">
              <span class="stats-label">平均分</span>
              <span class="stats-value purple">{{ averageScore }}</span>
            </div>
            <div class="stats-item">
              <span class="stats-label">最高分</span>
              <span class="stats-value red">{{ maxScore }}</span>
            </div>
            <div class="stats-item">
              <span class="stats-label">最低分</span>
              <span class="stats-value gray">{{ minScore }}</span>
            </div>
          </div>
          </div>
        </div>
      </div>

      <!-- 表格视图 -->
      <div v-else class="table-view">
        <!-- 筛选栏 -->
        <div class="filter-bar">
          <el-select
            v-model="clazzFilter"
            placeholder="选择班级"
            clearable
            size="small"
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
            size="small"
            @change="handleSearch"
          >
            <el-option label="已批改" value="GRADED" />
            <el-option label="待批改" value="PENDING" />
          </el-select>
        </div>

        <!-- 表格区域 -->
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
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="scope">
              <el-button type="primary" link size="small" @click="viewDetail(scope.row)">
                <el-icon><View /></el-icon> 详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

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
      </div>
    </div>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="答卷详情"
      width="800px"
      class="detail-dialog"
    >
      <div v-if="currentSheet" class="detail-content">
        <div class="detail-header">
          <div class="detail-info-grid">
            <div class="info-item">
              <span class="info-label">学生姓名</span>
              <span class="info-value">{{ currentSheet.username }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">考试名称</span>
              <span class="info-value">{{ currentSheet.examTitle }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">得分</span>
              <span class="info-value score-highlight">{{ currentSheet.score }} / {{ currentSheet.totalScore }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">提交时间</span>
              <span class="info-value">{{ currentSheet.submitTime }}</span>
            </div>
          </div>
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
import { TrendCharts, View, Histogram, User, PieChart, DataAnalysis, InfoFilled } from '@element-plus/icons-vue'
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

// 图表引用
const classAvgChartRef = ref(null)
const scoreRangeChartRef = ref(null)
let classAvgChart = null
let scoreRangeChart = null

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 统计数据
const gradedCount = computed(() => {
  return filteredSheets.value.filter(s => s.gradingStatus === 'GRADED').length
})

const pendingCount = computed(() => {
  return filteredSheets.value.filter(s => s.gradingStatus === 'PENDING').length
})

const averageScore = computed(() => {
  const graded = filteredSheets.value.filter(s => s.gradingStatus === 'GRADED' && s.score != null)
  if (graded.length === 0) return '-'
  const total = graded.reduce((sum, s) => sum + (s.score || 0), 0)
  return (total / graded.length).toFixed(1)
})

const maxScore = computed(() => {
  const graded = filteredSheets.value.filter(s => s.gradingStatus === 'GRADED' && s.score != null)
  if (graded.length === 0) return '-'
  return Math.max(...graded.map(s => s.score))
})

const minScore = computed(() => {
  const graded = filteredSheets.value.filter(s => s.gradingStatus === 'GRADED' && s.score != null)
  if (graded.length === 0) return '-'
  return Math.min(...graded.map(s => s.score))
})

// 班级学生成绩排名（从高到低）
const classStudentScores = computed(() => {
  if (!selectedClassId.value) return []
  const classSheets = filteredSheets.value.filter(
    s => s.classId === selectedClassId.value && s.gradingStatus === 'GRADED' && s.score != null
  )
  return classSheets.sort((a, b) => b.score - a.score)
})

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

  // 按班级筛选
  if (clazzFilter.value) {
    filtered = filtered.filter(sheet => sheet.classId === clazzFilter.value)
  }

  // 按考试筛选
  if (examFilter.value) {
    filtered = filtered.filter(sheet => sheet.examId === examFilter.value)
  }

  // 按状态筛选
  if (statusFilter.value) {
    filtered = filtered.filter(sheet => sheet.gradingStatus === statusFilter.value)
  }

  filteredSheets.value = filtered
  
  // 更新图表
  if (viewMode.value === 'chart') {
    nextTick(() => {
      initCharts()
    })
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

// 获取分数样式类
const getScoreClass = (score, totalScore) => {
  if (score == null || totalScore == null || totalScore === 0) return ''
  const ratio = score / totalScore
  if (ratio >= 0.9) return 'score-excellent'
  if (ratio >= 0.6) return 'score-pass'
  return 'score-fail'
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

// 格式化耗时
const formatDuration = (seconds) => {
  if (!seconds || seconds <= 0) return '-'
  if (seconds < 60) return `${seconds}秒`
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  if (minutes < 60) {
    return secs > 0 ? `${minutes}分钟${secs}秒` : `${minutes}分钟`
  }
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return `${hours}小时${mins}分钟`
}

// 查看详情
const viewDetail = (sheet) => {
  currentSheet.value = sheet
  detailDialogVisible.value = true
}

// 打印
const printSheet = (sheet) => {
  ElMessage.info('打印功能开发中...')
}

// 初始化图表
const initCharts = () => {
  initClassAvgChart()
  initScoreRangeChart()
}

// 更新学生成绩表（选择班级时调用）
const updateStudentChart = () => {
  // 表格数据通过计算属性自动更新，无需额外操作
}

// 班级平均分柱状图
const initClassAvgChart = () => {
  if (!classAvgChartRef.value) return
  
  if (classAvgChart) {
    classAvgChart.dispose()
  }
  classAvgChart = echarts.init(classAvgChartRef.value)
  
  // 按班级分组计算平均分
  const classScores = {}
  const graded = filteredSheets.value.filter(s => s.gradingStatus === 'GRADED' && s.score != null)
  
  graded.forEach(sheet => {
    const className = sheet.className || '未分班'
    if (!classScores[className]) {
      classScores[className] = { total: 0, count: 0 }
    }
    classScores[className].total += sheet.score
    classScores[className].count++
  })
  
  const classNames = Object.keys(classScores)
  const avgScores = classNames.map(name => 
    (classScores[name].total / classScores[name].count).toFixed(1)
  )
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: classNames,
      axisLabel: { interval: 0, rotate: classNames.length > 5 ? 30 : 0 }
    },
    yAxis: {
      type: 'value',
      name: '平均分'
    },
    series: [{
      name: '平均分',
      type: 'bar',
      data: avgScores,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#667eea' },
          { offset: 1, color: '#764ba2' }
        ])
      },
      label: {
        show: true,
        position: 'top',
        formatter: '{c}分'
      }
    }]
  }
  
  classAvgChart.setOption(option)
}

// 成绩分段饼图
const initScoreRangeChart = () => {
  if (!scoreRangeChartRef.value) return
  
  if (scoreRangeChart) {
    scoreRangeChart.dispose()
  }
  scoreRangeChart = echarts.init(scoreRangeChartRef.value)
  
  const graded = filteredSheets.value.filter(s => s.gradingStatus === 'GRADED' && s.score != null)
  const total = filteredSheets.value[0]?.totalScore || 100
  
  let excellent = 0, good = 0, pass = 0, fail = 0
  graded.forEach(s => {
    const ratio = s.score / total
    if (ratio >= 0.9) excellent++
    else if (ratio >= 0.8) good++
    else if (ratio >= 0.6) pass++
    else fail++
  })
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}人 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center'
    },
    series: [{
      name: '成绩分布',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold'
        }
      },
      data: [
        { value: excellent, name: '优秀(≥90%)', itemStyle: { color: '#67c23a' } },
        { value: good, name: '良好(80-89%)', itemStyle: { color: '#409eff' } },
        { value: pass, name: '及格(60-79%)', itemStyle: { color: '#e6a23c' } },
        { value: fail, name: '不及格(<60%)', itemStyle: { color: '#f56c6c' } }
      ]
    }]
  }
  
  scoreRangeChart.setOption(option)
}

// 监听视图模式变化
watch(viewMode, (newVal) => {
  if (newVal === 'chart') {
    nextTick(() => {
      initCharts()
    })
  }
})

// 监听筛选数据变化
watch(filteredSheets, () => {
  if (viewMode.value === 'chart') {
    nextTick(() => {
      initCharts()
    })
  }
})

onMounted(async () => {
  await loadAnswerSheets()
  await loadClazzList()
  await loadExamList()
  
  // 初始化图表
  nextTick(() => {
    if (viewMode.value === 'chart') {
      initCharts()
    }
  })
})
</script>

<style scoped>
.exam-completion-container {
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

.filter-select {
  width: 140px;
}

.filter-select :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.9);
  border: none;
  box-shadow: none;
}

/* 视图模式切换 */
.view-mode-switch :deep(.el-radio-button__inner) {
  background: rgba(255, 255, 255, 0.9);
  border: none;
  color: #666;
}

.view-mode-switch :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: #fff;
  color: #667eea;
  box-shadow: none;
}

/* 图表视图 */
.chart-view {
  flex: 1;
  padding: 20px;
  overflow: auto;
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-content: flex-start;
}

.chart-section {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 16px;
  width: 100%;
}

.chart-row {
  display: flex;
  gap: 20px;
  width: 100%;
}

.chart-section.half {
  flex: 1;
  width: auto;
}

.chart-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.chart-title .el-icon {
  color: #667eea;
}

.class-select {
  margin-left: auto;
  width: 150px;
}

.chart-container {
  height: 280px;
}

.chart-container-small {
  height: 220px;
}

/* 统计概览 */
.stats-section {
  display: flex;
  flex-direction: column;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  flex: 1;
}

.stats-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.stats-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.stats-value {
  font-size: 24px;
  font-weight: 700;
}

.stats-value.blue { color: #667eea; }
.stats-value.green { color: #67c23a; }
.stats-value.orange { color: #e6a23c; }
.stats-value.purple { color: #409eff; }
.stats-value.red { color: #f56c6c; }
.stats-value.gray { color: #909399; }

/* 表格视图 */
.table-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.filter-bar {
  padding: 12px 20px;
  background: #f8f9fa;
  display: flex;
  gap: 12px;
  border-bottom: 1px solid #eee;
}

/* 班级成绩表格 */
.score-table-container {
  max-height: 280px;
  overflow: auto;
}

.no-class-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 200px;
  color: #909399;
  font-size: 14px;
}

.no-class-tip .el-icon {
  font-size: 18px;
}

.score-excellent {
  color: #67c23a;
  font-weight: 600;
}

.score-pass {
  color: #409eff;
  font-weight: 600;
}

.score-fail {
  color: #f56c6c;
  font-weight: 600;
}

/* 统计卡片 */
.stats-row {
  display: flex;
  gap: 20px;
  padding: 20px 24px;
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
}

.stat-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  padding: 16px 20px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.stat-icon.blue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.stat-icon.green {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: #fff;
}

.stat-icon.orange {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #fff;
}

.stat-icon.purple {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: #fff;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

/* 表格区域 */
.table-wrapper {
  flex: 1;
  padding: 16px 20px;
  overflow: auto;
}

.completion-table {
  border-radius: 8px;
  overflow: hidden;
}

.completion-table :deep(.el-table__header th) {
  background: #f8f9fa;
  color: #333;
  font-weight: 500;
}

.completion-table :deep(.el-table__row:hover > td) {
  background: #f0f5ff !important;
}

.exam-title-text {
  font-weight: 500;
  color: #333;
}

.score-text {
  font-weight: 600;
}

.score-text.score-excellent {
  color: #67c23a;
}

.score-text.score-pass {
  color: #409eff;
}

.score-text.score-fail {
  color: #f56c6c;
}

/* 分页容器 */
.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
}

/* 详情对话框 */
.detail-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px 20px;
  margin: 0;
}

.detail-dialog :deep(.el-dialog__title) {
  color: #fff;
  font-weight: 600;
}

.detail-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #fff;
}

.detail-content {
  padding: 10px;
}

.detail-header {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  padding: 20px;
  border-radius: 8px;
}

.detail-info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: #909399;
}

.info-value {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}

.score-highlight {
  color: #667eea;
  font-size: 20px;
  font-weight: 700;
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
