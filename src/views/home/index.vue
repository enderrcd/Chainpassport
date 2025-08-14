<template>
    <h1>home</h1>
    <h2>logout</h2>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useUserStore } from '@/stores/moudules/user';

const userStore = useUserStore();

onMounted(() => {
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get('token');
  const giteeId = urlParams.get('giteeId');
  if (token && giteeId) {
    userStore.setToken(token);
    userStore.setGiteeId(giteeId);
  }
});
</script>