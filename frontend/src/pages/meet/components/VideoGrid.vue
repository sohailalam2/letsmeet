<template>
  <div class="flex justify-center px-4 gap-4 overflow-x-auto h-full mb-4">
    <div
      id="video-grid"
      class="flex items-center justify-center px-4 py-2 gap-2 w-full h-full overflow-y-hidden overflow-x-scroll rounded-xl bg-[#dacdaf]/50 dark:bg-dark-secondary-500/50"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { watch, onBeforeUnmount, ref, nextTick } from "vue";
import { useRouter } from "vue-router";
import {
  ConsoleLogger,
  DefaultDeviceController,
  LogLevel,
  MeetingSessionConfiguration,
  DefaultMeetingSession,
  MeetingSessionStatusCode,
  VideoTileState,
} from "amazon-chime-sdk-js";

import {
  MeetingService,
  MeetingSessionStore,
  MeetingManager, setupCleanupEventListeners,
  VideoTileInfo,
  type AttendeeJoinResponse,
  type MeetingResponse,
  type UserPayload, useToast,
} from "@/kernel";

import type { Meeting, JoinInfo } from "../types";

const props = defineProps<{
  meetingdetails: MeetingResponse | undefined;
  attendeedetails: AttendeeJoinResponse | undefined;
  userdetails: UserPayload | null;
}>();
const router = useRouter();
const { showErrorToast, showWarningToast } = useToast();

const updatingAttendees = new Set<string>();
// let videoTiles = ref<{ [tileId: number]: VideoTileInfo }>({});
// Initialize meeting manager directly
const meetingManager = new MeetingManager();
const isInitialized = ref(false);
const isCleaningUp = ref(false);
// Track all active streams for cleanup
const activeStreams = new Set<MediaStream>();
const activeTracks = new Set<MediaStreamTrack>();

// Setup cleanup event listeners for browser events
setupCleanupEventListeners();
// Enhanced cleanup on component unmount
onBeforeUnmount(async () => {
  if (isCleaningUp.value) return;

  try {
    console.log("Component unmounting, starting comprehensive cleanup...");
    await performComprehensiveCleanup();
  } catch (error) {
    console.error("Error during component cleanup:", error);
  }
});

watch(
  () => props.attendeedetails,
  (newVal, oldVal) => {
    if (newVal != null || oldVal != null) {
      console.log(">>>>>Props", props.meetingdetails, props.attendeedetails);

      if (
        props.meetingdetails &&
        props.attendeedetails &&
        !isInitialized.value &&
        !isCleaningUp.value &&
        newVal !== oldVal
      ) {
        setupChimeMeeting(
          {
            id: props.meetingdetails?.id,
            media_region: props.meetingdetails?.media_region as string,
            media_placement: props.meetingdetails?.media_placement,
          },
          {
            attendee_id: props.attendeedetails?.attendee_id,
            join_token: props.attendeedetails?.join_token,
          }
        );
      }
    }
  }
);

// Comprehensive cleanup function
async function performComprehensiveCleanup(): Promise<void> {
  if (isCleaningUp.value) return;
  isCleaningUp.value = true;

  try {
    console.log("Starting comprehensive cleanup...");

    // 1. Stop meeting session first
    if (meetingManager.isInMeeting && meetingManager.currentMeetingSession) {
      const audioVideo = meetingManager.currentMeetingSession.audioVideo;

      try {
        // Stop video tiles first
        audioVideo.stopLocalVideoTile();

        // Stop video input
        await audioVideo.stopVideoInput();

        // Stop audio input
        await audioVideo.stopAudioInput();

        // Stop the session
        audioVideo.stop();

        console.log("AudioVideo session stopped");
      } catch (error) {
        console.error("Error stopping audioVideo session:", error);
      }
    }

    // 2. Clean up all video elements and their streams
    await cleanupAllVideoElements();

    // 3. Clean up tracked streams and tracks
    await cleanupTrackedMedia();

    // 4. End meeting through manager
    if (meetingManager.isInMeeting) {
      await meetingManager.endMeeting();
    }

    // 5. Clear video grid
    await nextTick();
    const videoGrid = document.getElementById("video-grid");
    if (videoGrid) {
      videoGrid.innerHTML = "";
    }

    // 6. Reset states
    meetingManager.resetMeetingState();
    const store = MeetingSessionStore();
    store.videoTiles = {};

    console.log("Comprehensive cleanup completed successfully");
  } catch (error) {
    console.error("Error in comprehensive cleanup:", error);
    // Force cleanup as last resort
    await forceCleanupAllMedia();
  } finally {
    isCleaningUp.value = false;
    isInitialized.value = false;
  }
}

