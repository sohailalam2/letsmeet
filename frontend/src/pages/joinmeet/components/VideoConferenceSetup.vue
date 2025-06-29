<template>
  <div>
    <div
        class="video-container rounded-xl overflow-hidden shadow-lg bg-black/90 xl:w-[45rem] h-[26rem] relative xl:mx-0 mx-5 xl:mt-0 mt-10"
    >
      <video
          ref="videoRef"
          autoplay
          playsinline
          :muted="true"
          class="w-full h-full soft-corner border border-white"
      ></video>
      <audio ref="audioTestRef" src="/assets/sounds/speaker-test.mp3" preload="auto"></audio>
      <div
          v-if="!isCameraOn"
          class="absolute inset-0 flex items-center justify-center text-white/90 text-2xl"
          aria-live="polite"
      >
        Camera is off
      </div>
      <div class="absolute bottom-5 w-full flex justify-center gap-4">
        <button
            :class="[
            'p-4 rounded-full cursor-pointer transition-colors',
            isMicOn ? 'bg-white/10 hover:bg-white/20' : 'bg-red-500'
          ]"
            :aria-label="isMicOn ? 'Turn microphone off' : 'Turn microphone on'"
            @click="toggleMic"
        >
          <img
              :src="getAssetUrl(isMicOn ? 'mic.svg' : 'mic-closed.svg')"
              :alt="isMicOn ? 'Microphone on' : 'Microphone off'"
              class="w-[25px] h-[25px]"
          />
        </button>
        <button
            class="p-4 rounded-full cursor-pointer transition-colors bg-white/10 hover:bg-white/20"
            aria-label="Test speakers"
            @click="testSpeakers"
        >
          <img
              :src="getAssetUrl('speaker.svg')"
              alt="Test speakers"
              class="w-[25px] h-[25px]"
          />
        </button>
        <button
            :class="[
            'p-4 rounded-full cursor-pointer transition-colors',
            isCameraOn ? 'bg-white/10 hover:bg-white/20' : 'bg-red-500'
          ]"
            :aria-label="isCameraOn ? 'Turn camera off' : 'Turn camera on'"
            @click="toggleCamera"
        >
          <img
              :src="getAssetUrl(isCameraOn ? 'video.svg' : 'close-video.svg')"
              :alt="isCameraOn ? 'Camera on' : 'Camera off'"
              class="w-[25px] h-[25px]"
          />
        </button>
      </div>
      <div class="absolute top-2 xl:left-2 left-10 text-white text-sm">
        {{ userName }}
      </div>
    </div>
    <div
        class="device-selectors flex items-center justify-between gap-2 xl:w-[45rem] xl:mt-2.5 mt-5 xl:px-0 px-5"
    >
      <MediaDeviceSelect
          v-model="microphone"
          :devices="microphones"
          label="Microphone devices"
          icon="mic"
          @update:model-value="handleMicrophoneChange"
      />
      <MediaDeviceSelect
          v-model="speaker"
          :devices="speakers"
          label="Speaker devices"
          icon="speaker"
          :has-test-button="true"
          @test-device="testSpeakers"
          @update:model-value="handleSpeakerChange"
      />
      <MediaDeviceSelect
          v-model="camera"
          :devices="cameras"
          label="Camera devices"
          icon="camera"
          @update:model-value="handleCameraChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import {onBeforeUnmount, onMounted, ref, watch} from "vue";
import {AuthService, MeetingSessionStore, useMediaDevices, useMediaStream} from "@/kernel";

import MediaDeviceSelect from "./MediaDeviceSelect.vue";

// Store
const store = MeetingSessionStore();

// Refs
const videoRef = ref<HTMLVideoElement | null>(null);
const audioTestRef = ref<HTMLAudioElement | null>(null);
const userName = ref<string>("");

// Media device state
const {
  cameras,
  microphones,
  speakers,
  camera,
  microphone,
  speaker,
  refreshDevices
} = useMediaDevices();

// Media stream state
const {
  isCameraOn,
  isMicOn,
  toggleCamera,
  toggleMic,
  updateDeviceSelection,
  cleanup
} = useMediaStream(videoRef, camera, microphone);

