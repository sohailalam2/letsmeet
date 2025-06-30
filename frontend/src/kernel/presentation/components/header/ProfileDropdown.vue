<template>
  <div class="relative">
    <button
        @click="toggleDropdown"
        class="cursor-pointer focus:outline-none"
        aria-haspopup="true"
        :aria-expanded="isOpen"
    >
      <img
          :src="imageUrl || fallbackImage"
          :alt="`${user?.name || 'User'}'s profile`"
          class="w-10 h-10 rounded-full border border-dark-secondary-400 dark:border-dark-tertiary-400 p-1"
      />
    </button>

    <transition
        enter-active-class="transition ease-out duration-100"
        enter-from-class="transform opacity-0 scale-95"
        enter-to-class="transform opacity-100 scale-100"
        leave-active-class="transition ease-in duration-75"
        leave-from-class="transform opacity-100 scale-100"
        leave-to-class="transform opacity-0 scale-95"
    >
      <div
          v-if="isOpen"
          class="absolute top-14 right-0 min-w-60 max-w-sm w-fit soft-corner bg-dark-secondary-600 dark:bg-dark-tertiary-500 dark:text-black p-5 text-dark-tertiary-500 z-50 text-left shadow-lg"
          role="menu"
          aria-orientation="vertical"
          @click.outside="isOpen = false"
      >
        <h4 class="font-bold">{{ user?.name }}</h4>
        <p class="break-words">{{ user?.email }}</p>
        <div class="border-t border-gray-200 dark:border-dark-tertiary-600 my-1"></div>

        <div class="xl:hidden flex flex-col justify-start items-start">
          <div class="md:hidden">
            <ThemeToggle />
          </div>
        </div>

        <button
            @click="handleLogout"
            class="block w-full bg-red-600 text-dark-tertiary-500 px-8 py-3 rounded-lg cursor-pointer mt-3 hover:bg-red-700 transition-colors"
        >
          Logout
        </button>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import ThemeToggle from "../ThemeToggle.vue";
import type {UserTokenPayload} from "@/kernel";

defineProps<{
  user: UserTokenPayload | null;
  imageUrl?: string;
}>();

const emit = defineEmits<{
  (e: 'logout'): void;
}>();

const fallbackImage = "/images/default.png";
const isOpen = ref(false);

const toggleDropdown = () => {
  isOpen.value = !isOpen.value;
};

const handleLogout = () => {
  isOpen.value = false;
  emit('logout');
};
</script>
