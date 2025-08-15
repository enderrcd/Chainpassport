<template>
  <div class="login-container">
    <div class="text-center mb-8">
      <div class="logo-badge">
        <img class="fas fa-shield-alt" src="@/assets/ga.png" alt="logo" style="display: flex; width: 100%; height: 100%;"/>
      </div>
      <h1 class="system-title">公司管理系统</h1>
      <p class="system-subtitle">请使用您的账号登录系统</p>
    </div>

    <form @submit.prevent="handleLogin" class="space-y-6">
      <div>
        <label for="username" class="input-label">用户名</label>
        <div class="input-wrapper">
          <div class="input-icon">
            <i class="fas fa-user"></i>
          </div>
          <el-input
            v-model="form.username"
            type="text"
            id="username"
            class="input-field"
            placeholder="请输入用户名"
            @focus="clearError('username')"
            clearable
          />
        </div>
        <div v-if="errors.username" class="error-message">
          {{ errors.username }}
        </div>
      </div>

      <div>
        <label for="password" class="input-label">密码</label>
        <div class="input-wrapper">
          <div class="input-icon">
            <i class="fas fa-lock"></i>
          </div>
          <el-input
            v-model="form.password"
            type="password"
            id="password"
            class="input-field"
            placeholder="请输入密码"
            @focus="clearError('password')"
            clearable
            show-password
          />
        </div>
        <div v-if="errors.password" class="error-message">
          {{ errors.password }}
        </div>
      </div>

      <div class="flex-container">
        <div class="remember-me">
          <input
            v-model="form.remember"
            id="remember-me"
            name="remember-me"
            type="checkbox"
            class="checkbox"
          >
          <label for="remember-me" class="checkbox-label">记住我</label>
        </div>
        <div class="forgot-password-container">
          <a href="#" class="forgot-password">忘记密码?</a>
        </div>
      </div>

      <button
        type="submit"
        class="auth-btn"
        :disabled="loading"
        @click="handleLogin"
      >
        <span v-if="loading">
          <i class="fas fa-spinner fa-spin spinner-icon"></i>登录中...
        </span>
        <span v-else>登录</span>
      </button>
    </form>

    <div class="divider-container">
      <div class="divider-line"></div>
      <span class="divider-text">其他登录方式</span>
      <div class="divider-line"></div>
    </div>

    <div class="alternative-login-container">
      <button
        class="alternative-btn"
        @click="idLogin"
        @mouseenter="hoverButton('id')"
        @mouseleave="hoverButton(null)"
      >
        <i class="fas fa-id-card-alt btn-icon"></i> 
        身份校验
      </button>
      <button
        class="alternative-btn"
        @click="giteeLogin(false)"
        @mouseenter="hoverButton('github')"
        @mouseleave="hoverButton(null)"
      >
        <i class="fab fa-github btn-icon"></i> Gitee账号
      </button>
    </div>

    <div class="footer-container">
      <p class="footer-text">© {{ currentYear }} 大连理工大学区块链版权所有</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { reqLogin, reqGitee, reqIdLogin } from '@/api/interface';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/moudules/user';

const router = useRouter();
const userStore = useUserStore();


// 表单数据
const form = ref({
  username: '',
  password: '',
  remember: false
});

// 错误信息
const errors = ref({
  username: '',
  password: ''
});

// 状态管理
const loading = ref(false);
const activeButton = ref(null);
const currentYear = ref(new Date().getFullYear());

// 表单验证
const validateForm = () => {
  let valid = true;
  errors.value = { username: '', password: '' };
  
  if (!form.value.username.trim()) {
    errors.value.username = '请输入用户名';
    valid = false;
  }
  
  if (!form.value.password) {
    errors.value.password = '请输入密码';
    valid = false;
  } else if (form.value.password.length < 6) {
    errors.value.password = '密码长度至少6位';
    valid = false;
  }
  
  return valid;
};

// 清除错误信息
const clearError = (field: any) => {
  errors.value.password = '';
  errors.value.username = '';
};

// 按钮 hover 效果
const hoverButton = (type:any) => {
  activeButton.value = type;
};

// Gitee 登录
const giteeLogin = async(isAutoLogin: boolean = false) => {
  console.log('Gitee 登录');
  ElMessage.info('正在获取Gitee授权码...');
  const shouldProceed = isAutoLogin 
    ? !userStore.isTokenValid() 
    : !userStore.isGiteeIdValid() && !userStore.isTokenValid();

    if(shouldProceed) {
      const response = await reqGitee();
      if(response.data.code === 200){
        const { client_id, redirect_uri, scope, response_type } = response.data.data;
        const params = new URLSearchParams();
        params.append('client_id', client_id);
        params.append('redirect_uri', redirect_uri);
        params.append('response_type', response_type);
        if (scope) {
          params.append('scope', scope);
        }
        const url = 'http://localhost:8080/oauth2/register/gitee' + '?' + params.toString();
        window.location.href = url;
    }else {
      ElMessage.error('获取 Gitee 配置失败');
    }
    }
};