// Clean up all video elements and their streams
async function cleanupAllVideoElements(): Promise<void> {
  try {
    const videoElements = document.querySelectorAll("video");
    console.log(`Cleaning up ${videoElements.length} video elements`);

    for (const video of videoElements) {
      if (video.srcObject instanceof MediaStream) {
        const stream = video.srcObject;
        console.log(`Stopping stream with ${stream.getTracks().length} tracks`);

        stream.getTracks().forEach((track) => {
          console.log(`Stopping ${track.kind} track: ${track.label}`);
          track.stop();
          activeTracks.delete(track);
        });

        video.srcObject = null;
        activeStreams.delete(stream);
      }

      // Pause and reset video element
      video.pause();
      video.removeAttribute("src");
      video.load();
    }

    // Also cleanup audio elements
    const audioElements = document.querySelectorAll("audio");
    for (const audio of audioElements) {
      if (audio.srcObject instanceof MediaStream) {
        const stream = audio.srcObject;
        stream.getTracks().forEach((track) => {
          console.log(`Stopping audio track: ${track.label}`);
          track.stop();
          activeTracks.delete(track);
        });
        audio.srcObject = null;
        activeStreams.delete(stream);
      }
    }
  } catch (error) {
    console.error("Error cleaning up video elements:", error);
  }
}

// Clean up tracked media streams and tracks
async function cleanupTrackedMedia(): Promise<void> {
  try {
    console.log(`Cleaning up ${activeTracks.size} tracked tracks`);

    activeTracks.forEach((track) => {
      if (track.readyState !== "ended") {
        console.log(
          `Force stopping tracked ${track.kind} track: ${track.label}`
        );
        track.stop();
      }
    });

    activeTracks.clear();
    activeStreams.clear();
  } catch (error) {
    console.error("Error cleaning up tracked media:", error);
  }
}

// Force cleanup function for stubborn media devices
async function forceCleanupAllMedia(): Promise<void> {
  try {
    console.log("Performing force cleanup of all media...");

    // Get all active media streams from navigator
    if (
      navigator.mediaDevices &&
      (await navigator.mediaDevices.getUserMedia())
    ) {
      try {
        // This will help identify any remaining active streams
        const devices = await navigator.mediaDevices.enumerateDevices();
        console.log(`Found ${devices.length} media devices`);
      } catch (error) {
        console.warn("Could not enumerate devices:", error);
      }
    }

    // Force stop all video and audio elements
    await cleanupAllVideoElements();

    await cleanupTrackedMedia();

    // Clear the video grid completely
    const videoGrid = document.getElementById("video-grid");
    if (videoGrid) {
      videoGrid.innerHTML = "";
    }

    console.log("Force cleanup completed");
  } catch (error) {
    console.error("Force cleanup failed:", error);
  }
}

