<template>
  <div class="redirect-container">
   
    <div class="background-layer"></div>
    
    <div class="redirect-content">
     
      <div class="rotating-logo">
        <div class="logo-badge">
        
            <img class="fas fa-shield-alt" src="@/assets/ga.png" alt="logo" style="display: flex; width: 100%; height: 100%;"/>
         
        </div>
      </div>
      
      <!-- 状态信息 -->
      <div class="status-info">
        <h1 class="system-title">公司管理系统</h1>
        <p class="status-message">{{ message }}</p>
      </div>
      
      <!-- 倒计时 -->
      <div class="countdown-circle">
        <div class="circle-container">
          <div class="circle-border">
            <div class="circle-fill" :style="{ transform: `rotate(${(1 - countdown/5) * 360}deg)` }"></div>
          </div>
          <span class="countdown-text">{{ countdown }}s</span>
        </div>
      </div>
      
      <!-- 详细信息 -->
      <div class="details-panel">
        <div class="detail-item">
          <i class="fas fa-user"></i>
          <span>用户验证完成</span>
        </div>
        <div class="detail-item">
          <i class="fas fa-database"></i>
          <span>数据加载中... (85%)</span>
        </div>
        <div class="detail-item">
          <i class="fas fa-cogs"></i>
          <span>系统初始化</span>
        </div>
      </div>
    </div>
    
    <div class="footer-container">
      <p class="footer-text">© {{ currentYear }} 大连理工大学区块链版权所有</p>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const countdown = ref(5);
const message = ref('正在跳转到主页面...');
const loadingMessages = [
  "正在加载用户配置...",
  "验证登录状态...",
  "准备应用资源...",
  "初始化界面组件...",
  "最后一步..."
];

const currentYear = ref(new Date().getFullYear());
const loading = ref(true);

// 模拟跳转过程
onMounted(() => {
  // 倒计时
  const timer = setInterval(() => {
    countdown.value--;
    message.value = loadingMessages[countdown.value];
    
    if (countdown.value === 0) {
      clearInterval(timer);
      router.push('/main');
    }
  }, 800);
  
  // 如果加载时间过长（6秒以上）需要处理
  setTimeout(() => {
    if (loading.value) {
      clearInterval(timer);
      message.value = "跳转时间过长，请稍候...";
    }
  }, 6000);
});
</script>
<style scoped>

.redirect-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
  background: linear-gradient(135deg, #0a192f 0%, #172b4d 100%);
  font-family: 'Segoe UI', system-ui, sans-serif;
  color: #e2e8f0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

/* 动态背景层 */
.background-layer {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: conic-gradient(
    transparent, rgba(29, 78, 216, 0.15), transparent 60%
  );
  animation: rotate 15s linear infinite;
  z-index: -1;
}

@keyframes rotate {
  100% {
    transform: rotate(1turn);
  }
}

/* 主内容区域 */
.redirect-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: rgba(19, 25, 42, 0.85);
  border-radius: 16px;
  padding: 40px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(56, 139, 253, 0.2);
  position: relative;
  overflow: hidden;
}

/* 旋转logo */
.rotating-logo {
  margin-bottom: 24px;
}

.logo-badge {
  width: 100px;
  height: 100px;
  margin: 0 auto;
  border-radius: 50%;
  background: linear-gradient(135deg, #1d4ed8 0%, #0c4a6e 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 5px 15px rgba(29, 78, 216, 0.4);
  border: 2px solid rgba(255, 255, 255, 0.1);
  animation: pulse 2s infinite ease-in-out;
}

.logo-badge i {
  font-size: 48px;
  color: #ffffff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

/* Logo动画 */
@keyframes pulse {
  0% {
    transform: scale(1);
    box-shadow: 0 5px 15px rgba(29, 78, 216, 0.4);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 10px 25px rgba(29, 78, 216, 0.6);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 5px 15px rgba(29, 78, 216, 0.4);
  }
}

/* 状态信息 */
.status-info {
  text-align: center;
  margin-bottom: 30px;
}

.system-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 15px;
  color: #f8fafc;
}

.status-message {
  font-size: 18px;
  color: #94a3b8;
  margin-top: 10px;
}

/* 倒计时动画 */
.countdown-circle {
  margin: 20px 0;
}

.circle-container {
  position: relative;
  width: 120px;
  height: 120px;
}

.circle-border {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 6px solid rgba(30, 41, 59, 0.8);
  position: relative;
  overflow: hidden;
}

.circle-fill {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: conic-gradient(
    rgba(29, 78, 216, 0.9) 0%,
    rgba(29, 78, 216, 0.7) 50%,
    rgba(29, 78, 216, 0.3) 100%
  );
  transform-origin: center;
  transform: rotate(0deg);
  transition: transform 0.8s ease;
}

.circle-fill::before {
  content: '';
  position: absolute;
  top: 6px;
  left: 6px;
  right: 6px;
  bottom: 6px;
  background-color: rgba(19, 25, 42, 0.9);
  border-radius: 50%;
}

.countdown-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 32px;
  font-weight: 700;
  color: #f8fafc;
}

/* 详细信息面板 */
.details-panel {
  margin-top: 20px;
  width: 100%;
  background: rgba(30, 41, 59, 0.8);
  border-radius: 10px;
  padding: 15px;
  border: 1px solid #334155;
}

.detail-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-item i {
  width: 24px;
  text-align: center;
  margin-right: 10px;
  color: #60a5fa;
}

.detail-item span {
  font-size: 14px;
  color: #e2e8f0;
}

/* 页脚 */
.footer-container {
  margin-top: 30px;
  text-align: center;
}

.footer-text {
  color: #64748b;
  font-size: 12px;
  margin: 0;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .redirect-content {
    width: 90%;
    padding: 30px 20px;
  }
  
  .logo-badge {
    width: 80px;
    height: 80px;
  }
  
  .logo-badge i {
    font-size: 36px;
  }
  
  .system-title {
    font-size: 24px;
  }
  
  .status-message {
    font-size: 16px;
  }
  
  .circle-container {
    width: 100px;
    height: 100px;
  }
  
  .countdown-text {
    font-size: 28px;
  }
}
</style>