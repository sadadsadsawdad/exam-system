<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <el-button 
            v-if="isAdmin" 
            type="primary" 
            link 
            @click="goBack"
            style="margin-right: 10px;"
          >
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <span>个人信息</span>
        </div>
      </template>

      <div class="profile-content">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <el-avatar :size="120" :src="avatarUrl" class="user-avatar">
              <el-icon :size="60"><User /></el-icon>
            </el-avatar>
            <el-upload
              class="avatar-uploader"
              action="#"
              :show-file-list="false"
              :http-request="customUpload"
              :before-upload="beforeAvatarUpload"
              accept="image/*"
            >
              <el-button type="primary" size="small" class="upload-btn">
                <el-icon><Upload /></el-icon>
                更换头像
              </el-button>
            </el-upload>
          </div>
          <div class="avatar-tips">
            <p>支持 JPG、PNG 格式</p>
            <p>建议尺寸 200x200 像素</p>
          </div>
        </div>

        <!-- 基本信息 -->
        <el-divider content-position="left">基本信息</el-divider>
        
        <el-form
          ref="profileFormRef"
          :model="profileForm"
          label-width="100px"
          class="profile-form"
        >
          <el-form-item label="用户名">
            <el-input v-model="profileForm.username" disabled>
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
            <div class="form-tip">用户名不可修改</div>
          </el-form-item>

          <el-form-item label="角色">
            <el-tag :type="profileForm.role === '2' ? 'danger' : 'primary'" size="large">
              {{ profileForm.role === '2' ? '管理员' : '学生' }}
            </el-tag>
          </el-form-item>

          <el-form-item label="班级" v-if="profileForm.role !== '2'">
            <el-input :value="profileForm.className || '未分配班级'" disabled>
              <template #prefix>
                <el-icon><School /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="创建时间">
            <el-input :value="profileForm.createTime || '-'" disabled>
              <template #prefix>
                <el-icon><Calendar /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="最后登录">
            <el-input :value="profileForm.lastLoginTime || '从未登录'" disabled>
              <template #prefix>
                <el-icon><Clock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
        </el-form>

        <!-- 修改密码 -->
        <el-divider content-position="left">修改密码</el-divider>

        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-width="100px"
          class="password-form"
        >
          <el-form-item label="原密码" prop="oldPassword">
            <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入原密码"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码（至少6位）"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-button type="warning" @click="handleChangePassword" :loading="changingPassword">
              <el-icon><Key /></el-icon>
              修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Upload, Calendar, Clock, Lock, Key, School, ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()

// 用户ID
const userId = ref(null)

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 头像URL
const avatarUrl = computed(() => {
  return profileForm.avatar || defaultAvatar
})

// 保存状态
const changingPassword = ref(false)

// 表单引用
const profileFormRef = ref(null)
const passwordFormRef = ref(null)

// 是否是管理员
const isAdmin = computed(() => profileForm.role === '2')

// 返回上一页
const goBack = () => {
  router.push('/admin')
}

