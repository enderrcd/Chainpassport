import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/login/login.vue'
import Home from '@/views/home/index.vue'
import IdLogin from '@/views/login/idLogin.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: Login
    },
    {
      path: '/home',
      name: 'home',
      component: Home,
    },

    {
      path: '/idLogin',
      name: 'idLogin',
      component: IdLogin,
    }
  ],
})

export default router
