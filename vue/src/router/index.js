import { createRouter, createWebHistory } from 'vue-router'

// 考试状态管理：用于记录用户是否正在考试中
export const examState = {
  isInExam: false,
  examId: null,
  hasSubmitted: false  // 是否已交卷，用于区分交卷跳转和手动点击
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {path: '/', redirect:'/login'},
    {path: '/login', component: ()=> import('../views/Login.vue')},
    {path: '/top', component: ()=> import('../views/top.vue'),children: [
        {path: '', redirect: '/top/home'},
        {path: 'home', name: 'home', component: ()=> import('../views/Home.vue')},
        {path: 'exams', name: 'userExams', component: ()=> import('../views/UserExamList.vue')},
        {path: 'history', name: 'userHistory', component: ()=> import('../views/UserExamHistory.vue')},
        {path: 'test', name: 'test', component: ()=> import('../views/Test.vue')},
        {path: 'profile', name: 'userProfile', component: ()=> import('../views/Profile.vue')},
      ]},
    {path: '/admin', name: 'admin', component: ()=> import('../views/Admin.vue')},
    {path: '/admin/profile', name: 'adminProfile', component: ()=> import('../views/Profile.vue')},
    {path: '/admin-question-bank', name: 'adminQuestionBank', component: ()=> import('../views/QuestionBank.vue')},
  ],
})

// 简单登录守卫
router.beforeEach((to, from, next) => {
  const userStr = sessionStorage.getItem('exam_user')
  let user = null
  if (userStr) {
    try {
      user = JSON.parse(userStr)
    } catch (e) {
      user = null
    }
  }

  const isLoggedIn = !!user

  // 未登录访问受保护路由时，跳转到登录页
  if (!isLoggedIn && (to.path.startsWith('/top') || to.path.startsWith('/admin'))) {
    return next('/login')
  }

  // 已登录访问登录页时，根据角色跳转到对应首页
  if (isLoggedIn && to.path === '/login') {
    if (user && (user.role === 'ADMIN' || user.role === 2 || user.role === '2')) {
      return next('/admin')
    }
    return next('/top')
  }

  // 考试中禁止离开考试页面（除非已交卷）
  if (examState.isInExam && from.path === '/top/test' && to.path !== '/top/test') {
    // 只有交卷后才允许跳转到历史记录页
    if (examState.hasSubmitted && to.path === '/top/history') {
      examState.isInExam = false
      examState.examId = null
      examState.hasSubmitted = false
      return next()
    }
    // 其他情况阻止跳转
    return next(false)
  }

  next()
})

export default router