import { createApp } from 'vue'
import { createPinia } from 'pinia'  // 导入创建函数

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import piniaPluginPersistedstate from "pinia-plugin-persistedstate";

import App from './App.vue'
import router from './router'

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

const app = createApp(App)

app.use(pinia)  
app.use(router)
app.use(ElementPlus)
app.mount('#app')
