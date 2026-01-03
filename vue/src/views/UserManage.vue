<template>
  <div class="user-manage">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-number">{{ statistics.totalCount }}</div>
          <div class="stat-label">总用户数</div>
        </div>
        <el-icon class="stat-icon" color="#409eff"><User /></el-icon>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-number">{{ statistics.studentCount }}</div>
          <div class="stat-label">学生数量</div>
        </div>
        <el-icon class="stat-icon" color="#67c23a"><UserFilled /></el-icon>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-number">{{ statistics.adminCount }}</div>
          <div class="stat-label">管理员数量</div>
        </div>
        <el-icon class="stat-icon" color="#e6a23c"><Avatar /></el-icon>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-number">{{ statistics.activeCount }}</div>
          <div class="stat-label">已启用</div>
        </div>
        <el-icon class="stat-icon" color="#67c23a"><CircleCheck /></el-icon>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-number">{{ statistics.disabledCount }}</div>
          <div class="stat-label">已禁用</div>
        </div>
        <el-icon class="stat-icon" color="#f56c6c"><CircleClose /></el-icon>
      </el-card>
    </div>

    <!-- 搜索和操作栏 -->
    <div class="toolbar">
      <div class="search-area">
        <el-input
          v-model="searchUsername"
          placeholder="搜索用户名"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="searchRole" placeholder="选择角色" clearable style="width: 140px; margin-left: 10px">
          <el-option label="全部" value="" />
          <el-option label="普通用户" value="1" />
          <el-option label="管理员" value="2" />
        </el-select>
        <el-select v-model="searchStatus" placeholder="选择状态" clearable style="width: 140px; margin-left: 10px">
          <el-option label="全部" value="" />
          <el-option label="已启用" :value="1" />
          <el-option label="已禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch" style="margin-left: 10px">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
      <div class="action-area">
        <el-button type="success" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          添加用户
        </el-button>
        <el-button type="primary" @click="handleBatchEnable" :disabled="selectedUsers.length === 0">
          <el-icon><CircleCheck /></el-icon>
          批量启用
        </el-button>
        <el-button type="warning" @click="handleBatchDisable" :disabled="selectedUsers.length === 0">
          <el-icon><CircleClose /></el-icon>
          批量禁用
        </el-button>
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedUsers.length === 0">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 用户列表表格 -->
    <el-table 
      :data="userList" 
      stripe 
      style="width: 100%" 
      v-loading="loading"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="150" />
      <el-table-column prop="email" label="邮箱" width="180">
        <template #default="scope">
          {{ scope.row.email || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="130">
        <template #default="scope">
          {{ scope.row.phone || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="className" label="班级" width="120">
        <template #default="scope">
          <span>{{ scope.row.className || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="role" label="角色" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.role === '2' || scope.row.role === 2 ? 'danger' : 'primary'">
            {{ getRoleName(scope.row.role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
            @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160">
        <template #default="scope">
          {{ scope.row.createTime || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="lastLoginTime" label="最后登录" width="160">
        <template #default="scope">
          {{ scope.row.lastLoginTime || '从未登录' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" @click="openEditDialog(scope.row)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button size="small" type="warning" @click="handleResetPassword(scope.row)">
            <el-icon><Key /></el-icon>
            重置密码
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '添加用户'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱（选填）" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号（选填）" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通用户" value="1" />
            <el-option label="管理员" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" prop="classId">
          <el-select v-model="userForm.classId" placeholder="请选择班级" clearable style="width: 100%">
            <el-option v-for="c in clazzList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isEdit" label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          {{ isEdit ? '保存' : '添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Key, User, UserFilled, Avatar, CircleCheck, CircleClose } from '@element-plus/icons-vue'

// 状态
const loading = ref(false)
const submitLoading = ref(false)
const userList = ref([])
const searchUsername = ref('')
const searchRole = ref('')
const searchStatus = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const userFormRef = ref(null)
const selectedUsers = ref([])

// 统计数据
const statistics = reactive({
  totalCount: 0,
  studentCount: 0,
  adminCount: 0,
  activeCount: 0,
  disabledCount: 0
})

// 班级列表
const clazzList = ref([])

// 表单数据
const userForm = reactive({
  id: null,
  username: '',
  email: '',
  phone: '',
  password: '',
  role: '1',
  classId: null,
  status: 1
})

// 表单验证规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在6-20个字符之间', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 获取角色名称
const getRoleName = (role) => {
  if (role === '2' || role === 2) return '管理员'
  return '普通用户'
}

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    let url = 'http://localhost:8081/api/users'
    const params = new URLSearchParams()
    if (searchUsername.value) params.append('username', searchUsername.value)
    if (searchRole.value) params.append('role', searchRole.value)
    if (searchStatus.value !== '' && searchStatus.value !== null) params.append('status', searchStatus.value)
    if (params.toString()) url += '/search?' + params.toString()

    const res = await fetch(url)
    if (res.ok) {
      const result = await res.json()
      userList.value = result.data || result || []
    } else {
      ElMessage.error('获取用户列表失败')
    }
  } catch (e) {
    ElMessage.error('网络错误，请检查服务器连接')
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/users/statistics')
    if (res.ok) {
      const result = await res.json()
      if (result.data) {
        Object.assign(statistics, result.data)
      }
    }
  } catch (e) {
    // 忽略错误
  }
}

// 搜索
const handleSearch = () => {
  loadUsers()
}

// 重置搜索
const resetSearch = () => {
  searchUsername.value = ''
  searchRole.value = ''
  searchStatus.value = ''
  loadUsers()
}

// 处理表格选择变化
const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

// 加载班级列表
const loadClazzes = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/clazz')
    if (res.ok) {
      const result = await res.json()
      clazzList.value = result.data || result || []
    }
  } catch (e) {
    // 忽略错误
  }
}

// 打开添加对话框
const openAddDialog = () => {
  isEdit.value = false
  Object.assign(userForm, {
    id: null,
    username: '',
    email: '',
    phone: '',
    password: '',
    role: '1',
    classId: null,
    status: 1
  })
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (row) => {
  isEdit.value = true
  Object.assign(userForm, {
    id: row.id,
    username: row.username,
    email: row.email || '',
    phone: row.phone || '',
    password: '',
    role: String(row.role),
    classId: row.classId || null,
    status: row.status !== undefined ? row.status : 1
  })
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!userFormRef.value) return

  await userFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      const url = isEdit.value
        ? `http://localhost:8081/api/users/${userForm.id}`
        : 'http://localhost:8081/api/users'
      const method = isEdit.value ? 'PUT' : 'POST'

      const body = {
        username: userForm.username,
        email: userForm.email || null,
        phone: userForm.phone || null,
        role: userForm.role,
        classId: userForm.classId || null,
        status: userForm.status
      }
      if (!isEdit.value) {
        body.password = userForm.password
      }

      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      })

      const result = await res.json()
      if (res.ok && result.code === 200) {
        ElMessage.success(isEdit.value ? '用户更新成功' : '用户添加成功')
        dialogVisible.value = false
        loadUsers()
      } else {
        ElMessage.error(result.message || '操作失败')
      }
    } catch (e) {
      ElMessage.error('网络错误，请检查服务器连接')
    } finally {
      submitLoading.value = false
    }
  })
}

// 重置密码
const handleResetPassword = (row) => {
  ElMessageBox.confirm(
    `确定要重置用户 "${row.username}" 的密码为 123456 吗？`,
    '重置密码',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await fetch(`http://localhost:8081/api/users/${row.id}/reset-password`, {
        method: 'PUT'
      })
      const result = await res.json()
      if (res.ok && result.code === 200) {
        ElMessage.success('密码已重置为 123456')
      } else {
        ElMessage.error(result.message || '重置密码失败')
      }
    } catch (e) {
      ElMessage.error('网络错误')
    }
  }).catch(() => {})
}

// 删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除用户 "${row.username}" 吗？此操作不可恢复！`,
    '删除用户',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'danger'
    }
  ).then(async () => {
    try {
      const res = await fetch(`http://localhost:8081/api/users/${row.id}`, {
        method: 'DELETE'
      })
      const result = await res.json()
      if (res.ok && result.code === 200) {
        ElMessage.success('用户删除成功')
        loadUsers()
      } else {
        ElMessage.error(result.message || '删除失败')
      }
    } catch (e) {
      ElMessage.error('网络错误')
    }
  }).catch(() => {})
}

// 处理状态切换
const handleStatusChange = async (row) => {
  try {
    const res = await fetch(`http://localhost:8081/api/users/${row.id}/status`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: row.status })
    })
    const result = await res.json()
    if (res.ok && result.code === 200) {
      ElMessage.success(row.status === 1 ? '用户已启用' : '用户已禁用')
      loadStatistics()
    } else {
      ElMessage.error(result.message || '操作失败')
      // 恢复原状态
      row.status = row.status === 1 ? 0 : 1
    }
  } catch (e) {
    ElMessage.error('网络错误')
    row.status = row.status === 1 ? 0 : 1
  }
}

