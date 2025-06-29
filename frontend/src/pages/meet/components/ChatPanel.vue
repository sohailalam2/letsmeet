<template>
  <div
    class="flex flex-col justify-between h-full relative bg-[#fff6f3] dark:bg-dark-secondary-900 text-white"
  >
    <div class="flex-1">
      <!-- Tabs -->
      <div
        class="flex space-x-1 bg-[#cbd5e0] dark:bg-dark-secondary-900 p-1 rounded-t-lg"
      >
        <button
          :class="tab === 'group' ? activeTabClass : inactiveTabClass"
          class="flex-1 py-2 rounded-md text-sm font-medium"
          @click="tab = 'group'"
        >
          Group
        </button>
        <button
          :class="tab === 'personal' ? activeTabClass : inactiveTabClass"
          class="flex-1 py-2 rounded-md text-sm font-medium"
          @click="tab = 'personal'"
        >
          Personal
        </button>
      </div>

      <!-- Chats -->
      <div class="overflow-y-scroll flex-1 px-4 py-2 space-y-4">
        <div
          v-for="(chat, index) in filteredChats"
          :key="index"
          class="flex items-start gap-3"
        >
          <div
            class="w-8 h-8 rounded-full flex-shrink-0"
            :style="{ backgroundColor: getColor(chat.name) }"
          ></div>
          <div
            class="bg-[#dacdaf] dark:bg-gray-800 text-black dark:text-white p-3 rounded-xl max-w-md w-full"
          >
            <div class="flex justify-between items-center mb-1">
              <span class="text-sm font-semibold">{{ chat.name }}</span>
              <span
                class="text-xs text-dark-secondary-500 dark:text-gray-400"
                >{{ chat.time }}</span
              >
            </div>
            <div class="text-sm text-dark-secondary-500 dark:text-white">
              {{ chat.message }}
            </div>
            <!-- <div v-if="chat.attachment" class="mt-2">
            <a
              :href="chat.attachment.url"
              target="_blank"
              class="text-blue-400 underline"
              >{{ chat.attachment.name }}</a
            >
          </div> -->
          </div>
        </div>
      </div>
    </div>

    <!-- Input -->
    <div
      class="px-2 pb-2 pt-4 border-t border-gray-700 xl:w-fit w-full"
    >
      <form
        @submit.prevent="sendMessage"
        class="flex gap-2 bg-dark-secondary-500 px-3 py-2 rounded-full items-center w-full"
      >
        <label class="cursor-pointer text-white">
          <svg
            width="30"
            height="30"
            viewBox="0 0 30 30"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M14.9629 15V19.375C14.9629 21.7875 16.9254 23.75 19.3379 23.75C21.7504 23.75 23.7129 21.7875 23.7129 19.375V12.5C23.7129 7.6625 19.8004 3.75 14.9629 3.75C10.1254 3.75 6.21289 7.6625 6.21289 12.5V20C6.21289 24.1375 9.57539 27.5 13.7129 27.5"
              stroke="#8D8F98"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          <input type="file" @change="handleFile" class="hidden" />
        </label>
        <input
          v-model="newMessage"
          type="text"
          placeholder="Type a message..."
          class="w-full px-2 py-1 bg-transparent text-white outline-none"
        />
        <button class="text-white">
          <svg
            width="40"
            height="40"
            viewBox="0 0 50 50"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <foreignObject x="-7" y="-7" width="64" height="64"
              ><div
                xmlns="http://www.w3.org/1999/xhtml"
                style="
                  backdrop-filter: blur(3.5px);
                  clip-path: url(#bgblur_0_49_1073_clip_path);
                  height: 100%;
                  width: 100%;
                "
              ></div
            ></foreignObject>
            <rect
              data-figma-bg-blur-radius="7"
              width="50"
              height="50"
              rx="25"
              fill="#1A71FF"
            />
            <path
              d="M19.2496 17.9L29.8621 14.3625C34.6246 12.775 37.2121 15.375 35.6371 20.1375L32.0996 30.75C29.7246 37.8875 25.8246 37.8875 23.4496 30.75L22.3996 27.6L19.2496 26.55C12.1121 24.175 12.1121 20.2875 19.2496 17.9Z"
              stroke="white"
              stroke-width="2.2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            <path
              d="M22.6377 27.0625L27.1127 22.575"
              stroke="white"
              stroke-width="2.2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            <defs>
              <clipPath
                id="bgblur_0_49_1073_clip_path"
                transform="translate(7 7)"
              >
                <rect width="50" height="50" rx="25" />
              </clipPath>
            </defs>
          </svg>
        </button>
      </form>
      <div
        v-if="attachedFile"
        class="text-xs text-gray-300 truncate max-w-xs mt-1 ml-3"
      >
        Selected: {{ attachedFile.name }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import type {
  AttendeeJoinResponse,
  MeetingResponse,
  UserPayload,
} from "@/kernel";

defineProps<{
  meetingdetails: MeetingResponse | undefined;
  attendeedetails: AttendeeJoinResponse | undefined;
  userdetails: UserPayload | null;
}>();
const tab = ref<"group" | "personal">("group");
const newMessage = ref("");
const attachedFile = ref<File | null>(null);

const chats = ref([
  {
    name: "Cassie Jung",
    message: "Hey everyone!",
    time: "10:00 AM",
    type: "group",
  },
  {
    name: "Alice Wong",
    message: "Let's get started.",
    time: "10:02 AM",
    type: "group",
  },
  {
    name: "Theresa Webb",
    message: "Sure!",
    time: "10:03 AM",
    type: "personal",
  },
]);

const activeTabClass =
  "bg-[#fff4d4] dark:bg-gray-700 text-black dark:text-white";
const inactiveTabClass =
  "bg-[#dacdaf] dark:bg-gray-900 text-black dark:text-gray-400";

const filteredChats = computed(() =>
  chats.value.filter((chat) => chat.type === tab.value)
);

function sendMessage() {
  if (!newMessage.value.trim() && !attachedFile.value) return;
  const message = {
    name: "You",
    message: newMessage.value,
    time: new Date().toLocaleTimeString([], {
      hour: "2-digit",
      minute: "2-digit",
    }),
    type: tab.value,
    attachment: attachedFile.value
      ? {
          name: attachedFile.value.name,
          url: URL.createObjectURL(attachedFile.value),
        }
      : null,
  };
  chats.value.push(message);
  newMessage.value = "";
  attachedFile.value = null;
}

function handleFile(event: Event) {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    attachedFile.value = target.files[0];
  }
}

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
</script>
