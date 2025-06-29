<template>
  <div class="bg-black text-white rounded-md h-48 flex items-center justify-center">
    {{ name }}'s Video
  </div>
</template>

<script setup lang="ts">
 defineProps<{ name: string }>()
</script>