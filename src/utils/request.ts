import axios from 'axios';

let request = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 5000
});

// 添加请求拦截器
request.interceptors.request.use((config) => {
    return config;
})

// 添加响应拦截器
request.interceptors.response.use(
    (response) => {
        return response.data;
    },
    (error) => {
        let message = '';
        let status = error.response ? error.response.status : null;
        switch (status) {
            case 401:
                message = 'TOKEN 过期';
                break;
            case 403:
                message = '无权访问';
                break;
            case 404:
                message = '请求地址错误';
                break;
            case 500:
                message = '服务器出问题';
                break;
            default:
                message = '网络出问题了';
        }
        console.error(message);
        return Promise.reject(error);
    }
)

export default request;
