<template>
  <button
      class="bg-dark-primary-600 text-dark-tertiary-500 px-8 py-3 rounded-lg cursor-pointer hover:bg-dark-primary-700 transition-colors"
      @click="$emit('click')"
  >
    Login
  </button>
</template>

<script setup lang="ts">
defineEmits(['click']);
</script>
