<template>
  <div
    class="flex items-center justify-between bg-[#dacdaf] dark:bg-dark-secondary-500 md:p-4 p-2.5"
  >
    <div class=""></div>
    <div class="flex justify-center md:gap-6 gap-4">
      <button
        class="bg-black/50 p-3 rounded-full cursor-pointer"
        @click="toggleAudio"
      >
        <img
          src="../../../assets/icons/mic-closed.svg"
          class="w-6 h-6"
          v-if="!toggleMic"
          id="mic-close"
        />
        <img
          src="../../../assets/icons/mic.svg"
          class="w-6 h-6"
          v-if="toggleMic"
          id="mic"
        />
      </button>
      <button
        class="bg-black/50 p-3 rounded-full cursor-pointer"
        @click="toggleVideo"
      >
        <img
          src="../../../assets/icons/video.svg"
          class="w-6 h-6"
          v-if="toggleCamera"
          id="video"
        />
        <img
          src="../../../assets/icons/close-video.svg"
          class="w-6 h-6"
          v-if="!toggleCamera"
          id="close-video"
        />
      </button>
      <button
        class="bg-black/50 p-3 rounded-full cursor-pointer"
        @click="toggleScreenShare"
      >
        <img
          src="../../../assets/icons/screen-share-close.svg"
          class="w-6 h-6"
          v-if="!toggleShare"
          id="close-share"
        />
        <img
          src="../../../assets/icons/screen-share-open.svg"
          class="w-6 h-6"
          v-if="toggleShare"
          id="show-share"
        />
      </button>
      <button
        class="bg-black/50 p-3 rounded-full cursor-pointer"
        :class="IS_DEVELOPMENT ? 'hidden' : ''"
        @click="toggleChat"
      >
        <img src="../../../assets/icons/message.svg" class="w-6 h-6" />
      </button>
      <button
        class="bg-black/50 p-3 rounded-full cursor-pointer relative"
        @click="toggleMenu"
      >
        <img src="../../../assets/icons/select-more.svg" class="w-6 h-6" />
        <div
          class="absolute left-10 bottom-5 p-5 w-96 rounded-sm bg-dark-tertiary-500 dark:bg-dark-secondary-600"
          v-if="isMenuActive"
        >
          <div class="p-4 space-y-6">
            <!-- Microphones -->
            <div>
              <h3 class="font-semibold text-lg mb-2">Microphone</h3>
              <div class="space-y-2">
                <div
                  v-for="device in audioInputDevices"
                  :key="device.deviceId"
                  @click="selectAudioInput(device.deviceId)"
                  :class="[
                    'cursor-pointer p-2 rounded border flex items-center',
                    selectedAudioInput === device.deviceId
                      ? 'bg-blue-100 border-blue-500 text-blue-700 font-semibold'
                      : 'hover:bg-gray-100',
                  ]"
                >
                  <span
                    v-if="selectedAudioInput === device.deviceId"
                    class="mr-2"
                    >✅</span
                  >
                  <span>{{ device.label || "Unnamed Microphone" }}</span>
                </div>
              </div>
            </div>

            <!-- Cameras -->
            <div>
              <h3 class="font-semibold text-lg mb-2">Camera</h3>
              <div class="space-y-2">
                <div
                  v-for="device in videoInputDevices"
                  :key="device.deviceId"
                  @click="selectVideoInput(device.deviceId)"
                  :class="[
                    'cursor-pointer p-2 rounded border flex items-center',
                    selectedVideoInput === device.deviceId
                      ? 'bg-green-100 border-green-500 text-green-700 font-semibold'
                      : 'hover:bg-gray-100',
                  ]"
                >
                  <span
                    v-if="selectedVideoInput === device.deviceId"
                    class="mr-2"
                    >✅</span
                  >
                  <span>{{ device.label || "Unnamed Camera" }}</span>
                </div>
              </div>
            </div>

            <!-- Speakers -->
            <div>
              <h3 class="font-semibold text-lg mb-2">Speaker</h3>
              <div class="space-y-2">
                <div
                  v-for="device in audioOutputDevices"
                  :key="device.deviceId"
                  @click="selectAudioOutput(device.deviceId)"
                  :class="[
                    'cursor-pointer p-2 rounded border flex items-center',
                    selectedAudioOutput === device.deviceId
                      ? 'bg-yellow-100 border-yellow-500 text-yellow-700 font-semibold'
                      : 'hover:bg-gray-100',
                  ]"
                >
                  <span
                    v-if="selectedAudioOutput === device.deviceId"
                    class="mr-2"
                    >✅</span
                  >
                  <span>{{ device.label || "Unnamed Speaker" }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </button>
    </div>
    <button
      class="bg-dark-primary-500 hover:bg-dark-primary-700 py-3 px-5 rounded-full cursor-pointer w-28"
      :class="props.meetingdetails?.is_host ? '' : 'hidden'"
      @click="toggleExitOverlayButton"
    >
      End Call
      <div
        v-if="toogleExitOverlay"
        class="absolute bottom-16 right-10 w-40 h-40 soft-corner bg-sky-100 text-black flex flex-col gap-5 p-5"
      >
        <button
          @click="leaveTheCall"
          class="px-4 py-2 rounded-full bg-dark-secondary-600 hover:bg-dark-secondary-700 text-dark-tertiary-600 cursor-pointer"
        >
          Leave Call
        </button>
        <button
          @click="EndTheCall"
          class="px-4 py-2 rounded-full bg-dark-primary-400 hover:bg-dark-primary-700 text-dark-tertiary-600 cursor-pointer"
        >
          End Call
        </button>
      </div>
    </button>
    <button
      @click="leaveTheCall"
      class="bg-dark-primary-500 hover:bg-dark-primary-700 py-3 px-1 rounded-full cursor-pointer w-28"
      :class="props.meetingdetails?.is_host ? 'hidden' : ''"
    >
      Leave Call
    </button>
  </div>
</template>

<script setup lang="ts">
// call control logic can be added here later
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import {
  MeetingService,
  MeetingSessionStore,
  RandomButtonStore,
  type AttendeeJoinResponse,
  type MeetingResponse,
  type UserTokenPayload, IS_DEVELOPMENT, useToast,
} from "@/kernel";

const props = defineProps<{
  meetingdetails: MeetingResponse | undefined;
  attendeedetails: AttendeeJoinResponse | undefined;
  userdetails: UserTokenPayload | null;
}>();
const router = useRouter();
const {showInfoToast, showSuccessToast, showErrorToast} = useToast();

const toogleExitOverlay = ref(false);
const isMenuActive = ref(false);
const toggleMic = computed(() => MeetingSessionStore().audioEnabled);
const toggleCamera = computed(() => MeetingSessionStore().videoEnabled);
const toggleShare = computed(() => MeetingSessionStore().screenShareEnabled);

const toggleExitOverlayButton = () => {
  toogleExitOverlay.value = !toogleExitOverlay.value;
};
const leaveTheCall = async () => {
  if (props.meetingdetails) {
    router.push("/");
    try {
      MeetingSessionStore().meetingSession?.audioVideo.stop();
      MeetingSessionStore().meetingSession = null;
      MeetingSessionStore().audioVideo = null;
      MeetingSessionStore().currentMeeting = null;
      MeetingSessionStore().videoTiles = {};
      await MeetingService.leave(props.meetingdetails.id);
      showInfoToast("You leaved the meeting");
    } catch (error) {
      showErrorToast("You leaved the meeting but got some error");
      throw new Error(`${error}`);
    }
  }
};

const EndTheCall = async () => {
  if (
    props.meetingdetails &&
    confirm("Are you sure you want to end this meeting for all participants?")
  ) {
    router.push("/");
    try {
      MeetingSessionStore().meetingSession?.audioVideo.stop();
      MeetingSessionStore().meetingSession = null;
      MeetingSessionStore().audioVideo = null;
      MeetingSessionStore().currentMeeting = null;
      MeetingSessionStore().videoTiles = {};
      await MeetingService.end(props.meetingdetails.id);
      showSuccessToast("You ended the meeting");
    } catch (error) {
      showErrorToast("You end the meeting but got some error.Or Meeting is not ended.");
      throw new Error(`${error}`);
    }
  }
};

const toggleScreenShare = async () => {
  const audioVideo = MeetingSessionStore().audioVideo as any;
  if (!audioVideo) return;

  try {
    if (MeetingSessionStore().screenShareEnabled) {
      audioVideo.stopContentShare();
      showSuccessToast("Screen Sharing Stop Successfully");
    } else {
      // stop camera if it's on
      if (audioVideo.isLocalVideoStarted?.()) {
        audioVideo.stopLocalVideoTile();
        MeetingSessionStore().videoEnabled = false;
      }

      await audioVideo.startContentShareFromScreenCapture();
      showSuccessToast("Screen Sharing Started Successfully");
    }

    MeetingSessionStore().screenShareEnabled =
      !MeetingSessionStore().screenShareEnabled;
  } catch (err) {
    console.error("Error toggling screen share:", err);
    showErrorToast(`Error toggling screen share: ${err}`);
    alert("Unable to start screen sharing. Please check permissions.");
  }
};

const toggleVideo = async () => {
  const store = MeetingSessionStore();
  const audioVideo = store.audioVideo;
  if (!audioVideo) return;

  try {
    if (store.videoEnabled) {
      audioVideo.stopLocalVideoTile();
      await audioVideo.stopVideoInput(); // releases camera access
      showSuccessToast("Camera disabled");
    } else {
      // Ensure camera device is selected
      let deviceId = store.stagingDevices.videoInput;
      if (!deviceId) {
        const devices = await audioVideo.listVideoInputDevices();
        if (devices.length > 0) {
          deviceId = devices[0].deviceId;
          store.stagingDevices.videoInput = deviceId;
        } else {
          throw new Error("No video input devices available");
        }
      }

      await audioVideo.startVideoInput(deviceId);
      audioVideo.startLocalVideoTile(); // now bind the video tile
      showSuccessToast("Camera enabled");
    }

    store.videoEnabled = !store.videoEnabled;
  } catch (err) {
    console.error("Camera toggle error:", err);
    showErrorToast(`Camera error: ${err}`);
  }
};

const toggleAudio = async () => {
  const store = MeetingSessionStore();
  const audioVideo = store.audioVideo;
  if (!audioVideo) return;

  try {
    if (store.audioEnabled) {
      // Mute mic and optionally stop audio input if you want to release mic
      audioVideo.realtimeMuteLocalAudio();
      await audioVideo.stopAudioInput(); // optional
      showSuccessToast("Microphone disabled");
    } else {
      // Start mic with selected device or fallback
      let deviceId = store.stagingDevices.audioInput;
      if (!deviceId) {
        const devices = await audioVideo.listAudioInputDevices();
        if (devices.length > 0) {
          deviceId = devices[0].deviceId;
          store.stagingDevices.audioInput = deviceId;
        } else {
          throw new Error("No audio input devices available");
        }
      }

      await audioVideo.startAudioInput(deviceId);
      audioVideo.realtimeUnmuteLocalAudio();
      showSuccessToast("Microphone enabled");
    }

    store.audioEnabled = !store.audioEnabled;
  } catch (err) {
    console.error("Mic toggle error:", err);
    showErrorToast(`Mic error: ${err}`);
  }
};

const toggleChat = () => {
  RandomButtonStore().toggleChat = !RandomButtonStore().toggleChat;
};

const toggleMenu = () => {
  isMenuActive.value = !isMenuActive.value;
};

import { onMounted } from "vue";
const meetingSession = MeetingSessionStore().meetingSession;

const audioInputDevices = ref<MediaDeviceInfo[]>([]);
const videoInputDevices = ref<MediaDeviceInfo[]>([]);
const audioOutputDevices = ref<MediaDeviceInfo[]>([]);

const selectedAudioInput = ref("");
const selectedVideoInput = ref("");
const selectedAudioOutput = ref("");

const fetchDevices = async () => {
  if (!meetingSession?.audioVideo) return;

  // Use individual device listing methods
  audioInputDevices.value =
    (await meetingSession.audioVideo.listAudioInputDevices()) || [];
  videoInputDevices.value =
    (await meetingSession.audioVideo.listVideoInputDevices()) || [];
  audioOutputDevices.value =
    (await meetingSession.audioVideo.listAudioOutputDevices()) || [];

  // Get currently active devices
  const audioInputId = await meetingSession.audioVideo
    .getCurrentMeetingAudioStream()
    .then((df) => {
      return df?.id;
    });
  const videoInputId = meetingSession.audioVideo
    .getLocalVideoTile()
    ?.id()
    .toString();
  const audioOutputId = (
    await meetingSession.audioVideo.listAudioOutputDevices()
  ).filter((ot) => ot.kind === "audioinput")[0].deviceId;

  if (audioInputId) selectedAudioInput.value = audioInputId;
  if (videoInputId) selectedVideoInput.value = videoInputId;
  if (audioOutputId) selectedAudioOutput.value = audioOutputId;
};

const selectAudioInput = async (deviceId: string) => {
  await meetingSession?.audioVideo?.startAudioInput(deviceId);
  selectedAudioInput.value = deviceId;
};

const selectVideoInput = async (deviceId: string) => {
  await meetingSession?.audioVideo?.startVideoInput(deviceId);
  selectedVideoInput.value = deviceId;
};

const selectAudioOutput = async (deviceId: string) => {
  await meetingSession?.audioVideo?.chooseAudioOutput(deviceId);
  selectedAudioOutput.value = deviceId;
};

onMounted(() => {
  fetchDevices();
});
</script>
