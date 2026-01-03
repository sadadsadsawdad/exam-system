<template>
  <div class="clazz-manage">
    <div class="layout-container">
      <!-- 左侧班级列表 -->
      <div class="left-panel">
        <div class="panel-header">
          <span class="panel-title">班级列表</span>
          <el-button type="primary" size="small" class="add-btn" @click="openAddDialog">
            <el-icon><Plus /></el-icon>
            添加
          </el-button>
        </div>
        <div class="clazz-list" v-loading="loading">
          <div 
            v-for="clazz in clazzList" 
            :key="clazz.id" 
            :class="['clazz-item', { active: currentClazz.id === clazz.id }]"
            @click="viewStudents(clazz)"
          >
            <div class="clazz-info">
              <span class="clazz-name">{{ clazz.name }}</span>
              <span class="clazz-count">{{ clazz.studentCount || 0 }}人</span>
            </div>
            <div class="clazz-actions">
              <el-icon class="action-icon edit" @click.stop="openEditDialog(clazz)"><Edit /></el-icon>
              <el-icon class="action-icon delete" @click.stop="handleDelete(clazz)"><Delete /></el-icon>
            </div>
          </div>
          <div v-if="clazzList.length === 0 && !loading" class="empty-tip">
            <el-icon class="empty-icon"><FolderOpened /></el-icon>
            <p>暂无班级</p>
          </div>
        </div>
      </div>

      <!-- 右侧学生列表 -->
      <div class="right-panel">
        <div class="panel-header" v-if="currentClazz.id">
          <span class="panel-title student-title">{{ currentClazz.name }}</span>
          <el-button type="primary" size="small" class="add-btn" @click="openAddMemberDialog">
            <el-icon><Plus /></el-icon>
            添加成员
          </el-button>
        </div>
        <div class="panel-header" v-else>
          <span class="panel-title hint-title">请选择班级查看学生</span>
        </div>

        <div class="student-table-wrapper" v-if="currentClazz.id">
          <el-table :data="studentList" style="width: 100%" v-loading="studentLoading" class="student-table">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" width="200" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="role" label="角色" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.role === '2' ? 'danger' : 'success'" size="small">
                  {{ scope.row.role === '2' ? '管理员' : '学生' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div v-if="currentClazz.id && studentList.length === 0 && !studentLoading" class="empty-tip">
          <el-icon class="empty-icon"><User /></el-icon>
          <p>该班级暂无学生</p>
        </div>
        <div v-if="!currentClazz.id" class="empty-tip large">
          <el-icon class="empty-icon"><FolderOpened /></el-icon>
          <p>点击左侧班级查看学生列表</p>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑班级' : '添加班级'" width="500px" :close-on-click-modal="false">
      <el-form ref="clazzFormRef" :model="clazzForm" :rules="clazzRules" label-width="80px">
        <el-form-item label="班级名称" prop="name">
          <el-input v-model="clazzForm.name" placeholder="请输入班级名称" />
        </el-form-item>
        <el-form-item label="班级描述" prop="description">
          <el-input v-model="clazzForm.description" type="textarea" :rows="3" placeholder="请输入班级描述（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">{{ isEdit ? '保存' : '添加' }}</el-button>
      </template>
    </el-dialog>

    <!-- 添加成员弹窗 -->
    <el-dialog v-model="memberDialogVisible" title="添加成员" width="500px" :close-on-click-modal="false">
      <el-form ref="memberFormRef" :model="memberForm" :rules="memberRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="memberForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="memberForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="memberDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddMember" :loading="memberSubmitLoading">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, FolderOpened, User } from '@element-plus/icons-vue'

const loading = ref(false)
const submitLoading = ref(false)
const clazzList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const clazzFormRef = ref(null)

// 学生列表相关
const studentLoading = ref(false)
const studentList = ref([])
const currentClazz = ref({ id: null, name: '' })

// 添加成员相关
const memberDialogVisible = ref(false)
const memberFormRef = ref(null)
const memberSubmitLoading = ref(false)
const memberForm = reactive({
  username: '',
  password: ''
})
const memberRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const clazzForm = reactive({
  id: null,
  name: '',
  description: ''
})

const clazzRules = {
  name: [
    { required: true, message: '请输入班级名称', trigger: 'blur' },
    { min: 2, max: 50, message: '班级名称长度应在2-50个字符之间', trigger: 'blur' }
  ]
}

const loadClazzes = async () => {
  loading.value = true
  try {
    const res = await fetch('http://localhost:8081/api/clazz')
    if (res.ok) {
      const result = await res.json()
      clazzList.value = result.data || result || []
    } else {
      ElMessage.error('获取班级列表失败')
    }
  } catch (e) {
    ElMessage.error('网络错误，请检查服务器连接')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  isEdit.value = false
  Object.assign(clazzForm, { id: null, name: '', description: '' })
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  Object.assign(clazzForm, { id: row.id, name: row.name, description: row.description || '' })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!clazzFormRef.value) return
  await clazzFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const url = isEdit.value ? `http://localhost:8081/api/clazz/${clazzForm.id}` : 'http://localhost:8081/api/clazz'
      const method = isEdit.value ? 'PUT' : 'POST'
      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: clazzForm.name, description: clazzForm.description || null })
      })
      const result = await res.json()
      if (res.ok && result.code === 200) {
        ElMessage.success(isEdit.value ? '班级更新成功' : '班级添加成功')
        dialogVisible.value = false
        loadClazzes()
      } else {
        ElMessage.error(result.message || '操作失败')
      }
    } catch (e) {
      ElMessage.error('网络错误')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除班级 "${row.name}" 吗？`, '删除班级', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'danger'
  }).then(async () => {
    try {
      const res = await fetch(`http://localhost:8081/api/clazz/${row.id}`, { method: 'DELETE' })
      const result = await res.json()
      if (res.ok && result.code === 200) {
        ElMessage.success('班级删除成功')
        loadClazzes()
      } else {
        ElMessage.error(result.message || '删除失败')
      }
    } catch (e) {
      ElMessage.error('网络错误')
    }
  }).catch(() => {})
}

