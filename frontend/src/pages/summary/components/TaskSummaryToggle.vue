<template>
  <div class="xl:translate-y-1/3 h-fit">
    <div
        class="flex flex-col relative bg-black/90 text-white xl:mx-0 mx-5 xl:mt-0 mt-10 rounded-2xl border border-white p-1 xl:w-[39rem] h-[26rem]"
    >
      <!-- Tabs -->
      <div class="flex space-x-1 bg-black/90 p-1 rounded-t-lg">
        <button
            :class="tab === 'Summary' ? activeTabClass : inactiveTabClass"
            class="flex-1 py-2 rounded-md text-sm font-medium"
            @click="tab = 'Summary'"
        >
          Summary
        </button>
        <button
            :class="tab === 'Task' ? activeTabClass : inactiveTabClass"
            class="flex-1 py-2 rounded-md text-sm font-medium"
            @click="tab = 'Task'"
        >
          Task
        </button>
      </div>

      <!-- Chats -->
      <div class="overflow-y-scroll px-4 py-2 space-y-4">
        <div
            v-for="(task, index) in summaryRes?.action_items"
            :key="index"
            class="flex items-start gap-3 w-full"
            :class="tab === 'Task' ? '' : 'hidden'"
        >
          <div
              class="w-8 h-8 rounded-full flex-shrink-0 text-center text-base pt-1 text-dark-secondary-400"
              :style="{ backgroundColor: getColor(task.assignee_name) }"
          >
            <p class="my-auto h-fit w-fit text-center mx-auto">
              {{ (task.assignee_name || 'Unassigned').substring(0, 1) }}
            </p>
          </div>
          <div class="bg-gray-800 p-3 rounded-xl w-full">
            <div class="flex justify-between items-center mb-1">
              <span class="text-sm font-semibold"
              >{{ task.assignee_name || 'Unassigned' }} | {{ task.assignee_email || 'Unassigned' }}</span
              >
            </div>
            <div class="text-sm text-white">{{ task.task }}</div>
            <!-- <div class="text-xs text-white" v-for="details in task.breakdown">
            <p>{{ details }}</p>
          </div> -->
          </div>
        </div>
        <div
            class="w-full h-[20rem] overflow-y-scroll"
            :class="tab === 'Summary' ? '' : 'hidden'"
        >
          <h4
              class="border-b border-b-dark-primary-500 text-2xl font-bold pb-2.5"
              v-if="summaryRes?.summary.summary"
          >
            Summary
          </h4>
          <p class="pt-2.5 tracking-wider leading-6">
            {{ summaryRes?.summary.summary }}
          </p>
          <h5
              class="border-b border-b-dark-primary-500 pb-2 my-2.5 text-xl font-medium"
              v-if="summaryRes?.summary.meeting_notes"
          >
            Meeting Notes
          </h5>
          <ul
              v-for="list in summaryRes?.summary.meeting_notes"
              class="list-disc leading-7"
          >
            <li># {{ list }}</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
  <div
      class="fixed inset-0 bg-black/60 flex justify-center z-50"
      v-if="showOverlay"
  >
    <div class="overlay-std text-center p-8 bg-white translate-y-1/2">
      <h2 class="text-xl font-bold text-dark-secondary-800">Just a moment</h2>
      <p class="text-dark-secondary-200 mt-2">We are generating your summary</p>
      <div class="flex justify-center items-center mt-10">
        <div
            class="w-40 h-40 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"
        ></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from "vue";
import {useRoute} from "vue-router";

import {
  FaultTolerance,
  MeetingService,
  RandomButtonStore,
  useToast,
  type GeneratedMeetingInfoDTO,
} from "@/kernel";

const showOverlay = ref(true);
const { showErrorToast } = useToast();

const tab = ref<"Task" | "Summary">("Summary");

const activeTabClass = "bg-gray-700 text-white";
const inactiveTabClass = "bg-gray-900 text-gray-400";

function getColor(name: string = 'Unassigned'): string {
  const colors = [
    "#ef4444",
    "#3b82f6",
    "#10b981",
    "#f59e0b",
    "#8b5cf6",
    "#ec4899",
  ];
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash);
  }
  return colors[Math.abs(hash) % colors.length];
}

let summaryRes = ref<GeneratedMeetingInfoDTO | null>();
const route = useRoute();
const meetingId = route.params.meetingId as string;
const service = MeetingService.getSummary(meetingId);

onMounted(async () => {
  try {
    const result = await FaultTolerance.retry(() => service);

    if (result) {
      RandomButtonStore().toggleSummaryOverlay = false;
    }
    summaryRes.value = result;
    showOverlay.value = false;
  } catch (error) {
    showErrorToast(`Something Went Wrong: ${error}. Can you refresh the page.`);
    console.error(error);
    throw new Error(`${error}`);
  }
});
</script>
