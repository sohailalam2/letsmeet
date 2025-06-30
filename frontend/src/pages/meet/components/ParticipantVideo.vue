<template>
  <div
    class="relative max-w-full max-h-full bg-[#dacdaf] dark:bg-dark-secondary-500 p-4 rounded-2xl m-4"
  >
    <div
      class="absolute top-5 left-5 text-sm bg-black/50 rounded-full px-3 py-2 flex items-center gap-2.5 z-10"
    >
      <img
        src="../../../assets/icons/recording-logo.svg"
        class="w-6 h-6"
        id="show-recording-inprogress"
      />

      <p class="text-base">{{ formatTime(elapsedSeconds) }}</p>
    </div>
    <div
      class="max-w-full h-full flex justify-center rounded-xl border darkborder-dark-tertiary-400 border-dark-secondary-400"
    >
      <video
        id="active-speaker-video"
        autoplay
        muted
        class="rounded-xl w-full h-full object-fill"
      />
    </div>
    <div
      class="absolute top-5 right-5 text-white text-sm bg-black/50 rounded-full px-3 py-2 flex items-center gap-2"
    >
      <svg
        v-if="!isScreenSharing"
        class="w-5 h-5 text-white"
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 24 24"
        fill="currentColor"
      >
        <path
          d="M12 12c2.21 0 4-1.79 4-4S14.21 4 12 4 8 5.79 8 8s1.79 4 4 4z"
        />
        <path d="M12 14c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
      </svg>
      <svg
        v-else
        class="w-5 h-5 text-white"
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 24 24"
        fill="currentColor"
      >
        <path
          d="M20 18c1.1 0 1.99-.9 1.99-2L22 6c0-1.1-.9-2-2-2H4c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2H0v2h24v-2h-4zM4 6h16v10H4V6z"
        />
      </svg>
      {{ displayName }}
    </div>
    <div
      class="absolute bottom-4 left-0 right-0 px-4 w-full flex justify-center"
    >
      <VideoGrid
        class="w-full"
        :meetingdetails="props.meetingdetails"
        :attendeedetails="props.attendeedetails"
        :userdetails="props.userdetails"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onUnmounted, ref, watch, nextTick, onMounted } from "vue";
import {
  MeetingSessionStore,
  type AttendeeJoinResponse,
  type MeetingResponse,
  type UserTokenPayload,
} from "@/kernel";

import VideoGrid from "./VideoGrid.vue";

let startTimestamp = ref<number | undefined>(undefined);
const elapsedSeconds = ref(0);
let interval: number | undefined = undefined;
const activeSpeaker = ref<string | null>(null);
let activeSpeakerSwitchInProgress = false;
let activeSpeakerVideoSwitchInProgress = false;
const activeSpeakerAttendeeId = ref<string | null>(null);
const isScreenSharing = ref(false);
const speakingVolumes = new Map<string, number>();
let currentLargeScreenBoundTile: number | null = null;
let volumeTimeout: number | undefined = undefined;
const largeScreenSwitchingTimeBuffer: number = 500;

// Track all active screen shares with timestamps for prioritization
const currentScreenShareInfo = ref<{
  tileId: number;
  attendeeId: string;
  timestamp: number;
} | null>(null);

const props = defineProps<{
  meetingdetails: MeetingResponse | undefined;
  attendeedetails: AttendeeJoinResponse | undefined;
  userdetails: UserTokenPayload | null;
}>();

const activeScreenShares = ref<
  Map<
    string,
    {
      tileId: number;
      attendeeId: string;
      timestamp: number;
    }
  >
>(new Map());

const displayName = computed(() => {
  if (MeetingSessionStore().spotLightVideoTile != null) {
    const attendeeId = MeetingSessionStore()
      .audioVideo?.getVideoTile(
        MeetingSessionStore().spotLightVideoTile as number
      )
      ?.state().boundAttendeeId;
    return MeetingSessionStore().allParticipants.find(
      (val) => val.attendee_id === attendeeId
    )?.name;
  } else if (isScreenSharing.value && currentScreenShareInfo.value) {
    const participant = MeetingSessionStore().allParticipants.find(
      (val) => val.attendee_id === currentScreenShareInfo.value?.attendeeId
    );
    return `${participant?.name || "Unknown"} (Screen Share)`;
  }
  return activeSpeaker.value || "No Active Speaker";
});

watch(
  () => props.attendeedetails?.started_at,
  (newVal, oldVal) => {
    if (newVal != null || oldVal != null) {
      getTheRecordedTiming();
    }
  }
);

