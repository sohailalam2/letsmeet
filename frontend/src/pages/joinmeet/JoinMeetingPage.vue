<template>
  <TwoColumnLayout>
    <VideoConferenceSetup />
    <JoinOptions />
  </TwoColumnLayout>
</template>

<script setup lang="ts">
import TwoColumnLayout from "@/kernel/presentation/components/TwoColumnLayout.vue";
import VideoConferenceSetup from './components/VideoConferenceSetup.vue'
import JoinOptions from './components/JoinOptions.vue'
</script>
