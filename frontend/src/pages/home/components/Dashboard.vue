<template>
  <main>
    <div
      class="xl:max-w-[100rem] xl:mx-auto xl:h-[calc(100vh-96px)] h-full flex xl:flex-row py-8 xl:px-14 md:px-10 px-5 flex-col items-center justify-between"
    >
      <div class="flex flex-col justify-center xl:w-fit w-full">
        <h1
          class="md:text-5xl text-3xl text-dark-secondary-700 dark:text-dark-tertiary-500 font-normal max-w-lg leading-snug md:whitespace-nowrap"
        >
          Smarter Meetings Start Here
        </h1>
        <p
          class="text-base text-dark-secondary-50 dark:text-dark-tertiary-700 md:text-2xl mt-2 mb-6 max-w-lg"
        >
          Connect, collaborate — and let AI handle the notes, tasks, and follow-ups
        </p>
        <MeetingActions />
        <p class="text-xs text-dark-secondary-600 dark:text-dark-tertiary-700 mt-5">
          ✅ Logged in as
          <span class="text-dark-secondary-50">{{ name }}.&nbsp;</span>
          <span>Ready to collaborate? Your assistant is standing by</span>
        </p>
      </div>
      <div class="flex flex-col gap-4 xl:w-[700px] w-full xl:mt-0 mt-10">
        <MeetingInfoCard />
        <MeetingCard />
        <p class="text-sm text-dark-primary-500 dark:text-dark-primary-300">
          ✨ Click on any ended meeting to view its summary, notes, and tasks — automatically synced with Slack & Trello
        </p>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import {onMounted, ref} from "vue";

import {AuthService} from "@/kernel";

import MeetingActions from "../components/MeetingActions.vue";
import MeetingInfoCard from "../components/MeetingInfoCard.vue";
import MeetingCard from "../components/MeetingCard.vue";

const name = ref("");

onMounted(() => {
  name.value = AuthService.getUserContext()?.name;
});
</script>