// Helper function to get asset URLs
const getAssetUrl = (filename: string): string => {
  return new URL(`../../../assets/icons/${filename}`, import.meta.url).href;
};

/**
 * Test speakers by playing a sound
 */
const testSpeakers = async () => {
  if (!audioTestRef.value) return;

  try {
    // Stop any currently playing audio
    audioTestRef.value.pause();
    audioTestRef.value.currentTime = 0;

    // Apply selected speaker if supported
    if (speaker.value && 'setSinkId' in audioTestRef.value) {
      try {
        await (audioTestRef.value as any).setSinkId(speaker.value);
      } catch (sinkErr) {
        console.warn('Could not set audio sink ID:', sinkErr);
        // Continue anyway, will use default device
      }
    }

    // Play the test sound
    try {
      await audioTestRef.value.play();
    } catch (playErr) {
      // Handle autoplay restrictions
      if (playErr.name === 'NotAllowedError') {
        alert('Could not play audio test automatically. Browser restrictions require user interaction first.');
      } else {
        console.error('Error playing audio test:', playErr);
        alert('Could not test speakers. Please try again.');
      }
    }
  } catch (err) {
    console.error('Error testing speakers:', err);
    alert('Could not test speakers. This feature may not be supported in your browser.');
  }
};

// Handle device changes
const handleCameraChange = async (deviceId: string) => {
  store.stagingDevices.videoInput = deviceId;
  if (isCameraOn.value) {
    await updateDeviceSelection('video', deviceId);
  }
};

const handleMicrophoneChange = async (deviceId: string) => {
  store.stagingDevices.audioInput = deviceId;
  if (isMicOn.value) {
    await updateDeviceSelection('audio', deviceId);
  }
};

const handleSpeakerChange = async (deviceId: string) => {
  store.stagingDevices.audioOutput = deviceId;

  // Apply audio output device if supported
  if (videoRef.value && 'setSinkId' in videoRef.value) {
    try {
      // TypeScript doesn't recognize setSinkId by default
      await (videoRef.value as any).setSinkId(deviceId);

      // Also update the test audio element
      if (audioTestRef.value && 'setSinkId' in audioTestRef.value) {
        await (audioTestRef.value as any).setSinkId(deviceId);
      }
    } catch (err) {
      console.error('Error setting audio output device:', err);
    }
  }
};

// Watch for store changes
watch(() => store.stagingVideoEnabled, (enabled) => {
  if (enabled !== isCameraOn.value) {
    toggleCamera();
  }
});

watch(() => store.stagingAudioEnabled, (enabled) => {
  if (enabled !== isMicOn.value) {
    toggleMic();
  }
});

// Lifecycle hooks
onMounted(async () => {
  // Get user info
  const userContext = AuthService.getUserContext();
  userName.value = userContext?.name || "Anonymous";

  // Initialize device permissions and enumeration
  await refreshDevices();

  // Set initial state from store
  if (store.stagingVideoEnabled) {
    await toggleCamera();
  }

  if (store.stagingAudioEnabled) {
    await toggleMic();
  }

  // Listen for device changes
  navigator.mediaDevices.addEventListener('devicechange', refreshDevices);

  // Set up audio test element
  if (audioTestRef.value) {
    // Preload the audio
    audioTestRef.value.load();

    // Add event listener for audio test completion
    audioTestRef.value.onended = () => {
      if (audioTestRef.value) {
        audioTestRef.value.currentTime = 0;
      }
    };
  }
});

onBeforeUnmount(() => {
  // Clean up media streams
  cleanup();

  // Remove event listeners
  navigator.mediaDevices.removeEventListener('devicechange', refreshDevices);

  // Clean up audio test element
  if (audioTestRef.value) {
    audioTestRef.value.onended = null;
    audioTestRef.value.pause();
    audioTestRef.value.src = '';
    audioTestRef.value.load(); // Force release of resources
  }
});
</script>

<style scoped>
/* Ensure video fills container properly across browsers */
video {
  object-fit: cover;
}

/* Prevent blue highlight on buttons in mobile browsers */
button {
  -webkit-tap-highlight-color: transparent;
}
</style>
