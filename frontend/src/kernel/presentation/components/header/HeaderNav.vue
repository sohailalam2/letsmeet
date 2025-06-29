<template>
  <header
      class="bg-[#fff5ed] dark:bg-dark-secondary-900 border-b border-b-dark-primary-500"
  >
    <div
        class="xl:max-w-[100rem] xl:mx-auto flex justify-between items-center md:px-10 px-5 py-2 text-sm text-gray-700 dark:text-dark-tertiary-500"
    >
      <AppLogo @click="routeToHome"/>
      <div class="flex items-center gap-4">
        <TimeDisplay class="md:inline hidden"/>
        <div class="md:inline hidden">
          <ThemeToggle/>
        </div>
        <LoginButton
            v-if="!isAuthenticated"
            @click="openAuthOverlay"
        />
        <ProfileDropdown
            v-else
            :user="userInfo"
            :image-url="imageUrl"
            @logout="handleLogout"
        />
      </div>
    </div>
  </header>
  <AuthOverlay
      v-if="isAuthOverlayOpen"
      :can-close="CAN_CLOSE_AUTH_OVERLAY"
      :auth-login-uri="authLoginUri"
      :is-loading="isAuthLoading"
      @close="closeAuthOverlay"
  />
</template>

<script setup lang="ts">
import {onMounted, ref, watch} from "vue";
import {useRoute, useRouter} from "vue-router";

import {CAN_CLOSE_AUTH_OVERLAY, useAuth,} from "@/kernel";

import ThemeToggle from "../ThemeToggle.vue";
import AppLogo from "./AppLogo.vue";
import TimeDisplay from "./TimeDisplay.vue";
import LoginButton from "./LoginButton.vue";
import ProfileDropdown from "./ProfileDropdown.vue";
import AuthOverlay from "./AuthOverlay.vue";

const router = useRouter();
const route = useRoute();
const {
  isAuthenticated,
  userInfo,
  imageUrl,
  authLoginUri,
  login,
  logout,
  handleAuthCode
} = useAuth();

const isAuthOverlayOpen = ref(!isAuthenticated.value);
const isAuthLoading = ref(false);

// Handle auth code from URL if present
onMounted(async () => {
  const code = route.query.code as string;
  if (code) {
    try {
      isAuthLoading.value = true;
      await handleAuthCode(code);
      isAuthOverlayOpen.value = false;
      // Remove code from URL to prevent reprocessing on refresh
      await router.replace({query: {}});
    } catch (error) {
      console.error("Authentication failed:", error);
    } finally {
      isAuthLoading.value = false;
    }
  }
});

// Watch authentication state changes
watch(isAuthenticated, (newValue) => {
  if (newValue) {
    isAuthOverlayOpen.value = false;
  }
});

const routeToHome = () => {
  router.push("/");
};

const openAuthOverlay = () => {
  isAuthOverlayOpen.value = true;
};

const closeAuthOverlay = () => {
  isAuthOverlayOpen.value = false;
};

const handleLogout = async () => {
  await logout();
  isAuthOverlayOpen.value = true;
};
</script>