// 批量启用
const handleBatchEnable = () => {
  ElMessageBox.confirm(
    `确定要启用选中的 ${selectedUsers.value.length} 个用户吗？`,
    '批量启用',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      const ids = selectedUsers.value.map(u => u.id)
      const res = await fetch('http://localhost:8081/api/users/batch/status', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ids, status: 1 })
      })
      const result = await res.json()
      if (res.ok && result.code === 200) {
        ElMessage.success('批量启用成功')
        loadUsers()
        loadStatistics()
      } else {
        ElMessage.error(result.message || '操作失败')
      }
    } catch (e) {
      ElMessage.error('网络错误')
    }
  }).catch(() => {})
}

// 批量禁用
const handleBatchDisable = () => {
  ElMessageBox.confirm(
    `确定要禁用选中的 ${selectedUsers.value.length} 个用户吗？`,
    '批量禁用',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const ids = selectedUsers.value.map(u => u.id)
      const res = await fetch('http://localhost:8081/api/users/batch/status', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ids, status: 0 })
      })
      const result = await res.json()
      if (res.ok && result.code === 200) {
        ElMessage.success('批量禁用成功')
        loadUsers()
        loadStatistics()
      } else {
        ElMessage.error(result.message || '操作失败')
      }
    } catch (e) {
      ElMessage.error('网络错误')
    }
  }).catch(() => {})
}

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedUsers.value.length} 个用户吗？此操作不可恢复！`,
    '批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'danger'
    }
  ).then(async () => {
    try {
      const ids = selectedUsers.value.map(u => u.id)
      const res = await fetch('http://localhost:8081/api/users/batch', {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ids })
      })
      const result = await res.json()
      if (res.ok && result.code === 200) {
        ElMessage.success('批量删除成功')
        loadUsers()
        loadStatistics()
      } else {
        ElMessage.error(result.message || '操作失败')
      }
    } catch (e) {
      ElMessage.error('网络错误')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadUsers()
  loadClazzes()
  loadStatistics()
})
</script>

<style scoped>
.user-manage {
  padding: 0;
}

.stats-cards {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.stat-card {
  flex: 1;
  min-width: 150px;
  max-width: 200px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.stat-icon {
  font-size: 40px;
  opacity: 0.8;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
  background: #fff;
  padding: 16px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.search-area {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.action-area {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .stats-cards {
    flex-direction: column;
  }
  
  .stat-card {
    max-width: 100%;
  }
  
  .toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .action-area {
    margin-top: 10px;
  }
}
</style>
