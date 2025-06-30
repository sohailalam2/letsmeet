<template>
  <div
    class="flex flex-col items-center justify-center pr-8 pl-10 xl:mt-0 mt-10"
  >
    <h2 class="text-3xl font-normal text-black/80 dark:text-dark-tertiary-500">
      {{ meetings?.external_meeting_id }}
    </h2>
    <div
      class="my-4 dark:text-dark-tertiary-500"
      :class="meetings?.participants.length === 0 ? 'hidden' : ''"
    >
      Number of users {{ meetings?.participants.length }}
    </div>
    <p
      class="text-sm text-gray-600 my-4"
      :class="meetings?.participants.length === 0 ? '' : 'hidden'"
    >
      No one else is here
    </p>
    <button
      class="bg-dark-primary-500 text-white text-lg font-bold py-6 px-20 rounded-full mb-4 cursor-pointer disabled:bg-gray-500 disabled:cursor-not-allowed"
      @click="joinTheMeetingNow"
    >
      Join now
    </button>
    <div
      class="space-y-2 text-sm dark:text-white text-center border-2 border-dark-primary-500 px-8 py-2 rounded-full hover:bg-sky-50 hidden"
    >
      <p class="cursor-pointer">More options</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  type AttendeeJoinResponse,
  type MeetingResponse,
  MeetingService,
  useToast,
} from "@/kernel";

const route = useRoute();
const router = useRouter();
const meetings = ref<MeetingResponse | undefined>(undefined);
const joinMeetings = ref<AttendeeJoinResponse | undefined>(undefined);
const meetingId = route.params.meetingId as string;
const { showErrorToast } = useToast();

onMounted(async () => {
  try {
    meetings.value = await MeetingService.getById(meetingId);
    joinMeetings.value = await MeetingService.getJoinInfoByMeetingId(meetingId);
  } catch (error) {
    showErrorToast(`Something Went Wrong, ${error}`);
    throw new Error(`${error}`);
  }
});

const joinTheMeetingNow = async () => {
  await router.push(`/meetings/${meetingId}/meet`);
};
</script>
