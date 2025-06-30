<template>
  <div id="app" class="min-h-screen dark:bg-dark-secondary-900 bg-[#fff6f3]">
    <router-view :key="route.fullPath" v-slot="{ Component }">
      <transition name="fade">
        <component :is="Component" />
      </transition>
    </router-view>
  </div>
  <div id="toast-container" class="fixed bottom-4 right-4 z-50"></div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { AuthService } from "@/kernel";

const route = useRoute();
onMounted(() => {
  const router = useRouter();

  if (AuthService.getUserContext() && !AuthService.isUserLoggedIn()) {
    router.push("/");
  }
});
</script>
