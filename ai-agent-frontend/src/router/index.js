import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import CareerChat from '../views/CareerChat.vue'
import ManusChat from '../views/ManusChat.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/career',
    name: 'CareerChat',
    component: CareerChat
  },
  {
    path: '/manus',
    name: 'ManusChat',
    component: ManusChat
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
