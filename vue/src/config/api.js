// API 配置文件
// 根据环境自动切换 API 地址

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081'

export default API_BASE_URL

// 辅助函数：构建完整 API URL
export function apiUrl(path) {
  // 移除开头的斜杠（如果有）
  const cleanPath = path.startsWith('/') ? path.slice(1) : path
  
  // 如果 API_BASE_URL 已经包含完整路径，直接拼接
  return `${API_BASE_URL}/${cleanPath}`
}

// 便捷的 fetch 封装
export async function apiFetch(path, options = {}) {
  const url = apiUrl(path)
  const response = await fetch(url, options)
  return response
}
