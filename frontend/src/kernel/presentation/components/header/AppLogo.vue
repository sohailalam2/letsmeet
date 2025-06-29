<template>
  <div class="flex items-center gap-2 cursor-pointer" @click="$emit('click')">
    <span
        class="text-xl font-medium text-dark-secondary-600 dark:text-dark-tertiary-500 flex items-center gap-2.5"
    >
      <img
          class="w-14 h-14 rounded-full"
          src="@/assets/icons/logo.svg"
          alt="LetsMeet Logo"
      />LetsMeet</span
    >
  </div>
</template>

<script setup lang="ts">
defineEmits(['click']);
</script>
