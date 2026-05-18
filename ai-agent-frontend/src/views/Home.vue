<template>
  <div class="home-container">
    <h1 class="title">AI Agent 应用中心</h1>
    <div class="card-container">
      <div class="app-card" @click="goToCareer">
        <div class="card-icon">🎯</div>
        <h2>AI 职业规划</h2>
        <p>智能职业规划助手，帮助你规划职业发展路径</p>
        <button class="enter-btn">进入应用</button>
      </div>
      <div class="app-card" @click="goToManus">
        <div class="card-icon">🤖</div>
        <h2>AI 超级智能体</h2>
        <p>强大的AI智能体，支持多种任务处理</p>
        <button class="enter-btn">进入应用</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goToCareer = () => {
  router.push('/career')
}

const goToManus = () => {
  router.push('/manus')
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.title {
  color: white;
  font-size: 2.5rem;
  margin-bottom: 40px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.card-container {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
  justify-content: center;
}

.app-card {
  background: white;
  border-radius: 20px;
  padding: 40px 30px;
  width: 280px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.app-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.card-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.app-card h2 {
  color: #333;
  margin-bottom: 15px;
  font-size: 1.5rem;
}

.app-card p {
  color: #666;
  font-size: 0.95rem;
  line-height: 1.6;
  margin-bottom: 25px;
}

.enter-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px 30px;
  border-radius: 25px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.enter-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}
</style>
