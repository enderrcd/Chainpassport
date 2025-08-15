import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/login/login.vue'
import Home from '@/views/home/index.vue'
import IdLogin from '@/views/login/idLogin.vue'
import { useUserStore } from '@/stores/moudules/user';


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: Login
    },
    {
      path: '/main',
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


/* router.beforeEach((to) => {
  const userStore = useUserStore();
  // 已登录（token有效）则禁止访问登录页
  if (to.path === '/' && userStore.isTokenValid()) {
    return '/main';
  }
  // 未登录则禁止访问主页面
  if (to.path === '/main' && !userStore.isTokenValid()) {
    return '/';
  }
}); */

export default router
