import axios from 'axios';
import { useUserStore } from '@/stores/moudules/user';
import { storeToRefs } from 'pinia'; // 用于解构Pinia状态
import { ElMessage } from 'element-plus';

let request = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 5000
});

// 添加请求拦截器
request.interceptors.request.use((config) => {
    const userStore = useUserStore();
    const { token, giteeId } = storeToRefs(userStore); // 解构出响应式的token和giteeId
    
    const requestUrl = config.url || '';
    // 1. 全局添加JWT令牌
    // 仅在token有效且当前页面需要认证时添加
    const noAuthUrls = ['/login', '/public/login/gitee/config', '/public/register']; // 无需认证的接口
    if (!noAuthUrls.some(url => requestUrl.includes(url)) && userStore.isTokenValid()) {
    config.headers['authentication'] = token.value;
    }
    // 2. Gitee登录接口添加giteeId
    if (requestUrl.includes('/public/login/gitee/config')) {
        config.headers['giteeId'] = userStore.isGiteeIdValid() ? giteeId.value : null;
    }

    return config;
});

// 添加响应拦截器
request.interceptors.response.use(
    (response) => {
        const userStore = useUserStore();
        // 处理零知识证明认证接口的token存储
        if (response.config.url?.includes('/public/login/button') && response.data.code === 200) {
            userStore.setToken(response.data.data); // 存储ZKP返回的token
        }
        return response.data;
    },
    (error) => {
        let message = '';
        const status = error.response?.status;
        const userStore = useUserStore();
        switch (status) {
            case 401:
                message = 'TOKEN 过期或无效';
                userStore.clearJWT();
                break;
            case 403:
                message = '无权访问，请重新登录';
                break;
            case 404:
                message = '请求地址错误，请检查接口路径';
                break;
            case 500:
                message = '服务器内部错误，请稍后重试';
                break;
            default:
                message = '网络连接异常';
        }
        ElMessage.error(message);
        console.error(message);
        return Promise.reject(error);
    }
);

export default request;