async function setupChimeMeeting(
  meeting: Meeting,
  joinInfo: JoinInfo
): Promise<void> {
  try {
    console.log(">>>>SetupChimeMeeting");

    isInitialized.value = true;

    if (!joinInfo.attendee_id || !joinInfo.join_token) {
      alert("JoinInfo Not Avilable");
    }
    const configuration = new MeetingSessionConfiguration(
      {
        MeetingId: meeting.id,
        MediaRegion: meeting.media_region,
        MediaPlacement: {
          AudioHostUrl: meeting.media_placement.audio_host_url,
          AudioFallbackUrl: meeting.media_placement.audio_fallback_url,
          SignalingUrl: meeting.media_placement.signaling_url,
          TurnControlUrl: meeting.media_placement.turn_control_url,
          ScreenDataUrl: meeting.media_placement.screen_data_url,
          ScreenViewingUrl: meeting.media_placement.screen_viewing_url,
          ScreenSharingUrl: meeting.media_placement.screen_sharing_url,
          EventIngestionUrl: meeting.media_placement.event_ingestion_url,
        },
      },
      {
        AttendeeId: joinInfo.attendee_id,
        JoinToken: joinInfo.join_token,
      }
    );

    const logger = new ConsoleLogger("ChimeMeetingLogs", LogLevel.INFO);
    const deviceController = new DefaultDeviceController(logger);

    const meetingSession: DefaultMeetingSession = new DefaultMeetingSession(
      configuration,
      logger,
      deviceController
    );

    // Initialize meeting manager with the session
    await meetingManager.startMeeting(meetingSession, joinInfo.attendee_id);

    // Set up observers using the meeting manager's session
    const audioVideo = meetingManager.currentMeetingSession?.audioVideo;
    if (!audioVideo) {
      throw new Error("Failed to get audioVideo from meeting session");
    }

    audioVideo.addObserver({
      videoTileDidUpdate: (tileState) => {
        const tid = tileState.tileId;
        const baseId = tileState.boundAttendeeId?.split("#")[0];
        if (!tid || !baseId) return;

        console.log(">>>>TileState", tid);
        console.log(">>>>TileStateAllTiles", audioVideo.getAllVideoTiles());

        const store = MeetingSessionStore();
        // Check existing tile for same attendee
        const existingEntry = Object.entries(store.videoTiles).find(
          ([, info]) => {
            const boundId = info.tileState?.boundAttendeeId;
            return boundId?.split("#")[0] === baseId;
          }
        );

        if (existingEntry) {
          const [oldIdStr, info] = existingEntry;
          const oldId = Number(oldIdStr);
          if (oldId !== tid) {
            // Rebind to new tileId, reuse element
            audioVideo.bindVideoElement(tid, info.videoElement);
            info.tileState = tileState;
            delete store.videoTiles[oldId];
            store.videoTiles[tid] = info;
            meetingManager.updateVideoTiles(store.videoTiles);
          }
        } else {
          // Fresh tile
          addVideoTile(tileState);
        }
      },
      videoTileWasRemoved: (tileId) => {
        removeVideoTile(tileId);
      },
      audioVideoDidStart: () => {
        console.log("Audio/video started");
        // Ensure audio is unmuted globally when the session starts
        if (meetingManager.isAudioEnabled) {
          audioVideo.realtimeUnmuteLocalAudio();
        }
      },
      audioVideoDidStop: (sessionStatus) => {
        const statusCode = sessionStatus.statusCode();
        console.log("Meeting stopped with status:", statusCode);

        // Handle different termination scenarios
        if (statusCode === MeetingSessionStatusCode.MeetingEnded) {
          console.log("Meeting ended normally");
          alert("The meeting has ended.");
          handleMeetingEnd();
        } else if (statusCode === MeetingSessionStatusCode.Left) {
          console.log("Left the meeting");
          handleMeetingEnd();
        } else if (
          statusCode === MeetingSessionStatusCode.AudioJoinedFromAnotherDevice
        ) {
          console.log("Audio joined from another device");
          alert("You have joined the meeting from another device.");
          handleMeetingEnd();
        } else if (
          statusCode === MeetingSessionStatusCode.ConnectionHealthReconnect
        ) {
          console.log("Connection health triggered reconnect");
          // Don't end meeting on reconnect, let it try to recover
        } else if (statusCode === MeetingSessionStatusCode.AudioDisconnected) {
          console.log("Audio disconnected");
          showWarningToast("Audio disconnected, attempting to reconnect...");
        } else {
          console.log("Meeting stopped with unexpected status:", statusCode);
          // For any other termination, clean up
          handleMeetingEnd();
        }
      },
      connectionDidBecomePoor: () => {
        showWarningToast("Your connection is poor");
      },
      connectionDidSuggestStopVideo: () => {
        console.log("Connection suggests stopping video");
        showWarningToast("Poor connection detected, consider turning off video");
      },
    });

    // Start the audio/video session
    await startAudioVideoSession();
  } catch (error: any) {
    console.error("Error setting up Chime meeting:", error);
    showErrorToast(`Something Went Wrong: ${error}`);
    isInitialized.value = false;
    throw new Error(
      "Failed to set up meeting: " + (error.message ?? String(error))
    );
  }
}

