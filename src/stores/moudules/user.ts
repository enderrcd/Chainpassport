import { defineStore } from 'pinia';
import { ElMessage } from "element-plus";

interface UserState {
    username: string;
    token: string;
    tokenExpiresAt: number;
}

export const useUserStore = defineStore('user', {
    state: (): UserState => ({
        username: '',
        token: '',
        tokenExpiresAt: 0
    }),
    actions: {
        setUserInfo(token: string, username: string) {
            this.token = token;
            this.username = username;
            // 计算30分钟后的时间戳（单位：毫秒）
            this.tokenExpiresAt = Date.now() + 30 * 60 * 1000;
        },

        // 清除token及过期时间
        clearUserInfo() {
            this.token = '';
            this.username = '';
            this.tokenExpiresAt = 0;
        },

        // 检查token是否有效（未过期）
        isTokenValid(): boolean {
            if (this.token) {
                return this.tokenExpiresAt > Date.now();
            }else {
                this.clearUserInfo();
                ElMessage.error("请先登录");
            }
            return false;
        },
    },
    persist: true
});