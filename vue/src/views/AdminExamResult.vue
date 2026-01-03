<template>
  <div class="admin-exam-result">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>考试成绩管理 / 查看代码</span>
        </div>
      </template>

      <el-table :data="results" style="width: 100%">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="examTitle" label="考试名称" />
        <el-table-column prop="score" label="得分" width="80" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="交卷时间" width="180" />
        <el-table-column label="编程代码" width="120">
          <template #default="scope">
            <el-button
              v-if="scope.row.codeAnswer"
              type="primary"
              link
              size="small"
              @click="showCode(scope.row)"
            >
              查看代码
            </el-button>
            <span v-else style="color: #999; font-size: 12px;">无</span>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog
        v-model="codeDialogVisible"
        title="编程题代码"
        width="800px"
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

const results = ref([])
const codeDialogVisible = ref(false)
const currentCodes = ref([])

const loadResults = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/exam-results/all')
    if (!res.ok) {
      throw new Error('获取考试成绩失败')
    }
    results.value = await res.json()
  } catch (e) {
    ElMessage.error(e.message || '获取考试成绩失败')
  }
}

const showCode = (row) => {
  currentCodes.value = []
  if (row && row.codeAnswer) {
    try {
      const arr = JSON.parse(row.codeAnswer)
      if (Array.isArray(arr)) {
        currentCodes.value = arr.map(c => (c || '').trim()).filter(c => c)
      }
    } catch (e) {
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
.admin-exam-result {
  padding: 0 4px;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.code-item {
  margin-bottom: 16px;
}

.code-item-title {
  font-weight: 600;
  margin-bottom: 6px;
}

.code-view {
  max-height: 320px;
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
