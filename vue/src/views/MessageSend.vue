<template>
  <div class="message-send">
    <div class="page-card">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <el-icon class="header-icon"><Promotion /></el-icon>
          <span class="header-title">消息发送</span>
        </div>
      </div>

      <!-- 表单区域 -->
      <div class="form-wrapper">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="message-form">
          <div class="form-section">
            <div class="section-title">
              <el-icon><Setting /></el-icon>
              发布设置
            </div>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="发布范围" prop="scope">
                  <el-select v-model="form.scope" placeholder="请选择发布范围" style="width: 100%" @change="handleScopeChange">
                    <el-option label="全部用户" value="all" />
                    <el-option label="指定班级" value="class" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12" v-if="form.scope === 'class'">
                <el-form-item label="选择班级" prop="classId">
                  <el-select v-model="form.classId" placeholder="请选择班级" style="width: 100%">
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
              <el-icon><EditPen /></el-icon>
              消息内容
            </div>
            <el-form-item label="消息标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入消息标题" maxlength="100" show-word-limit />
            </el-form-item>
            <el-form-item label="消息内容" prop="content">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="10"
                placeholder="请输入消息内容..."
                maxlength="2000"
                show-word-limit
              />
            </el-form-item>
          </div>

          <div class="form-actions">
            <el-button @click="handleReset">
              <el-icon><RefreshLeft /></el-icon> 重置
            </el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">
              <el-icon><Promotion /></el-icon> 发送消息
            </el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Promotion, Setting, EditPen, RefreshLeft } from '@element-plus/icons-vue'

const formRef = ref(null)
const submitting = ref(false)
const clazzList = ref([])

const form = reactive({
  scope: '',
  classId: null,
  title: '',
  content: ''
})

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

// 发布范围改变时清空班级选择
const handleScopeChange = () => {
  form.classId = null
}

const rules = {
  scope: [{ required: true, message: '请选择发布范围', trigger: 'change' }],
  classId: [{ required: true, message: '请选择班级', trigger: 'change' }],
  title: [{ required: true, message: '请输入消息标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入消息内容', trigger: 'blur' }]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // 获取当前用户信息
        const userStr = sessionStorage.getItem('user')
        const user = userStr ? JSON.parse(userStr) : {}
        
        const messageData = {
          scope: form.scope,
          classId: form.scope === 'class' ? form.classId : null,
          title: form.title,
          content: form.content,
          sender: user.username || 'admin',
          senderId: user.id || null,
          status: 'published'
        }
        
        const res = await fetch('http://localhost:8081/api/messages', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(messageData)
        })
        
        if (!res.ok) {
          throw new Error('发送失败')
        }
        
        const result = await res.json()
        if (result.code === 200) {
          ElMessage.success('消息发送成功')
          handleReset()
        } else {
          throw new Error(result.message || '发送失败')
        }
      } catch (e) {
        ElMessage.error(e.message || '消息发送失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleReset = () => {
  form.scope = ''
  form.classId = null
  form.title = ''
  form.content = ''
  formRef.value?.resetFields()
}

onMounted(() => {
  loadClazzList()
})
</script>

<style scoped>
.message-send {
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

/* 表单区域 */
.form-wrapper {
  flex: 1;
  padding: 24px;
  overflow: auto;
}

.message-form {
  max-width: 800px;
  margin: 0 auto;
}

.form-section {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px dashed #e0e0e0;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 20px;
}

.form-actions .el-button {
  min-width: 120px;
  height: 40px;
}

/* 滚动条美化 */
.form-wrapper::-webkit-scrollbar {
  width: 6px;
}

.form-wrapper::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 3px;
}

.form-wrapper::-webkit-scrollbar-track {
  background: transparent;
}
</style>
