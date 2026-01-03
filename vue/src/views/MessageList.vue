<template>
  <div class="message-list">
    <div class="page-card">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <el-icon class="header-icon"><Message /></el-icon>
          <span class="header-title">消息管理</span>
          <span class="header-subtitle">共 {{ filteredList.length }} 条消息</span>
        </div>
        <div class="header-right">
          <el-select v-model="searchForm.scope" placeholder="发布范围" clearable class="filter-select" @change="handleSearch">
            <el-option label="全部用户" value="all" />
            <el-option label="班级" value="class" />
          </el-select>
          <el-select v-model="searchForm.status" placeholder="发布状态" clearable class="filter-select" @change="handleSearch">
            <el-option label="已发布" value="published" />
            <el-option label="未发布" value="draft" />
          </el-select>
          <el-input 
            v-model="searchForm.sender" 
            placeholder="搜索发送人" 
            clearable 
            class="search-input"
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>

      <!-- 表格区域 -->
      <div class="table-wrapper">
        <el-table :data="paginatedList" class="message-table" stripe>
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="scope" label="发布范围" width="100" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="row.scope === 'all' ? 'primary' : 'success'">
                {{ getScopeName(row.scope) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" min-width="150">
            <template #default="{ row }">
              <span class="title-text">{{ row.title }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="content-text">{{ row.content }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="sender" label="发送人" width="100" align="center" />
          <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
          <el-table-column label="发布状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'published' ? 'success' : 'info'" size="small">
                {{ row.status === 'published' ? '已发布' : '未发布' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleView(row)">
                <el-icon><View /></el-icon> 查看
              </el-button>
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
          :total="filteredList.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 消息详情对话框 -->
    <el-dialog v-model="dialogVisible" title="消息详情" width="600px" class="message-dialog">
      <div class="message-detail">
        <div class="detail-header">
          <h3 class="detail-title">{{ currentMessage.title }}</h3>
          <div class="detail-meta">
            <span><el-icon><User /></el-icon> {{ currentMessage.sender }}</span>
            <span><el-icon><Clock /></el-icon> {{ currentMessage.createTime }}</span>
            <el-tag :type="currentMessage.status === 'published' ? 'success' : 'info'" size="small">
              {{ currentMessage.status === 'published' ? '已发布' : '未发布' }}
            </el-tag>
          </div>
        </div>
        <div class="detail-content">
          {{ currentMessage.content }}
        </div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Message, Search, View, User, Clock } from '@element-plus/icons-vue'

const searchForm = reactive({
  scope: '',
  status: '',
  sender: ''
})

const messageList = ref([])
const filteredList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const currentMessage = ref({})

// 计算分页后的数据
const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredList.value.slice(start, end)
})

// 加载消息列表
const loadMessages = async () => {
  try {
    const params = new URLSearchParams()
    if (searchForm.scope) params.append('scope', searchForm.scope)
    if (searchForm.status) params.append('status', searchForm.status)
    if (searchForm.sender) params.append('sender', searchForm.sender)
    
    const url = `http://localhost:8081/api/messages${params.toString() ? '?' + params.toString() : ''}`
    const res = await fetch(url)
    if (!res.ok) {
      throw new Error('获取消息列表失败')
    }
    const result = await res.json()
    messageList.value = result.data || []
    filteredList.value = messageList.value
  } catch (e) {
    ElMessage.error(e.message || '获取消息列表失败')
  }
}

// 获取发布范围名称
const getScopeName = (scope) => {
  const scopeMap = {
    'all': '全部',
    'class': '班级'
  }
  return scopeMap[scope] || scope || '-'
}

const handleSearch = () => {
  currentPage.value = 1
  loadMessages()
}

const handleReset = () => {
  searchForm.scope = ''
  searchForm.status = ''
  searchForm.sender = ''
  currentPage.value = 1
  loadMessages()
}

const handleSizeChange = () => {
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

const handleView = (row) => {
  currentMessage.value = row
  dialogVisible.value = true
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.message-list {
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
  width: 160px;
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

/* 表格区域 */
.table-wrapper {
  flex: 1;
  padding: 16px 20px;
  overflow: auto;
}

.message-table {
  border-radius: 8px;
  overflow: hidden;
}

.message-table :deep(.el-table__header th) {
  background: #f8f9fa;
  color: #333;
  font-weight: 500;
}

.message-table :deep(.el-table__row:hover > td) {
  background: #f0f5ff !important;
}

.title-text {
  font-weight: 500;
  color: #333;
}

.content-text {
  color: #666;
}

/* 分页容器 */
.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
}

/* 消息详情对话框 */
.message-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px 20px;
  margin: 0;
}

.message-dialog :deep(.el-dialog__title) {
  color: #fff;
  font-weight: 600;
}

.message-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #fff;
}

.message-detail {
  padding: 10px;
}

.detail-header {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.detail-title {
  margin: 0 0 12px 0;
  font-size: 18px;
  color: #333;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 13px;
  color: #666;
}

.detail-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.detail-content {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
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
