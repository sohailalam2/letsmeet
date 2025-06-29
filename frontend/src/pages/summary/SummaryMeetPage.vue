<template>
  <TwoColumnLayout>
    <SummaryVideoPreview />
    <TaskSummaryToggle />
  </TwoColumnLayout>
</template>

<script setup lang="ts">
import TwoColumnLayout from "@/kernel/presentation/components/TwoColumnLayout.vue";
import SummaryVideoPreview from "./components/SummaryVideoPreview.vue";
import TaskSummaryToggle from "./components/TaskSummaryToggle.vue";
</script>