// 个人信息表单
const profileForm = reactive({
  username: '',
  avatar: '',
  role: '',
  className: '',
  createTime: '',
  lastLoginTime: ''
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码验证规则
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 加载用户信息
const loadUserProfile = async () => {
  if (!userId.value) return
  
  try {
    // 使用普通用户接口获取信息
    const res = await fetch(`http://localhost:8081/api/users/${userId.value}`)
    if (res.ok) {
      const result = await res.json()
      if (result.code === 200 && result.data) {
        const user = result.data
        profileForm.username = user.username || ''
        profileForm.avatar = user.avatar || ''
        profileForm.role = String(user.role) || '1'
        profileForm.className = user.className || ''
        profileForm.createTime = user.createTime || ''
        profileForm.lastLoginTime = user.lastLoginTime || ''
        
        // 更新sessionStorage中的用户信息
        const userStr = sessionStorage.getItem('exam_user')
        if (userStr) {
          const sessionUser = JSON.parse(userStr)
          sessionUser.avatar = user.avatar
          sessionUser.className = user.className
          sessionStorage.setItem('exam_user', JSON.stringify(sessionUser))
        }
      }
    }
  } catch (e) {
    console.error('获取用户信息失败', e)
  }
}

// 自定义上传方法
const customUpload = ({ file }) => {
  const reader = new FileReader()
  reader.onload = async (e) => {
    const base64 = e.target.result
    profileForm.avatar = base64
    await saveAvatar(base64)
  }
  reader.readAsDataURL(file)
}

// 头像上传前检查
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 保存头像
const saveAvatar = async (avatarData) => {
  try {
    const res = await fetch(`http://localhost:8081/api/users/profile/${userId.value}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ avatar: avatarData })
    })
    const result = await res.json()
    if (res.ok && result.code === 200) {
      ElMessage.success('头像更新成功')
      
      // 更新sessionStorage
      const userStr = sessionStorage.getItem('exam_user')
      if (userStr) {
        const sessionUser = JSON.parse(userStr)
        sessionUser.avatar = avatarData
        sessionStorage.setItem('exam_user', JSON.stringify(sessionUser))
      }
    } else {
      ElMessage.error(result.message || '头像更新失败')
    }
  } catch (e) {
    ElMessage.error('网络错误')
  }
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    changingPassword.value = true
    try {
      const res = await fetch(`http://localhost:8081/api/users/${userId.value}/password`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
      })
      const result = await res.json()
      if (res.ok && result.code === 200) {
        ElMessage.success('密码修改成功')
        // 清空表单
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
      } else {
        ElMessage.error(result.message || '密码修改失败')
      }
    } catch (e) {
      ElMessage.error('网络错误')
    } finally {
      changingPassword.value = false
    }
  })
}

onMounted(() => {
  // 从sessionStorage获取用户ID
  const userStr = sessionStorage.getItem('exam_user')
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      userId.value = user?.id
      loadUserProfile()
    } catch (e) {
      ElMessage.error('获取用户信息失败')
    }
  }
})
</script>

<style scoped>
.profile-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0;
}

.profile-card {
  border-radius: 20px;
  border: none;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.profile-card :deep(.el-card__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-bottom: none;
  padding: 20px 24px;
}

.card-header {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  display: flex;
  align-items: center;
}

.card-header :deep(.el-button) {
  color: #fff;
}

.profile-content {
  padding: 30px 40px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 40px;
  margin-bottom: 40px;
  padding: 30px 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  position: relative;
  overflow: hidden;
}

.avatar-section::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 70%);
  pointer-events: none;
}

.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  position: relative;
  z-index: 1;
}

.user-avatar {
  border: 5px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.25);
  transition: transform 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
}

.upload-btn {
  background: rgba(255, 255, 255, 0.95);
  color: #667eea;
  border: none;
  border-radius: 20px;
  padding: 8px 20px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.upload-btn:hover {
  background: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
}

.avatar-tips {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  position: relative;
  z-index: 1;
}

.avatar-tips p {
  margin: 8px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar-tips p::before {
  content: '✓';
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  font-size: 10px;
}

.profile-form,
.password-form {
  max-width: 550px;
}

.profile-form :deep(.el-form-item),
.password-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.profile-form :deep(.el-input__wrapper),
.password-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 8px 15px;
  box-shadow: 0 0 0 1px #e4e7ed inset;
  transition: all 0.3s ease;
}

.profile-form :deep(.el-input__wrapper:hover),
.password-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #667eea inset;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
  padding-left: 4px;
}

:deep(.el-divider) {
  margin: 32px 0 24px 0;
}

:deep(.el-divider__text) {
  font-weight: 700;
  font-size: 16px;
  color: #303133;
  background: #fff;
  padding: 0 16px;
}

:deep(.el-divider__text::before) {
  content: '';
  display: inline-block;
  width: 4px;
  height: 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 2px;
  margin-right: 10px;
  vertical-align: middle;
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: #f8f9fc;
}

.password-form :deep(.el-button) {
  height: 44px;
  border-radius: 10px;
  font-weight: 600;
  padding: 0 30px;
}

:deep(.el-tag--large) {
  padding: 8px 16px;
  font-size: 14px;
  border-radius: 8px;
}
</style>
