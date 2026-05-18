import axios from 'axios'

const BASE_URL = 'http://localhost:8512/api'

const api = axios.create({
  baseURL: BASE_URL,
  timeout: 180000
})

// 生成UUID
export function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

// SSE流式调用 - 职业规划
export function chatWithCareerSSE(message, chatId, onMessage, onError, onComplete) {
  const url = `${BASE_URL}/ai/career_app/chat/sse?message=${encodeURIComponent(message)}&chatId=${encodeURIComponent(chatId)}`

  const eventSource = new EventSource(url)
  let isCompleted = false
  let hasError = false
  let hasReceivedData = false

  eventSource.onmessage = (event) => {
    if (isCompleted || hasError) return
    hasReceivedData = true
    const data = event.data
    // 尝试解析是否为错误响应
    try {
      const parsed = JSON.parse(data)
      if (parsed.code !== undefined && parsed.code !== 0) {
        hasError = true
        onError(parsed.message || '请求失败')
        eventSource.close()
        return
      }
    } catch (e) {
      // 不是JSON格式，直接作为普通消息处理
    }
    onMessage(data)
  }

  eventSource.onerror = () => {
    // 如果已经完成或出错，直接关闭
    if (isCompleted || hasError) {
      eventSource.close()
      return
    }
    // 关闭连接，防止自动重连
    eventSource.close()

    // 如果收到过数据，视为正常完成
    if (hasReceivedData) {
      isCompleted = true
      onComplete()
    } else {
      // 没收到任何数据，报错
      hasError = true
      onError('连接失败，请重试')
    }
  }

  return eventSource
}

// SSE流式调用 - Manus智能体
export function chatWithManusSSE(message, onMessage, onError, onComplete) {
  const url = `${BASE_URL}/ai/manus/chat?message=${encodeURIComponent(message)}`

  const eventSource = new EventSource(url)
  let isCompleted = false
  let hasError = false
  let hasReceivedData = false

  eventSource.onmessage = (event) => {
    if (isCompleted || hasError) return
    hasReceivedData = true
    const data = event.data
    // 尝试解析是否为错误响应
    try {
      const parsed = JSON.parse(data)
      if (parsed.code !== undefined && parsed.code !== 0) {
        hasError = true
        onError(parsed.message || '请求失败')
        eventSource.close()
        return
      }
    } catch (e) {
      // 不是JSON格式，直接作为普通消息处理
    }
    onMessage(data)
  }

  eventSource.onerror = () => {
    // 如果已经完成或出错，直接关闭
    if (isCompleted || hasError) {
      eventSource.close()
      return
    }
    // 关闭连接，防止自动重连
    eventSource.close()

    // 如果收到过数据，视为正常完成
    if (hasReceivedData) {
      isCompleted = true
      onComplete()
    } else {
      // 没收到任何数据，报错
      hasError = true
      onError('连接失败，请重试')
    }
  }

  return eventSource
}

export default api