async function handleMeetingEnd(): Promise<void> {
  if (isCleaningUp.value) return;

  try {
    console.log("Ending meeting gracefully...");
    await performComprehensiveCleanup();
    console.log("Meeting ended successfully");
    await router.push("/");
  } catch (error) {
    console.error("Error ending meeting:", error);
    showErrorToast(`Error ending meeting: ${error}`);
    await router.push("/");
  }
}

async function startAudioVideoSession(): Promise<void> {
  const store = MeetingSessionStore();

  try {
    const audioVideo = meetingManager.currentMeetingSession?.audioVideo;
    const staging = store.stagingDevices;

    console.log(">>>>Stagging Devices", staging);

    if (!audioVideo) throw new Error("AudioVideo instance is not initialized.");

    // ✅ Set audio input device
    if (store.audioEnabled) {
      if (staging.audioInput) {
        await audioVideo.startAudioInput(staging.audioInput);
        if (store.audioEnabled) {
          audioVideo.realtimeUnmuteLocalAudio();
        } else {
          audioVideo.realtimeMuteLocalAudio();
        }
      } else {
        const devices = await audioVideo.listAudioInputDevices();
        console.log(">>>>>>List Of Input Devices", devices);
        if (devices.length > 0) {
          await audioVideo.startAudioInput(devices[0].deviceId);
          if (store.audioEnabled) {
            audioVideo.realtimeUnmuteLocalAudio();
          } else {
            audioVideo.realtimeMuteLocalAudio();
          }
        }
      }
    }

    // ✅ Set audio output (speaker) device
    if (staging.audioOutput) {
      try {
        const audioElements = document.querySelectorAll("audio");
        for (const audioElement of audioElements) {
          // @ts-ignore - setSinkId is not in standard DOM types yet
          if (typeof audioElement.setSinkId === "function") {
            await audioElement.setSinkId(staging.audioOutput);
          }
        }
      } catch (err) {
        console.warn("Could not set audio output device:", err);
      }
    }

    // ✅ Set video input device with error handling
    if (store.videoEnabled) {
      if (staging.videoInput) {
        try {
          await audioVideo.startVideoInput(staging.videoInput);
        } catch (error) {
          console.error("Error starting video input:", error);
          showWarningToast("Could not access camera");
        }
      } else {
        try {
          const devices = await audioVideo.listVideoInputDevices();
          if (devices.length > 0) {
            console.log(">>>>>>List Of Video Devices", devices);
            await audioVideo.startVideoInput(devices[0].deviceId);
          }
        } catch (error) {
          console.error("Error starting default video input:", error);
        }
      }
    }

    // ✅ Bind audio element
    const audioElement = document.createElement("audio");
    audioElement.autoplay = true;
    audioVideo.bindAudioElement(audioElement);

    audioVideo.start();

    // ✅ Update meeting manager states
    meetingManager.updateAudioState(store.audioEnabled || false);
    meetingManager.updateVideoState(store.videoEnabled || false);
  } catch (error: any) {
    console.error("Error starting audio/video session:", error);
    throw new Error(
      "Failed to start audio/video: " + (error.message || String(error))
    );
  }
}

