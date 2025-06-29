<template>
  <section>
    <div
        class="fixed inset-0 bg-dark-secondary-600 rounded shadow-lg text-black z-50 flex justify-center items-center"
    >
      <div class="overlay-std bg-white relative">
        <button
            v-if="canClose"
            class="absolute right-5 top-5 text-black text-2xl cursor-pointer"
            @click="$emit('close')"
            aria-label="Close authentication overlay"
        >
          <img
              src="@/assets/icons/close.svg"
              class="bg-black/50 rounded-full w-7 h-7"
              alt="close-overlay"
          />
        </button>

        <div class="w-full h-full flex flex-col justify-center items-center">
          <div v-if="isLoading" class="absolute inset-0 bg-white/80 flex items-center justify-center z-10">
            <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-dark-primary-600"></div>
          </div>
          <img
              class="w-20 h-20 rounded-full"
              src="@/assets/icons/logo.svg"
              alt="LetsMeet Logo"
          />
          <h2 class="text-3xl font-bold text-black/60 text-center mt-8">
            LetsMeet
          </h2>
          <h3 class="text-xl text-black/50 text-center mt-5">
            Where Conversations Become Action
          </h3>
          <p class="text-black/40 text-center mt-3">AI-crafted meeting notes, Slack integration</p>
          <p class="text-black/40 text-center mt-3">Auto assigned tasks in Trello</p>

          <div class="mt-8">
            <a
                :href="authLoginUri"
                class="border border-black/70 soft-corner px-5 py-2.5 flex items-center gap-2.5 hover:bg-gray-100 transition-colors"
            >
              <img
                  src="@/assets/icons/google.svg"
                  class="w-6 h-6"
                  alt="Google logo"
              />
              LogIn with Google
            </a>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  canClose: boolean;
  authLoginUri?: string;
  isLoading?: boolean;
}>();

defineEmits<{
  (e: 'close'): void;
}>();
</script>