watch(
  () => MeetingSessionStore().audioVideo,
  () => {
    const audioVideo = MeetingSessionStore().audioVideo;
    console.log(">>>>>>>>AUDIOVIDEO", audioVideo);

    if (!audioVideo) return;

    // Subscribe to video tile updates to detect screen sharing
    audioVideo.addObserver({
      videoTileDidUpdate: (tileState) => {
        console.log("Video tile updated:", tileState);
        nextTick(() => {
          checkForScreenShares();
        });
      },
      videoTileWasRemoved: (tileId) => {
        console.log("Video tile removed:", tileId);
        // Remove from active screen shares if it was a screen share
        removeScreenShareByTileId(tileId);
        nextTick(() => {
          checkForScreenShares();
        });
      },
    });

    audioVideo.realtimeSubscribeToAttendeeIdPresence((attendeeId, present) => {
      console.log("Attendee presence:", attendeeId, present);

      if (present) {
        audioVideo.realtimeSubscribeToVolumeIndicator(
          attendeeId,
          (attendeeId, volume, muted) => {
            console.log("Volume indicator:", attendeeId, volume, muted);

            if (muted || !volume) {
              speakingVolumes.delete(attendeeId);
              return;
            }
            // // Clear existing timeout
            // if (volumeTimeout) {
            //   clearTimeout(volumeTimeout);
            // }

            if (typeof volume === "number" && volume > 0.00001) {
              speakingVolumes.set(attendeeId, volume);
              updateActiveSpeaker();
            } else {
              speakingVolumes.delete(attendeeId);
            }

            // Debounce volume changes to avoid too frequent updates
            // volumeTimeout = setTimeout(() => {}, 200);
          }
        );
      } else {
        speakingVolumes.delete(attendeeId);
        // Remove any screen shares from this attendee
        removeScreenShareByAttendeeId(attendeeId);
        updateActiveSpeaker();
      }
    });

    // Initial check for screen sharing
    nextTick(() => {
      checkForScreenShares();
    });
  }
);

watch(
  () => MeetingSessionStore().spotLightVideoTile,
  () => {
    const { spotLightVideoTile } = MeetingSessionStore();
    console.log(">>>SpotLightTile", spotLightVideoTile);
    const videoElement = document.getElementById(
      "active-speaker-video"
    ) as HTMLVideoElement;
    if (spotLightVideoTile) {
      const tileId = spotLightVideoTile;
      if (tileId && videoElement) {
        setTimeout(() => {
          activeSpeakerVideoSwitchInProgress = false;
        }, largeScreenSwitchingTimeBuffer);
        activeSpeakerVideoSwitchInProgress = true;
        if (currentLargeScreenBoundTile) {
          MeetingSessionStore().audioVideo?.unbindVideoElement(
            currentLargeScreenBoundTile
          );
          currentLargeScreenBoundTile = null;
        }
        MeetingSessionStore().audioVideo?.bindVideoElement(
          tileId,
          videoElement
        );
      }
      currentLargeScreenBoundTile = spotLightVideoTile;
    } else {
      console.log(">>>SpotLightTile Else", spotLightVideoTile);
      checkForScreenShares();
    }
  }
);

onMounted(() => {
  getTheRecordedTiming();
});

onUnmounted(() => {
  if (interval) {
    clearInterval(interval);
  }
  if (volumeTimeout) {
    clearTimeout(volumeTimeout);
  }
});

function getTheRecordedTiming() {
  startTimestamp = computed(() => props.attendeedetails?.started_at);

  if (startTimestamp.value !== undefined) {
    const startMs = startTimestamp.value * 1000;
    const nowMs = Date.now();
    elapsedSeconds.value = Math.max(Math.floor((nowMs - startMs) / 1000), 0);

    interval = setInterval(() => {
      elapsedSeconds.value++;
    }, 1000);
  }
}

function removeScreenShareByTileId(tileId: number) {
  for (const [key, shareInfo] of activeScreenShares.value.entries()) {
    if (shareInfo.tileId === tileId) {
      console.log("Removing screen share by tile ID:", tileId, key);
      activeScreenShares.value.delete(key);
      break;
    }
  }
}