async function addVideoTile(tileState: VideoTileState) {
  const tileId = tileState.tileId;
  const attendeeId = tileState.boundAttendeeId;
  const isLocalTile = tileState.localTile;
  const store = MeetingSessionStore();
  if (!tileId || !attendeeId) return;
  const attendeeBaseId = attendeeId.split("#")[0];
  const videoTileStore = store.videoTiles;

  const existingTileEntry = Object.entries(videoTileStore).find(([, info]) => {
    console.log(">>>>>existingTileEntry", info);
    const boundId = info.tileState?.boundAttendeeId;
    return boundId?.split("#")[0] === attendeeBaseId;
  });

  if (existingTileEntry) {
    console.log(">>>>>>TileStateEntryFound", tileState);
    const [existingTileId, existingTile] = existingTileEntry;

    // Only rebind the new tileId to the same video element
    const videoElement = existingTile.videoElement;
    try {
      const audioVideo = meetingManager.currentMeetingSession?.audioVideo;
      if (audioVideo) {
        audioVideo.bindVideoElement(tileId, videoElement);
        existingTile.tileState = tileState;

        // Clean up old binding
        if (tileId !== +existingTileId) {
          delete store.videoTiles[+existingTileId];
          store.videoTiles[tileId] = existingTile;
        }

        meetingManager.updateVideoTiles(store.videoTiles);
        return; // ✅ Skip creating new tile
      }
    } catch (err) {
      console.error("Rebinding video tile failed:", err);
    }
  } else {
    // 💥 Prevent re-entrancy
    if (updatingAttendees.has(attendeeId)) {
      console.log(`⚠️ Skipping duplicate update for ${attendeeId}`);
      return;
    }
    updatingAttendees.add(attendeeId);
    console.log(">>>>>>TileStateEntryNotFound", tileState);
  }

  // Bind the video element to the tile
  try {
    // Create video tile container
    const videoTileElement = document.createElement("div");
    videoTileElement.className =
      "video-tile relative bg-black rounded-xl cursor-pointer h-40";
    // videoTileElement.style.height = "100px";

    // Create video element
    const videoElement = document.createElement("video");
    videoElement.id = `video-${tileId}`;
    videoElement.className = "h-40 rounded-xl";
    // videoElement.style.width = "100%";
    videoElement.style.objectFit = "cover";
    videoElement.autoplay = true;
    videoElement.playsInline = true; // Important for mobile

    // Store reference to track for cleanup
    videoTileElement.setAttribute("data-tile-id", tileId.toString());
    videoTileElement.dataset.active = "false";
    videoTileElement.id = `videocontainer-${tileId}`;
    videoTileElement.addEventListener("click", (e: Event) => {
      const currentVideoElementContainer = e.currentTarget as HTMLElement;
      const currentVideoElementContainerId = currentVideoElementContainer.id;
      const currentTileId = Number(
        currentVideoElementContainerId.split("-")[1]
      );
      if (MeetingSessionStore().spotLightVideoTileInfo == null) {
        MeetingSessionStore().spotLightVideoTile = tileId;
        MeetingSessionStore().spotLightVideoTileInfo = {
          tileId,
          tileContainer: videoTileElement,
        };
        videoTileElement.classList.add("border-2", "border-blue-800");
        return;
      }
      const spotLightTileInfo = MeetingSessionStore().spotLightVideoTileInfo;

      if (spotLightTileInfo && spotLightTileInfo.tileId === currentTileId) {
        MeetingSessionStore().spotLightVideoTile = null;
        MeetingSessionStore().spotLightVideoTileInfo = null;
        videoTileElement.classList.remove("border-2", "border-blue-800");
      } else {
        spotLightTileInfo?.tileContainer.classList.remove(
          "border-2",
          "border-blue-800"
        );
        MeetingSessionStore().spotLightVideoTile = currentTileId;
        MeetingSessionStore().spotLightVideoTileInfo = {
          tileId: currentTileId,
          tileContainer: currentVideoElementContainer,
        };
          currentVideoElementContainer?.classList.add(
          "border-2",
          "border-blue-800"
        );
      }
    });

    if (isLocalTile) {
      videoElement.muted = true;
    } else {
      videoElement.muted = false;
      videoElement.volume = 1.0;
    }

    // Track streams for cleanup
    videoElement.addEventListener("loadeddata", () => {
      if (videoElement.srcObject instanceof MediaStream) {
        activeStreams.add(videoElement.srcObject);
        videoElement.srcObject.getTracks().forEach((track) => {
          activeTracks.add(track);
        });
      }
    });

    // Create name tag
    if (props.meetingdetails) {
      try {
        const allAttendees = await MeetingService.getAllAttendeesByMeetingId(
          props.meetingdetails?.id
        );
        MeetingSessionStore().allParticipants = allAttendees.attendees;
        const attendeeName =
          allAttendees.attendees.find(
            (val) => val.attendee_id === attendeeId.split("#")[0]
          )?.name || "Unknown";

        console.log(">>>>>>>>>>AttendeeId", attendeeId);
        console.log(">>>>>>>>>>AttendeeName", attendeeName);

        const nameTag = document.createElement("div");
        nameTag.className =
          "video-name-tag absolute bottom-2 left-2 bg-black bg-opacity-50 text-white px-2 py-1 rounded text-sm";
        nameTag.textContent = isLocalTile ? "You" : attendeeName;

        // Add elements to the DOM
        videoTileElement.appendChild(videoElement);
        videoTileElement.appendChild(nameTag);
      } catch (error) {
        showErrorToast(`Video Element got some error:${error}`);
      }
    }

    // Create a container for the video tile
    const tileContainer = document.createElement("div");
    tileContainer.className = "relative";
    tileContainer.setAttribute("data-tile-container", tileId.toString());
    tileContainer.appendChild(videoTileElement);

    document.getElementById("video-grid")?.appendChild(tileContainer);
    const audioVideo = meetingManager.currentMeetingSession?.audioVideo;
    if (audioVideo) {
      audioVideo.bindVideoElement(tileId, videoElement);
      console.log(`✅ Bound video element for tile ${tileId}`);

      if (!isLocalTile) {
        // Make sure audio is enabled for this remote participant
        audioVideo.realtimeSubscribeToVolumeIndicator(
          attendeeId,
          (attendeeId, volume, muted) => {
            console.log(
              `Remote attendee ${attendeeId}: volume ${volume}, muted: ${muted}`
            );
          }
        );
      }
    }
    // Store tile info and update meeting manager
    store.videoTiles[tileId] = {
      tileState,
      videoElement,
      tileContainer,
    } as VideoTileInfo;

    // Update meeting manager with new tiles
    store.videoTiles[tileId] = { tileState, videoElement, tileContainer };
    MeetingSessionStore().attendeeCount += 1;
    meetingManager.updateVideoTiles(store.videoTiles);
    console.log(">>>>Store", videoTileStore);
    console.log(">>>>StoreEntries", Object.entries(videoTileStore));
    console.log(">>>@Raw store:", videoTileStore);
    console.log(">>>@Keys:", Object.keys(videoTileStore));
    console.log(">>>@Entries:", Object.entries(videoTileStore));
  } catch (error) {
    showErrorToast(`Something Went Wrong: ${error}`);
    console.error(`❌ Failed to bind video element for tile ${tileId}:`, error);
  } finally {
    updatingAttendees.delete(attendeeId);
  }
}

