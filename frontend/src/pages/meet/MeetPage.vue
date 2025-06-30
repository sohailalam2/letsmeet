<template>
  <div
    class="bg-[#fff6f3] dark:bg-dark-secondary-900 text-white h-screen overflow-hidden"
  >
    <!-- Full-height container -->
    <div class="flex flex-col h-full">
      <!-- HeaderBar takes natural height -->
      <HeaderBar
        :meetingdetails="meetings"
        :attendeedetails="attendeeMeetings"
        :userdetails="userDetails"
      />

      <!-- Main body: video + controls + chat (full remaining height) -->
      <div class="flex flex-1 overflow-hidden">
        <!-- Left side: video + call controls -->
        <div
          class="w-full flex flex-col h-full"
          :class="isChatOpen ? 'xl:flex hidden' : ''"
        >
          <!-- Participant video takes up MOST of the space -->
          <ParticipantVideo
            class="flex-grow overflow-hidden"
            :meetingdetails="meetings"
            :attendeedetails="attendeeMeetings"
            :userdetails="userDetails"
          />

          <!-- CallControls take natural height -->
          <div class="shrink-0">
            <CallControls
              :meetingdetails="meetings"
              :attendeedetails="attendeeMeetings"
              :userdetails="userDetails"
            />
          </div>
        </div>

        <!-- ChatPanel section -->
        <div
          class="xl:w-[350px] xl:p-0 w-full border-l border-gray-700 relative overflow-hidden"
          :class="isChatOpen ? '' : 'hidden'"
        >
          <div
            class="text-black absolute right-5 top-2.5 z-50 md:hidden"
            @click="toggleChat"
          >
            close
          </div>
          <ChatPanel
            :meetingdetails="meetings"
            :attendeedetails="attendeeMeetings"
            :userdetails="userDetails"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";

import {
  MeetingService,
  type AttendeeJoinResponse,
  type MeetingResponse,
  type UserTokenPayload, AuthService, useToast,
  RandomButtonStore,
} from "@/kernel";

import HeaderBar from "./components/HeaderBar.vue";
import ParticipantVideo from "./components/ParticipantVideo.vue";
import CallControls from "./components/CallControls.vue";
import ChatPanel from "./components/ChatPanel.vue";

const route = useRoute();
const {showErrorToast} = useToast();

const attendeeMeetings = ref<AttendeeJoinResponse | undefined>(undefined);
const userDetails = ref<UserTokenPayload | null>(null);
const meetings = ref<MeetingResponse | undefined>(undefined);
const isChatOpen = ref(false);
const buttonState = RandomButtonStore();
const meetingId = route.params.meetingId as string;

watch(
  () => buttonState.toggleChat,
  (newVal, oldVal) => {
    if (newVal != oldVal) {
      isChatOpen.value = !isChatOpen.value;
    }
  }
);

userDetails.value = AuthService.getUserContext();

onMounted(async () => {
  try {
    meetings.value = await MeetingService.getById(meetingId);
    attendeeMeetings.value = await MeetingService.join(meetingId);
  } catch (error) {
    showErrorToast(`Something Went Wrong: ${error}. Can you refresh the page.`);
    console.error(error);
    throw new Error(`${error}`);
  }
});

const toggleChat = () => {
  RandomButtonStore().toggleChat = !RandomButtonStore().toggleChat;
};
</script>