function removeScreenShareByAttendeeId(attendeeId: string) {
  const keysToRemove: string[] = [];
  for (const [key, shareInfo] of activeScreenShares.value.entries()) {
    if (shareInfo.attendeeId === attendeeId) {
      keysToRemove.push(key);
    }
  }
  keysToRemove.forEach((key) => {
    console.log("Removing screen share by attendee ID:", attendeeId, key);
    activeScreenShares.value.delete(key);
  });
}

function updateActiveSpeaker() {
  // Only update active speaker if no screen sharing is active
  if (isScreenSharing.value) {
    return;
  }
  let stPointer = null;
  if (activeSpeakerSwitchInProgress || activeSpeakerVideoSwitchInProgress) {
    stPointer = setTimeout(() => {
      activeSpeakerSwitchInProgress = false;
    }, 500);
    return;
  }
  activeSpeakerSwitchInProgress = true;

  try {
    let topSpeaker: string | null = null;
    let maxVolume = 0; // Max volume for an individual

    // Find the loudest speaker
    speakingVolumes.forEach((volume, attendeeId) => {
      if (volume > maxVolume) {
        maxVolume = volume;
        topSpeaker = attendeeId;
      }
    });

    console.log(
      "Current speaking volumes:",
      Array.from(speakingVolumes.entries())
    );
    console.log("Top speaker:", topSpeaker, "with volume:", maxVolume);

    // Only update if we have a new speaker
    if (topSpeaker && topSpeaker !== activeSpeakerAttendeeId.value) {
      const topSpeakerName =
        MeetingSessionStore().allParticipants.find(
          (val) => val.attendee_id === topSpeaker
        )?.name || "Unknown";

      console.log(
        "Switching to new active speaker:",
        topSpeakerName,
        topSpeaker
      );

      activeSpeaker.value = topSpeakerName;
      activeSpeakerAttendeeId.value = topSpeaker;

      // Switch video tile
      switchActiveSpeakerVideo(topSpeaker);
    }
  } finally {
    activeSpeakerSwitchInProgress = false;
    if (stPointer) {
      clearTimeout(stPointer);
    }
  }
}

function formatTime(seconds: number): string {
  const h = Math.floor(seconds / 3600)
    .toString()
    .padStart(2, "0");
  const m = Math.floor((seconds % 3600) / 60)
    .toString()
    .padStart(2, "0");
  const s = (seconds % 60).toString().padStart(2, "0");
  return `${h}:${m}:${s}`;
}

function getTileIdForAttendee(attendeeId: string): number | null {
  const tiles = MeetingSessionStore().audioVideo?.getAllVideoTiles();
  console.log("Getting tile for attendee:", attendeeId);

  if (tiles) {
    for (const tile of tiles) {
      const state = tile.state();

      // Look for non-content tiles (regular video tiles, not screen share)
      if (state.boundAttendeeId === attendeeId && !state.isContent) {
        console.log(
          "Found matching video tile:",
          state.tileId,
          "for attendee:",
          attendeeId
        );
        return state.tileId;
      }
    }
  }

  console.log("No video tile found for attendee:", attendeeId);
  return null;
}

function getAllScreenShareTiles(): Array<{
  tileId: number;
  attendeeId: string;
}> {
  const tiles = MeetingSessionStore().audioVideo?.getAllVideoTiles();
  const screenShares: Array<{ tileId: number; attendeeId: string }> = [];

  if (tiles) {
    for (const tile of tiles) {
      const state = tile.state();

      // Check if this is a screen share tile (content share)
      if (state.isContent && state.boundAttendeeId && state.tileId) {
        screenShares.push({
          tileId: state.tileId,
          attendeeId: state.boundAttendeeId,
        });
      }
    }
  }

  console.log("Found screen shares:", screenShares);
  return screenShares;
}