function removeVideoTile(tileId: number): void {
  const store = MeetingSessionStore();
  const tile = store.videoTiles[tileId];

  if (!tile) return;

  // 🔇 Unbind the video element from Chime
  const audioVideo = meetingManager.currentMeetingSession?.audioVideo;
  if (audioVideo) {
    try {
      audioVideo.unbindVideoElement(tileId);
    } catch (err) {
      console.warn(
        `⚠️ Failed to unbind video element for tile ${tileId}:`,
        err
      );
    }
  }

  // 🛑 Stop any media tracks
  const stream = tile.videoElement.srcObject;
  if (stream instanceof MediaStream) {
    stream.getTracks().forEach((track) => {
      console.log(
        `🛑 Stopping track for tile ${tileId}:`,
        track.kind,
        track.label
      );
      track.stop();
      activeTracks.delete(track);
    });
    activeStreams.delete(stream);
    tile.videoElement.srcObject = null;
  }

  // 🧼 Clean video element
  tile.videoElement.pause();
  tile.videoElement.removeAttribute("src");
  tile.videoElement.load();

  // 🧹 Remove from DOM
  tile.tileContainer.remove();

  // 🧾 Update state
  MeetingSessionStore().attendeeCount -= 1;
  delete store.videoTiles[tileId];
  meetingManager.updateVideoTiles(store.videoTiles);

  console.log(`✅ Video tile ${tileId} removed and cleaned up`);
}

// Expose methods for parent components if needed
defineExpose({
  endMeeting: handleMeetingEnd,
  meetingManager,
  isInitialized,
  forceCleanup: performComprehensiveCleanup,
  cleanupAllMedia: forceCleanupAllMedia,
});
</script>