//自动登录部分
if(userStore.isTokenValid()) {
  router.push('/main');
}else {
  if(userStore.isGiteeIdValid()) {
    giteeLogin(true);
  }else {
    router.push('/');
  }
}
// 处理登录
const handleLogin = async() => {
  if (!validateForm()) return;

  loading.value = true;
  
  try {
    const reqLoginForm = {
      userName: form.value.username,
      password: form.value.password,
    }
    const response = await reqLogin(reqLoginForm);
    userStore.setToken(response.data.data);
    userStore.setUserInfo(form.value.username);
    router.push('/main');
    ElMessage.success('登录成功');
  }catch (error) {
    console.error('登录失败:', error);
    loading.value = false;
    ElMessage.error('登录失败，请检查用户名和密码');
    return;
  }finally {
    loading.value = false;
  }
};

const idLogin = async() => {
  console.log('身份校验登录');
  const response = await reqIdLogin();
  if(response.data.code === 200) {
    router.push('/idLogin');
  } else {
    ElMessage.error('身份校验登录失败，请稍后再试');
  }
};



</script>

<style>
/* 基础样式 */
body {
  background: linear-gradient(135deg, #0a192f 0%, #172b4d 100%);
  font-family: 'Segoe UI', system-ui, sans-serif;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  margin: 0;
  padding: 20px;
  color: #e2e8f0;
}

/* 登录容器 */
.login-container {
  background-color: rgba(19, 25, 42, 0.9);
  border-radius: 16px;
  padding: 40px;
  width: 100%;
  max-width: 480px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(56, 139, 253, 0.2);
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: conic-gradient(
    transparent, rgba(29, 78, 216, 0.2), transparent 60%
  );
  animation: rotate 8s linear infinite;
  z-index: -1;
}

@keyframes rotate {
  100% {
    transform: rotate(1turn);
  }
}

/* 顶部logo区域 */
.text-center {
  text-align: center;
  margin-bottom: 32px;
}

.logo-badge {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1d4ed8 0%, #0c4a6e 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 5px 15px rgba(29, 78, 216, 0.4);
  border: 2px solid rgba(255, 255, 255, 0.1);
  overflow: hidden;
}

.logo-badge i {
  font-size: 36px;
  font-size: 28px;
  font-weight: 700;
  color: #ffffff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  letter-spacing: 0.5px;
  margin: 0;
}

.system-subtitle {
  font-size: 16px;
  color: #94a3b8;
  margin-top: 10px;
  margin-bottom: 0;
}

/* 表单样式 */
.space-y-6 > * + * {
  margin-top: 24px;
}

.input-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #e2e8f0;
  margin-bottom: 8px;
}

.input-wrapper {
  position: relative;
}

.input-icon {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  padding-left: 12px;
  display: flex;
  align-items: center;
  pointer-events: none;
}

.input-icon i {
  color: #64748b;
  font-size: 16px;
}

.input-field {
  width: 100%;
  padding: 10px 12px 10px 40px;
  background-color: rgba(30, 41, 59, 0.8);
  border: 1px solid #334155;
  border-radius: 8px;
  color: #f8fafc;
  font-size: 14px;
  transition: all 0.2s;
}

.input-field:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.error-message {
  color: #ef4444;
  font-size: 12px;
  margin-top: 4px;
}

/* 记住我和忘记密码 */
.flex-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.remember-me {
  display: flex;
  align-items: center;
}

.checkbox {
  width: 16px;
  height: 16px;
  border: 1px solid #4b5563;
  border-radius: 4px;
  background-color: #1e293b;
  appearance: none;
  position: relative;
  cursor: pointer;
}

.checkbox:checked {
  background-color: #2563eb;
  border-color: #2563eb;
}

.checkbox:checked::after {
  content: '✓';
  position: absolute;
  color: white;
  font-size: 12px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.checkbox-label {
  font-size: 14px;
  color: #e2e8f0;
  margin-left: 8px;
  cursor: pointer;
}

.forgot-password {
  color: #60a5fa;
  font-size: 14px;
  text-decoration: none;
  transition: color 0.2s;
}

.forgot-password:hover {
  color: #3b82f6;
}

/* 登录按钮 */
.auth-btn {
  width: 100%;
  padding: 12px;
  background-color: #1d4ed8;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  justify-content: center;
  align-items: center;
}

.auth-btn:hover {
  background-color: #1e40af;
}

.auth-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spinner-icon {
  margin-right: 8px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 分割线 */
.divider-container {
  display: flex;
  align-items: center;
  margin: 32px 0;
}

.divider-line {
  flex: 1;
  height: 1px;
  background-color: #334155;
}

.divider-text {
  padding: 0 12px;
  color: #94a3b8;
  font-size: 14px;
}

/* 其他登录方式 */
.alternative-login-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.alternative-btn {
  padding: 10px;
  background-color: #1e293b;
  color: #e2e8f0;
  border: 1px solid #334155;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  justify-content: center;
  align-items: center;
}

.alternative-btn:hover {
  background-color: #334155;
  transform: translateY(-2px);
}

.btn-icon {
  margin-right: 8px;
}

/* 页脚 */
.footer-container {
  margin-top: 32px;
  text-align: center;
}

.footer-text {
  color: #64748b;
  font-size: 12px;
  margin: 0;
}
</style>