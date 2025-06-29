import { defineStore } from "pinia";
import { ref } from "vue";

export const RandomButtonStore = defineStore("randombuttonstore", () => {
  const toggleChat = ref<boolean | null>(false);
  const toggleSummaryOverlay = ref<boolean | null>(true);

  return { toggleChat, toggleSummaryOverlay };
});