// 查看班级学生
const viewStudents = async (clazz) => {
  currentClazz.value = { id: clazz.id, name: clazz.name }
  studentLoading.value = true
  try {
    const res = await fetch(`http://localhost:8081/api/clazz/${clazz.id}/students`)
    if (res.ok) {
      const result = await res.json()
      studentList.value = result.data || result || []
    } else {
      ElMessage.error('获取学生列表失败')
      studentList.value = []
    }
  } catch (e) {
    ElMessage.error('网络错误')
    studentList.value = []
  } finally {
    studentLoading.value = false
  }
}

// 打开添加成员弹窗
const openAddMemberDialog = () => {
  memberForm.username = ''
  memberForm.password = ''
  memberDialogVisible.value = true
}

// 添加成员到班级（创建新用户并加入班级）
const handleAddMember = async () => {
  if (!memberFormRef.value) return
  await memberFormRef.value.validate(async (valid) => {
    if (!valid) return
    memberSubmitLoading.value = true
    try {
      // 创建新用户并直接分配到当前班级
      const res = await fetch('http://localhost:8081/api/users', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username: memberForm.username,
          password: memberForm.password,
          role: '1',
          classId: currentClazz.value.id
        })
      })
      const result = await res.json()
      if (res.ok && (result.code === 200 || result.code === '200')) {
        ElMessage.success('成员添加成功')
        memberDialogVisible.value = false
        viewStudents(currentClazz.value)
        loadClazzes()
      } else {
        ElMessage.error(result.message || '添加失败')
      }
    } catch (e) {
      ElMessage.error('网络错误')
    } finally {
      memberSubmitLoading.value = false
    }
  })
}

onMounted(() => { loadClazzes() })
</script>

<style scoped>
.clazz-manage {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
}

.layout-container {
  display: flex;
  gap: 20px;
  height: calc(100vh - 180px);
  min-height: 500px;
}

/* 左侧面板 */
.left-panel {
  width: 300px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 右侧面板 */
.right-panel {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 面板头部 */
.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
}

.hint-title {
  color: rgba(255, 255, 255, 0.8);
  font-weight: 400;
}

.student-title::before {
  content: '👥';
  margin-right: 8px;
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

/* 班级列表 */
.clazz-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.clazz-item {
  padding: 14px 16px;
  border-radius: 10px;
  cursor: pointer;
  margin-bottom: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
  background: #fafafa;
}

.clazz-item:hover {
  background: #f0f5ff;
  border-color: #667eea;
  transform: translateX(4px);
}

.clazz-item.active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
}

.clazz-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.clazz-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.clazz-count {
  font-size: 12px;
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  padding: 2px 8px;
  border-radius: 10px;
}

.clazz-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.clazz-item:hover .clazz-actions {
  opacity: 1;
}

.action-icon {
  font-size: 16px;
  padding: 6px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-icon.edit {
  color: #667eea;
}

.action-icon.edit:hover {
  background: rgba(102, 126, 234, 0.1);
}

.action-icon.delete {
  color: #f56c6c;
}

.action-icon.delete:hover {
  background: rgba(245, 108, 108, 0.1);
}

/* 学生表格区域 */
.student-table-wrapper {
  flex: 1;
  padding: 16px;
  overflow: auto;
}

.student-table {
  border-radius: 8px;
  overflow: hidden;
}

.student-table :deep(.el-table__header th) {
  background: #f8f9fa;
  color: #333;
  font-weight: 500;
}

.student-table :deep(.el-table__row:hover > td) {
  background: #f0f5ff !important;
}

/* 空状态 */
.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
}

.empty-tip.large {
  flex: 1;
}

.empty-icon {
  font-size: 48px;
  color: #d9d9d9;
  margin-bottom: 16px;
}

.empty-tip p {
  margin: 0;
  font-size: 14px;
}

/* 滚动条美化 */
.clazz-list::-webkit-scrollbar,
.student-table-wrapper::-webkit-scrollbar {
  width: 6px;
}

.clazz-list::-webkit-scrollbar-thumb,
.student-table-wrapper::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 3px;
}

.clazz-list::-webkit-scrollbar-track,
.student-table-wrapper::-webkit-scrollbar-track {
  background: transparent;
}
</style>
