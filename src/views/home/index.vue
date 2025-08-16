<template>
  <div class="home-container">
   
    <div class="header">
      <div class="logo">
        <i class="fas fa-cubes"></i>
        <span>Global ERP</span>
      </div>
      <div class="user-info">
        <div class="avatar">{{ userInitials }}</div>
        <div class="details">
          <div class="name">{{ userStore.giteeId || '未登录用户' }}</div>
          <div class="role">系统管理员</div>
        </div>
      </div>
    </div>

  
    <div class="main-content">
      <h1 class="welcome-title">欢迎使用企业资源规划系统</h1>
      <p class="welcome-text">高效管理企业资源，提升运营效率</p>

     
      <div class="user-card">
        <div class="card-header">
          <i class="fas fa-user-circle"></i>
          <h2>用户信息</h2>
        </div>
        <div class="card-content">
          <div class="info-row">
            <span class="label">username:</span>
            <span class="value">{{ userStore.username || '未登录' }}</span>
          </div>
          <div class="info-row">
            <span class="label">giteeId:</span>
            <span class="value token">{{ userStore.giteeId }}</span>
          </div>
          <div class="info-row">
            <span class="label">登录状态:</span>
            <span class="value status">
              <span class="status-indicator" :class="{'online': isLoggedIn}"></span>
              {{ isLoggedIn ? '已登录' : '未登录' }}
            </span>
          </div>
        </div>
      </div>

    
      <div class="logout-section">
        <h2 class="logout-title">
          <i class="fas fa-sign-out-alt"></i>
          系统登出
        </h2>
        <p class="logout-description">
          完成工作后请及时登出，保护您的账户安全。登出操作将清除您的登录凭证。
        </p>
        <button 
          class="logout-btn" 
          @click="handleLogout"
          :disabled="!isLoggedIn"
        >
          <i class="fas fa-sign-out-alt"></i>
          安全登出
        </button>
        <div class="logout-message">{{ logoutMessage }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useUserStore } from '@/stores/moudules/user';
import { reqLogout } from '@/api/interface/index'
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const userStore = useUserStore();
const logoutMessage = ref('');
const router = useRouter();


onMounted(() => {
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get('token');
  const giteeId = urlParams.get('giteeId');
  
  if (token && giteeId) {
    userStore.setToken(token);
    userStore.setGiteeId(giteeId);
  }
});

// 计算属性
const isLoggedIn = computed(() => !!userStore.token);
const userInitials = computed(() => {
  if (!userStore.giteeId) return 'GU';
  const parts = userStore.giteeId.split(/[_-]/);
  return parts.map(p => p.charAt(0).toUpperCase()).slice(0, 2).join('');
});

// 登出处理
const handleLogout = async() => {
  logoutMessage.value = '正在登出系统...';
  var response = await reqLogout(userStore.token);
  if(response.data.code === 200) {
    userStore.clearJWT();
    userStore.clearGiteeId();
    router.push('/');
  }else {
    ElMessage.error('登出失败，请稍后再试！');
    logoutMessage.value = '登出失败，请稍后再试！';
  }
};
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #0a192f;
  color: #e6f1ff;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}


.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 30px;
  background-color: rgba(17, 34, 64, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(46, 66, 93, 0.5);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.5rem;
  font-weight: 700;
  color: #64ffda;
}

.logo i {
  font-size: 1.8rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(45deg, #3b82f6, #64ffda);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 1.2rem;
}

.details {
  display: flex;
  flex-direction: column;
}

.name {
  font-weight: 600;
  font-size: 1rem;
}

.role {
  font-size: 0.85rem;
  color: #a8b2d1;
}


.main-content {
  flex: 1;
  padding: 30px;
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
}

.welcome-title {
  font-size: 2.5rem;
  text-align: center;
  margin-bottom: 15px;
  background: linear-gradient(45deg, #64ffda, #3b82f6);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.welcome-text {
  text-align: center;
  color: #a8b2d1;
  font-size: 1.2rem;
  margin-bottom: 40px;
}

/* 用户信息卡片样式 */
.user-card {
  width: 81%;
  background-color: rgba(17, 34, 64, 0.8);
  border-radius: 16px;
  padding: 25px;
  margin-bottom: 40px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 25px;
}

.card-header i {
  font-size: 2rem;
  color: #64ffda;
}

.card-header h2 {
  font-size: 1.8rem;
  color: #e6f1ff;
}

.info-row {
  display: flex;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(46, 66, 93, 0.5);
}

.info-row:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.label {
  width: 120px;
  color: #a8b2d1;
  font-weight: 500;
}

.value {
  flex: 1;
  color: #e6f1ff;
}

.token {
  font-family: monospace;
}

.status {
  display: flex;
  align-items: center;
}

.status-indicator {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 10px;
  background-color: #ef4444;
}

.status-indicator.online {
  background-color: #10b981;
}


.logout-section {
  width: 80%;
  text-align: center;
  padding: 30px;
  background-color: rgba(17, 34, 64, 0.8);
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.logout-title {
  font-size: 1.8rem;
  color: #64ffda;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.logout-description {
  color: #a8b2d1;
  margin-bottom: 25px;
  line-height: 1.6;
  max-width: 500px;
  margin-left: auto;
  margin-right: auto;
}

.logout-btn {
  background: linear-gradient(45deg, #3b82f6, #64ffda);
  border: none;
  border-radius: 8px;
  color: white;
  font-weight: bold;
  font-size: 1.1rem;
  padding: 14px 35px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin: 0 auto;
}

.logout-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 6px 15px rgba(59, 130, 246, 0.4);
}

.logout-btn:disabled {
  background: #64748b;
  cursor: not-allowed;
  opacity: 0.7;
}

.logout-message {
  color: #64ffda;
  font-weight: bold;
  font-size: 1.1rem;
  margin-top: 20px;
  height: 24px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 12px 20px;
  }
  
  .logo {
    font-size: 1.3rem;
  }
  
  .welcome-title {
    font-size: 2rem;
  }
  
  .main-content {
    padding: 20px;
  }
  
  .user-card, .logout-section {
    padding: 20px;
  }
  
  .info-row {
    flex-direction: column;
    gap: 8px;
  }
  
  .label {
    width: 100%;
  }
}
</style>