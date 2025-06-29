<template>
  <div class="fixed inset-0 bg-black/60 dark:bg-black/90 flex justify-center">
    <div class="overlay-std text-center p-8 bg-white">
      <h2 class="text-xl font-bold text-black/80">Just a moment</h2>
      <p class="text-black/50 mt-2">We are creating your space</p>
      <div class="flex justify-center items-center mt-10">
        <div
          class="w-40 h-40 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"
        ></div>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import {onMounted} from "vue";
import { useRoute, useRouter } from "vue-router";
import { AuthService } from "@/kernel";

onMounted(async () => {
  const router = useRouter();
  const route = useRoute();
  const code = route.query.code as string;

  await AuthService.loginWithCode(code);
  await router.push("/");
});
</script>
