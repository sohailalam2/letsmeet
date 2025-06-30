<template>
  <div
    class="flex items-center justify-between bg-[#fff5ed] dark:bg-dark-secondary-900 px-6 py-3 border-b border-gray-700"
  >
    <div>
      <h1
        class="xl:text-lg text-sm font-semibold text-dark-secondary-900 dark:text-dark-tertiary-900"
      >
        {{ meetingdetails?.external_meeting_id }}
      </h1>
      <p class="text-xs text-dark-secondary-600 dark:text-dark-tertiary-500">
        {{ DateTime.getFormattedCurrentDate() }}
      </p>
    </div>
    <div class="flex items-center gap-4">
      <div
        class="xl:flex -space-x-2 hidden justify-end w-60 mr-0 ml-auto overflow-hidden"
      >
        <div
          class="xl:flex -space-x-2 hidden text-white"
          id="attendee-count"
        ></div>
        <div
          class="w-8 h-8 rounded-full bg-[#dacdaf] dark:bg-[#242737] border-2 border-gray-900 text-xs flex items-center justify-center text-dark-primary-100"
          v-if="moreUsers"
        >
          +{{ leftAttendeeCount }}
        </div>
      </div>
      <div
        class="bg-[#dacdaf] dark:bg-[#242737] px-4 py-2 text-sm rounded-full md:flex items-center gap-4 hidden cursor-pointer"
        @click="copyToClipBoard"
      >
        <svg
          width="36"
          height="35"
          viewBox="0 0 36 35"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M22.0649 25.4605H24.2505C28.6215 25.4605 32.211 21.8855 32.211 17.5C32.211 13.129 28.636 9.53949 24.2505 9.53949H22.0649"
            stroke="#fa887a"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <path
            d="M13.3948 9.53949H11.2237C6.83818 9.53949 3.26318 13.1145 3.26318 17.5C3.26318 21.8711 6.83818 25.4605 11.2237 25.4605H13.3948"
            stroke="#fa887a"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <path
            d="M11.9473 17.5H23.5262"
            stroke="#fa887a"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
        <button
          class="border-l border-l-dark-primary-100 pl-4 text-base text-dark-primary-100 md:text-xs truncate"
        >
          {{ props.meetingdetails?.id }}
        </button>
      </div>
      <div class="relative" @click="toggleMenu">
        <div
          class="flex items-center gap-2 cursor-pointer bg-[#dacdaf] dark:bg-[#242737] xl:w-full w-44 px-5 py-3 rounded-full"
        >
          <img :src="userdetails?.picture" class="w-8 h-8 rounded-full" />
          <div
            class="flex items-center xl:gap-20 gap-5 text-black dark:text-white"
          >
            <div>
              <p class="xl:text-sm text-[10px] font-medium truncate">
                {{ userdetails?.name }}
              </p>
              <p class="xl:text-xs text-[10px] text-gray-400">
                {{ meetingdetails?.is_host ? "Moderator" : "User" }}
              </p>
            </div>
            <img src="../../../assets/icons/select-more.svg" class="w-6 h-6" />
          </div>
        </div>
        <div
          v-if="menuOpen"
          class="absolute right-0 mt-2 w-60 bg-dark-tertiary-500 rounded shadow-lg text-black z-50"
        >
          <div class="p-4 border-b border-gray-200">
            <p class="font-semibold truncate">{{ userdetails?.name }}</p>
            <p class="text-sm truncate">{{ userdetails?.email }}</p>
          </div>
          <ul class="py-1 flex flex-col items-start justify-start w-full">
            <li
              class="px-7 py-2 hover:bg-gray-100 cursor-pointer w-full"
              @click="logout"
            >
              Logout
            </li>
            <li class="py-2 hover:bg-gray-100 cursor-pointer">
              <ThemeToggle />
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { useRouter } from "vue-router";
import {
  AuthService,
  MeetingSessionStore,
  type AttendeeJoinResponse,
  type MeetingResponse,
  type UserTokenPayload, DateTime, useToast,
} from "@/kernel";

import ThemeToggle from "@/kernel/presentation/components/ThemeToggle.vue";

const {showToast} = useToast();

const menuOpen = ref(false);
const router = useRouter();
const moreUsers = ref(false);
const totalAttendee = ref(MeetingSessionStore().attendeeCount);
const leftAttendeeCount = ref(0);
const toggleMenu = () => {
  menuOpen.value = !menuOpen.value;
};
const props = defineProps<{
  meetingdetails: MeetingResponse | undefined;
  attendeedetails: AttendeeJoinResponse | undefined;
  userdetails: UserTokenPayload | null;
}>();

// 💡 Automatically updates when attendees change
watch(
  () => MeetingSessionStore().attendeeCount,
  () => {
    attendeeCountAndProfileIcon();
  }
);

// See the Total number of attendee on the header with the profile name
function attendeeCountAndProfileIcon() {
  if (totalAttendee.value < MeetingSessionStore().attendeeCount) {
    totalAttendee.value = MeetingSessionStore().attendeeCount;
    if (totalAttendee.value > 5) {
      leftAttendeeCount.value = totalAttendee.value - 5;
      moreUsers.value = true;
      return;
    }
    MeetingSessionStore().allParticipants.forEach((participant) => {
      const attendeeDiv = document.createElement("div");
      attendeeDiv.className = "w-8 h-8 rounded-full border-2 border-gray-900";
      attendeeDiv.style.backgroundColor = getColor(
        `${MeetingSessionStore().attendeeCount}`
      );
      if (participant.picture) {
        const imgEle = document.createElement("img");
        imgEle.className = "my-auto rounded-full h-fit w-fit text-center mx-auto";
        imgEle.src = participant.picture;
        imgEle.style.width = "100%";
        imgEle.style.height = "100%";
        imgEle.style.objectFit;
        attendeeDiv.appendChild(imgEle);
      } else {
        const paraEle = document.createElement("p");
        paraEle.className = "my-auto rounded-full h-fit w-fit text-center mx-auto";
        paraEle.textContent = participant.name.substring(0, 1);
        attendeeDiv.appendChild(paraEle);
      }
      document.getElementById("attendee-count")?.appendChild(attendeeDiv);
    });
  } else {
    let numberOfAttendeeLeave =
      totalAttendee.value - MeetingSessionStore().attendeeCount;
    const childNodes = document.getElementById("attendee-count");
    while (numberOfAttendeeLeave != 0) {
      if (totalAttendee.value < 5) {
        moreUsers.value = false;
      }
      if (childNodes?.firstChild) {
        childNodes?.removeChild(childNodes.firstChild);
      }
      numberOfAttendeeLeave -= 1;
    }
  }
}

// Get the random color according to you name's first letter
function getColor(name: string): string {
  const colors = [
    "#ef4444",
    "#3b82f6",
    "#10b981",
    "#f59e0b",
    "#8b5cf6",
    "#ec4899",
  ];
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash);
  }
  return colors[Math.abs(hash) % colors.length];
}

const logout = async () => {
  await AuthService.logout();
  router.push("/");
};

// Copy the meeting link to the clipboard with this function
const copyToClipBoard = () => {
  if (props.meetingdetails?.id) {
    navigator.clipboard
      .writeText(
        `${window.location.origin}/meetings/${props.meetingdetails?.id}/join`
      )
      .then(() => {
        console.log("Copied to clipboard!");
        showToast("Copied Meeting Url Successfully", "success");
      })
      .catch((err) => {
        showToast(`Something Went Wrong. ${err}`, "error");
        console.error("Failed to copy:", err);
      });
  }
};
</script>
