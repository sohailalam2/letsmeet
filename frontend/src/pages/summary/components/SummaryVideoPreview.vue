<template>
  <div class="xl:translate-y-1/3 h-fit">
    <div
      class="rounded-xl overflow-hidden shadow-lg bg-black/90 xl:w-[39rem] h-[26rem] relative xl:mx-0 mx-5 xl:mt-0 mt-10 p-2.5"
    >
      <video
        :src="videoRef"
        controls
        class="w-full h-full soft-corner border border-white"
      ></video>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import {MeetingService, useToast} from "@/kernel";

const videoRef = ref("");
const route = useRoute();
const { showErrorToast } = useToast();

const meetingId = route.params.meetingId as string;

onMounted(async () => {
  try {
    if (!videoRef.value) {
      videoRef.value = (
        await MeetingService.getRecording(meetingId)
      ).signed_url;
    }
  } catch (error) {
    showErrorToast(`Something Went Wrong: ${error}. Can you refresh the page.`);
    console.error(error);
    throw new Error(`${error}`);
  }
});
</script>
