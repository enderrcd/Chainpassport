import { defineStore } from 'pinia';
import { ElMessage } from "element-plus";

interface UserState {
    username: string;
    token: string;
    giteeId: string;
    tokenExpiresAt: number;
    giteeIdExpiresAt: number;

}

export const useUserStore = defineStore('user', {
    state: (): UserState => ({
        username: '',
        token: '',
        giteeId: '',
        tokenExpiresAt: 0,
        giteeIdExpiresAt: 0,

    }),
    actions: {
        setToken(token: string) {
            this.token = token;
            // 计算30分钟后的时间戳（单位：毫秒）
            this.tokenExpiresAt = Date.now() + 30 * 60 * 1000;
        },
        setGiteeId(giteeId: string) {
            this.giteeId = giteeId;
            // 计算1h后的时间戳（单位：毫秒）
            this.giteeIdExpiresAt = Date.now() + 60 * 60 * 1000;
        },
        setUserInfo(username: string) { 
            this.username = username;
        },

        // 清除token过期
        clearJWT() {
            this.token = '';
            this.tokenExpiresAt = 0;
        },
        // 清除giteeId过期
        clearGiteeId() {
            this.giteeId = '';
            this.username = '';
            this.giteeIdExpiresAt = 0;          
        },

        // 检查token是否有效（未过期）
        isTokenValid(): boolean {
            if (this.token) {
                return this.tokenExpiresAt > Date.now();
            }else {
                this.clearJWT();
            }
            return false;
        },
        isGiteeIdValid(): boolean {
            if(this.giteeId) {
                return this.giteeIdExpiresAt > Date.now();
            }else {
                this.clearGiteeId();
            }
            return false;
        },
    },
    persist: true
});