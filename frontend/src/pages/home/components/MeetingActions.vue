<template>
  <div
      class="flex flex-col sm:flex-row gap-2 items-center mt-6 border-b dark:border-b-dark-tertiary-500 pb-6"
  >
    <button
        class="w-full sm:w-auto bg-dark-primary-500 text-dark-tertiary-500 p-4 rounded-full flex items-center gap-2.5 md:text-base text-xs cursor-pointer font-medium"
        @click="toggleCreateMeetOverlay"
    >
      <img
          src="../../../assets/icons/video.svg"
          class="w-5 h-5"
          alt="create new video meeting"
      />
      Start a Smart Meeting
    </button>
    <div
        class="w-full sm:w-auto border-2 border-dark-primary-500 dark:text-dark-tertiary-500 rounded-full md:w-[22rem] w-56 flex items-center justify-between pl-4 pr-2.5 py-2.5"
    >
      <input
          v-model="joiningId"
          type="text"
          placeholder="Enter a code or link"
          class="focus:outline-none w-full truncate md:placeholder:text-base placeholder:text-sm"
      />
      <button
          class="text-dark-tertiary-500 md:text-lg text-xs font-medium bg-dark-primary-500 px-5 py-1 rounded-full cursor-pointer"
          @click="joinTheMeetWithJoin"
      >
        Join
      </button>
    </div>
  </div>
  <section>
    <!--  Overlay for creating new meeting  -->
    <div
        v-if="overlayOpen"
        class="fixed inset-0 bg-black/60 dark:bg-black/85 rounded shadow-lg text-black z-50"
    >
      <div
          class="flex justify-self-center items-center overlay-std bg-white dark:bg-dark-tertiary-500 my-auto mx-auto translate-y-1/2"
      >
        <p
            class="absolute right-5 top-5 text-black text-2xl cursor-pointer"
            @click="toggleCreateMeetOverlay"
        >
          <img
              src="../../../assets/icons/close.svg"
              class="bg-black/50 rounded-full w-7 h-7"
              alt="close-overlay"
          />
        </p>
        <div class="w-full h-full flex flex-col justify-center items-center">
          <img
              class="w-20 h-20 rounded-full"
              src="../../../assets/icons/logo.svg"
              alt="Banding Logo"
          />
          <span v-if="!toggleTheOverlayInput">
            <h3 class="text-2xl font-medium text-black/60 text-center mt-8">
            Create Your Smart Meeting
            </h3>
            <p class="text-black/50">Name your meeting to get started</p>
          </span>
          <span v-if="toggleTheOverlayInput">
            <h3 class="text-2xl font-medium text-black/60 text-center mt-5">
            Join Your Smart Meeting
            </h3>
            <p class="text-black/50">Share the meeting link and get started</p>
          </span>
          <div class="mt-8">
            <div
                v-if="!toggleTheOverlayInput"
                class="flex items-center border-2 border-dark-primary-500 soft-corner p-2"
            >
              <div>
                <input
                    v-model="externalMeetingId"
                    type="text"
                    placeholder="What's this meeting about?"
                    class="focus:outline-none"
                />
                <p
                    v-if="notEnterTheTitle"
                    class="text-red-600 text-sm font-medium mt-2 ml-2.5 w-60"
                >
                  {{ errorMessage }}
                </p>
              </div>
              <button
                  class="bg-dark-primary-500 font-medium p-4 soft-corner text-dark-tertiary-500 ml-2 cursor-pointer"
                  @click="createANewMeeting"
              >
                Create
              </button>
            </div>
            <div
                v-if="toggleTheOverlayInput"
                class="flex items-start border-2 border-dark-primary-500 p-2 soft-corner"
            >
              <div class="">
                <h4 class="text-sm font-semibold">
                  {{ meetings?.external_meeting_id }}
                </h4>
                <p class="soft-corner w-64 text-xs text-gray-600">
                  {{ meetings?.join_url.split("/")?.[4] }}
                </p>
              </div>
              <button
                  class=" bg-dark-primary-500 p-3 soft-corner text-dark-tertiary-500 text-base ml-2  font-semibold cursor-pointer"
                  v-if="toggleTheOverlayInput"
                  :class="clicked ? 'scale-90' : ''"
                  @click="copyToClipBoard"
              >
                Copy
              </button>
            </div>
            <button
                class="mt-2.5 bg-dark-primary-500 font-medium w-full py-3 soft-corner text-dark-tertiary-500 cursor-pointer"
                v-if="toggleTheOverlayInput"
                @click="joinTheCreatedMeeting"
            >
              Join
            </button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import {ref} from "vue";
import {useRouter} from "vue-router";
import {type MeetingResponse, MeetingService, useToast} from "@/kernel";

const router = useRouter();
const { showInfoToast, showSuccessToast, showErrorToast } = useToast();

const externalMeetingId = ref<string | undefined>(undefined);
let overlayOpen = ref(false);
let notEnterTheTitle = ref(false);
let toggleTheOverlayInput = ref(false);
const meetings = ref<MeetingResponse | undefined>(undefined);
const errorMessage = ref("Please Enter The Title*");
const joiningId = ref("");
const toggleCreateMeetOverlay = () => {
  overlayOpen.value = !overlayOpen.value;
  notEnterTheTitle.value = false;
};
const createANewMeeting = async () => {
  if (externalMeetingId.value) {
    try {
      showInfoToast("Just a second, creating your new meeting");
      const newMeetingRes = await MeetingService.create(
          externalMeetingId.value
      );
      meetings.value = newMeetingRes;
      toggleTheOverlayInput.value = !toggleTheOverlayInput.value;
      showSuccessToast("Meeting Session Created Successfully");
    } catch (error) {
      notEnterTheTitle.value = true;
      showErrorToast(`Something Went Wrong. Please try again error:${error}`);
      errorMessage.value = "Something went wrong. Please try again after some time";
      throw new Error(`${error}`);
    }
  } else {
    notEnterTheTitle.value = true;
  }
};

const joinTheCreatedMeeting = () => {
  router.push(`/meetings/${meetings.value?.id}/join`);
};

const clicked = ref(false);

const copyToClipBoard = () => {
  if (meetings.value?.id) {
    clicked.value = true;
    navigator.clipboard
        .writeText(`${window.location.origin}/meetings/${meetings.value?.id}/join`)
        .then(() => {
          showSuccessToast("Copied. Now share your meeting url!");
        })
        .catch((err) => {
          showErrorToast(`Something Went Wrong. Please try again error: ${err}`);
        });
  }
};

const joinTheMeetWithJoin = async () => {
  let joinId = joiningId.value.trim();

  if (!joinId) {
    showErrorToast("Please enter a valid meeting id or url");
    return;
  }

  joinId = joinId.includes('/meetings/')
      ? joinId.split('/meetings/')[1].split('/')[0]
      : joinId;

  await router.push(`/meetings/${joinId}/join`);
};
</script>