function checkForScreenShares() {
  const currentScreenShares = getAllScreenShareTiles();
  const now = Date.now();

  // Update active screen shares map
  const newActiveShares = new Map<
    string,
    {
      tileId: number;
      attendeeId: string;
      timestamp: number;
    }
  >();

  // Process current screen shares
  currentScreenShares.forEach((share) => {
    const key = `${share.attendeeId}_${share.tileId}`;
    const existingShare = activeScreenShares.value.get(key);

    if (existingShare) {
      // Keep existing timestamp for ongoing shares
      newActiveShares.set(key, existingShare);
    } else {
      // New screen share detected
      console.log("New screen share detected:", share);
      newActiveShares.set(key, {
        ...share,
        timestamp: now,
      });
    }
  });

  // Update the active screen shares
  activeScreenShares.value = newActiveShares;

  console.log(
    "Active screen shares:",
    Array.from(activeScreenShares.value.entries())
  );

  if (activeScreenShares.value.size > 0) {
    // Find the most recent screen share (highest timestamp)
    interface MostRecentShare {
      tileId: number;
      attendeeId: string;
      timestamp: number;
    }

    let mostRecentShare: {
      tileId: number;
      attendeeId: string;
      timestamp: number;
    } | null = null;

    activeScreenShares.value.forEach((share) => {
      if (!mostRecentShare || share.timestamp > mostRecentShare.timestamp) {
        mostRecentShare = share as MostRecentShare;
      }
    });

    if (mostRecentShare) {
      // Check if we need to switch to a different screen share
      const needsSwitch =
        !currentScreenShareInfo.value ||
        currentScreenShareInfo.value.tileId !==
          (mostRecentShare as MostRecentShare).tileId;

      if (needsSwitch) {
        console.log("Switching to most recent screen share:", mostRecentShare);
        isScreenSharing.value = true;
        currentScreenShareInfo.value = mostRecentShare;
        switchToScreenShare((mostRecentShare as MostRecentShare).tileId);
      }
    }
  } else {
    // No screen sharing, fall back to active speaker
    if (isScreenSharing.value) {
      console.log(
        "All screen sharing stopped, switching back to active speaker"
      );
      isScreenSharing.value = false;
      currentScreenShareInfo.value = null;

      // Switch back to active speaker if available
      if (activeSpeakerAttendeeId.value) {
        switchActiveSpeakerVideo(activeSpeakerAttendeeId.value);
      }
    }
  }
}

function switchToScreenShare(tileId: number) {
  // Check for the spotLightVideoTile or Is Screen Sharing happened
  if (MeetingSessionStore().spotLightVideoTile != null) {
    return;
  }
  const videoElement = document.getElementById(
    "active-speaker-video"
  ) as HTMLVideoElement;
  if (videoElement && MeetingSessionStore().audioVideo) {
    console.log("Binding screen share tile:", tileId);
    setTimeout(() => {
      activeSpeakerVideoSwitchInProgress = false;
    }, largeScreenSwitchingTimeBuffer);
    activeSpeakerVideoSwitchInProgress = true;
    if (currentLargeScreenBoundTile) {
      MeetingSessionStore().audioVideo?.unbindVideoElement(
        currentLargeScreenBoundTile,
        true
      );
      currentLargeScreenBoundTile = null;
    }
    MeetingSessionStore().audioVideo?.bindVideoElement(tileId, videoElement);
    currentLargeScreenBoundTile = tileId;
  }
}

function switchActiveSpeakerVideo(attendeeId: string | null) {
  // Check for the spotLightVideoTile or Is Screen Sharing happened
  if (
    MeetingSessionStore().spotLightVideoTile != null ||
    MeetingSessionStore().screenShareEnabled
  ) {
    return;
  }
  if (!attendeeId) {
    console.log("No attendee ID provided for video switch");
    return;
  }
  if (activeSpeakerVideoSwitchInProgress) {
    return;
  }
  setTimeout(() => {
    console.log("Switching active speaker video to:<<<<<", attendeeId);
    activeSpeakerVideoSwitchInProgress = false;
  }, largeScreenSwitchingTimeBuffer);
  activeSpeakerVideoSwitchInProgress = true;
  try {
    const tileId = getTileIdForAttendee(attendeeId);
    console.log("Found tile ID:", tileId);
    console.log("Switching active speaker video to:", attendeeId, tileId);

    if (tileId !== null) {
      const videoElement = document.getElementById(
        "active-speaker-video"
      ) as HTMLVideoElement;
      if (videoElement && MeetingSessionStore().audioVideo) {
        console.log("Binding video element to tile:", tileId);
        if (currentLargeScreenBoundTile) {
          MeetingSessionStore().audioVideo?.unbindVideoElement(
            currentLargeScreenBoundTile,
            true
          );
          currentLargeScreenBoundTile = null;
        }
        MeetingSessionStore().audioVideo?.bindVideoElement(
          tileId,
          videoElement
        );
        currentLargeScreenBoundTile = tileId;
      } else {
        console.log("Video element or audioVideo not available");
      }
    } else {
      console.log("No video tile found for attendee:", attendeeId);
    }
  } finally {
    // activeSpeakerVideoSwitchInProgress = false;
    // if (stPointer) {
    //   clearTimeout(stPointer);
    // }
  }
}
</script>
