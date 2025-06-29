<template>
  <span class="text-dark-secondary-600 dark:text-dark-tertiary-500 text-lg">
    {{ formattedTime }}
  </span>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue";

const now = ref(new Date());
let intervalId: number;

onMounted(() => {
  // Update immediately to avoid delay
  now.value = new Date();

  intervalId = setInterval(() => {
    now.value = new Date();
  }, 60 * 1000); // every minute
});

onUnmounted(() => {
  clearInterval(intervalId);
});

const formattedTime = computed(() => {
  const time = new Intl.DateTimeFormat("en-US", {
    hour: "numeric",
    minute: "2-digit",
    hour12: true,
  }).format(now.value);

  const day = new Intl.DateTimeFormat("en-US", {
    weekday: "short",
    month: "short",
    day: "numeric",
  }).format(now.value);

  return `${time} • ${day}`;
});
</script>
