<template>
  <div class="xl:max-w-3xl max-w-full">
    <div
      ref="scrollContainer"
      class="overflow-y-auto max-h-38 space-y-3 relative pb-7 scrollbar-hidden"
      @scroll="checkVisibleCards"
    >
      <div
        v-for="(meeting, index) in meetings"
        :key="meeting.id"
        :ref="el => cardRefs[index] = el as HTMLElement"
        :class="[
          'rounded-xl shadow-md p-4 flex items-center justify-between transition-colors duration-300',
          index === activeCardIndex
            ? 'bg-blue-100 cursor-pointer'
            : 'bg-white/50 blur-[1px]',
        ]"
        @click="() => index === activeCardIndex && joinTheMeeting(meeting)"
      >
        <span class="text-sm text-dark-secondary-400">{{
          formatTime(meeting.created_at)
        }}</span>
        <p class="font-medium text-sm truncate max-w-[50%]">
          {{ meeting.external_meeting_id }}
        </p>
        <button
          class="text-xs rounded px-2 py-1"
          :class="
            meeting.is_ended
              ? 'text-dark-primary-400 border border-dark-primary-400'
              : 'text-blue-600 border border-blue-600'
          "
        >
          {{ meeting.is_ended ? "Ended" : "Join" }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from "vue";
import { onBeforeRouteUpdate, useRouter } from "vue-router";
import {MeetingService, type MeetingResponse, useToast} from "@/kernel";

const meetings = ref<MeetingResponse[]>([]);
const cardRefs = ref<HTMLElement[]>([]);
const scrollContainer = ref<HTMLElement | null>(null);
const activeCardIndex = ref<number>(-1);
const router = useRouter();

const {showErrorToast} = useToast();

async function fetchTheMeetings() {
  const token = localStorage.getItem("access_token");
  if (!token) {
    return;
  }
  try {
    const res = await MeetingService.getAll();
    meetings.value = res.meetings;
    await nextTick();
    checkVisibleCards();
  } catch (err) {
    showErrorToast(`Something wrong while getting the meetings. API:${err}`);
    console.error("Failed to fetch meetings:", err);
  }
}

onMounted(async () => {
  await fetchTheMeetings();
});

onBeforeRouteUpdate((_to, _from, next) => {
  fetchTheMeetings().then(() => next());
});

function formatTime(timestampInSeconds: number): string {
  return new Date(timestampInSeconds * 1000).toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  });
}

function checkVisibleCards() {
  if (!scrollContainer.value) return;
  const containerHeight = scrollContainer.value.clientHeight;

  let bestIndex = -1;
  let closestTop = Infinity;

  cardRefs.value.forEach((el, idx) => {
    if (!el) return;
    const rect = el.getBoundingClientRect();
    const parentRect = scrollContainer.value!.getBoundingClientRect();
    const top = rect.top - parentRect.top;
    const visible = top >= 0 && top < containerHeight;
    if (visible && top < closestTop) {
      closestTop = top;
      bestIndex = idx;
    }
  });

  activeCardIndex.value = bestIndex;
}

const joinTheMeeting = (meeting: MeetingResponse) => {
  if (meeting.is_ended) {
    router.push(`/meetings/${meeting.id}/summary`);
  } else {
    router.push(`/meetings/${meeting.id}/join`);
  }
};
</script>
