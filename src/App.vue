<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <RouterView v-slot="{ Component }">
    <keep-alive>
      <component :is="Component" :key="$route.fullPath" />
    </keep-alive>
  </RouterView>
</template>