<template>
  <div class="app-shell">
    <AppSidebar />
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import AppSidebar from '@/components/AppSidebar.vue'
</script>

<style scoped>
.app-shell {
  display: flex;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  margin-left: 220px;
  padding: 0;
  min-height: 100vh;
  transition: margin-left 0.2s ease;
}

@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
    padding-bottom: 56px;
  }
}
</style>
