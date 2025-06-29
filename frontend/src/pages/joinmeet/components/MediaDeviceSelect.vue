<template>
  <div class="media-device-select relative w-full">
    <select
        :id="`${icon}-input-select`"
        :value="modelValue"
        class="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-full focus:outline-none focus:ring-2 focus:ring-primary-500 text-dark-secondary-500 dark:text-dark-tertiary-500 appearance-none"
        @change="$emit('update:modelValue', ($event.target as HTMLSelectElement).value)"
    >
      <option value="">{{ label }}...</option>
      <option
          v-for="device in devices"
          :key="device.deviceId"
          :value="device.deviceId"
      >
        {{ device.label || `${label} ${devices.indexOf(device) + 1}` }}
      </option>
    </select>
    <div class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500">
      <img
          :src="getIconUrl(icon)"
          :alt="`${label} icon`"
          class="w-5 h-5"
      />
    </div>
    <div v-if="hasTestButton" class="absolute right-10 top-1/2 transform -translate-y-1/2">
      <button
          class="text-primary-500 hover:text-primary-600 focus:outline-none"
          @click.prevent="$emit('testDevice')"
          aria-label="Test this device"
      >
      </button>
    </div>
    <div class="absolute right-3 top-1/2 transform -translate-y-1/2 pointer-events-none">
      <svg class="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
      </svg>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  modelValue: string | null;
  devices: MediaDeviceInfo[];
  label: string;
  icon: string;
  hasTestButton?: boolean;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
  (e: 'testDevice'): void;
}>();

const getIconUrl = (iconName: string): string => {
  const iconMap: Record<string, string> = {
    mic: new URL('../../../assets/icons/mic.svg', import.meta.url).href,
    speaker: new URL('../../../assets/icons/speaker.svg', import.meta.url).href,
    camera: new URL('../../../assets/icons/video.svg', import.meta.url).href
  };

  return iconMap[iconName] || '';
};
</script>

<style scoped>
/* Custom styles for the select dropdown */
select {
  background-color: transparent;
}

/* Ensure consistent appearance across browsers */
@media screen and (-webkit-min-device-pixel-ratio: 0) {
  select {
    padding-right: 25px;
  }
}
</style>
