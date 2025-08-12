import { createApp } from 'vue'
import { createPinia } from 'pinia'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import piniaPluginPersistedstate from "pinia-plugin-persistedstate";

import App from './App.vue'
import router from './router'
import pinia from './stores';

const app = createApp(App)

pinia.use(piniaPluginPersistedstate)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')

