<template>
  <div class="bg-[#fff6f3] dark:bg-dark-secondary-900 xl:h-screen xl:pb-0 pb-10">
    <HeaderNav/>
    <div :class="['text-gray-900', { 'xl:translate-y-1/3': isMeetingsJoinPage }]">
      <div class="xl:max-w-7xl xl:mx-auto grid xl:grid-cols-2 md:grid-cols-1 xl:gap-8 gap-5 xl:pl-10 h-fit">
        <slot/>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import HeaderNav from './header/HeaderNav.vue'

const route = useRoute()

const isMeetingsJoinPage = computed(() =>
    /^\/meetings\/[^/]+\/join$/.test(route.path)
)
</script>
