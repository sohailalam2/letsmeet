import { DefaultMeetingSession } from "amazon-chime-sdk-js";
import { MeetingSessionStore } from "@/kernel"; // Update with correct path
import type { VideoTileInfo } from "@/stores/types"; // Update with correct path

// Types for video tile info (if not already defined in your types file)
interface VideoTileState {
  [tileId: number]: VideoTileInfo;
}

// Main cleanup function using Pinia store
async function endMeetingAndReleaseDevices(): Promise<void> {
  const store = MeetingSessionStore();

  try {
    const audioVideo = store.audioVideo;

    if (!audioVideo) {
      console.warn("No audioVideo instance found");
      return;
    }

    // Stop video
    await audioVideo.stopVideoInput();

    // Stop audio input (microphone)
    await audioVideo.stopAudioInput();

    // Stop sharing screen if active
    if (store.screenShareEnabled) {
      await audioVideo.stopContentShare();
    }

    // Leave the meeting
    audioVideo.stop();

    // Clean up meeting session
    if (store.meetingSession?.destroy) {
      await store.meetingSession.destroy();
    }
  } catch (error) {
    console.error("Error during cleanup:", error);
    throw error;
  }
}

// Manual MediaStream cleanup
function releaseMediaStreams(): void {
  // Get all video elements and stop their streams
  const videoElements: NodeListOf<HTMLVideoElement> =
    document.querySelectorAll("video");
  videoElements.forEach((video: HTMLVideoElement) => {
    if (video.srcObject instanceof MediaStream) {
      video.srcObject.getTracks().forEach((track: MediaStreamTrack) => {
        track.stop();
      });
      video.srcObject = null;
    }
  });

  // Get all audio elements and stop their streams
  const audioElements: NodeListOf<HTMLAudioElement> =
    document.querySelectorAll("audio");
  audioElements.forEach((audio: HTMLAudioElement) => {
    if (audio.srcObject instanceof MediaStream) {
      audio.srcObject.getTracks().forEach((track: MediaStreamTrack) => {
        track.stop();
      });
      audio.srcObject = null;
    }
  });
}

// Complete Meeting Manager Class using Pinia store
class MeetingManager {
  private store: ReturnType<typeof MeetingSessionStore>;

  constructor() {
    this.store = MeetingSessionStore();
  }

  async endMeeting(): Promise<void> {
    try {
      // 1. Chime SDK cleanup
      if (this.store.audioVideo && this.store.meetingSession) {
        const audioVideo = this.store.audioVideo;

        // Stop video input
        if (this.store.videoEnabled) {
          await audioVideo.stopVideoInput();
        }

        // Stop audio input
        if (this.store.audioEnabled) {
          await audioVideo.stopAudioInput();
        }

        // Stop screen share if active
        if (this.store.screenShareEnabled) {
          await audioVideo.stopContentShare();
        }

        // Stop the meeting
        audioVideo.stop();

        // Destroy meeting session
        if (this.store.meetingSession.destroy) {
          await this.store.meetingSession.destroy();
        }
      }

      // 2. Release device permissions
      await this.releaseDevicePermissions();

      // 3. Clean up UI elements
      this.cleanupVideoElements();

      // 4. Reset store state
      this.resetMeetingState();
    } catch (error) {
      console.error("Failed to end meeting gracefully:", error);
      throw error;
    }
  }

  private async releaseDevicePermissions(): Promise<void> {
    try {
      // Release manual streams
      releaseMediaStreams();

      // Additional cleanup for any remaining active streams
      const activeStreams = await this.getActiveMediaStreams();
      activeStreams.forEach((stream: MediaStream) => {
        stream.getTracks().forEach((track: MediaStreamTrack) => track.stop());
      });
    } catch (error) {
      console.log("No active streams to release or error releasing:", error);
    }
  }

  private async getActiveMediaStreams(): Promise<MediaStream[]> {
    const streams: MediaStream[] = [];

    try {
      // Check video tiles for active streams
      Object.values(this.store.videoTiles).forEach(
        (_tileInfo: VideoTileInfo) => {
          // Add logic here based on your VideoTileInfo structure
          // This depends on how you store stream references in your tiles
        }
      );

      // Check for any other active media streams in the DOM
      const mediaElements = document.querySelectorAll(
        "video, audio"
      ) as NodeListOf<HTMLMediaElement>;
      mediaElements.forEach((element: HTMLMediaElement) => {
        if (element.srcObject instanceof MediaStream) {
          streams.push(element.srcObject);
        }
      });
    } catch (error) {
      console.error("Error getting active streams:", error);
    }

    return streams;
  }

  private cleanupVideoElements(): void {
    // Remove video elements and their streams
    const videoElements: NodeListOf<HTMLVideoElement> =
      document.querySelectorAll("video");
    videoElements.forEach((video: HTMLVideoElement) => {
      if (video.srcObject) {
        const stream = video.srcObject as MediaStream;
        stream.getTracks().forEach((track: MediaStreamTrack) => track.stop());
        video.srcObject = null;
      }
    });
  }

  public resetMeetingState(): void {
    // Reset all meeting-related state in the store
    this.store.meetingSession = null;
    this.store.audioVideo = null;
    this.store.attendeeId = null;
    this.store.currentMeeting = null;
    this.store.videoEnabled = false;
    this.store.audioEnabled = false;
    this.store.screenShareEnabled = false;
    this.store.videoTiles = {};
    this.store.audioLevel = 0;
    this.store.attendeeCount = 0;

    // Reset staging state
    this.store.stagingMeeting = null;
    this.store.stagingPreviewStarted = false;
    this.store.stagingAudioEnabled = true;
    this.store.stagingVideoEnabled = true;
    this.store.stagingDevices.audioInput = "";
    this.store.stagingDevices.audioOutput = "";
    this.store.stagingDevices.videoInput = "";
  }

  // Convenience methods to access store state
  get isInMeeting(): boolean {
    return !!this.store.currentMeeting && !!this.store.meetingSession;
  }

  get currentMeetingSession(): DefaultMeetingSession | null {
    return this.store.meetingSession as DefaultMeetingSession;
  }

  get isVideoEnabled(): boolean {
    return this.store.videoEnabled || false;
  }

  get isAudioEnabled(): boolean {
    return this.store.audioEnabled || false;
  }

  get isScreenShareEnabled(): boolean {
    return this.store.screenShareEnabled || false;
  }

  get currentVideoTiles(): VideoTileState {
    return this.store.videoTiles;
  }

  // Methods to update store state
  async startMeeting(
    meetingSession: DefaultMeetingSession,
    attendeeId: string
  ): Promise<void> {
    this.store.meetingSession = meetingSession;
    this.store.audioVideo = meetingSession.audioVideo;
    this.store.attendeeId = attendeeId;
    this.store.currentMeeting = attendeeId; // or use a proper meeting ID
  }

  updateVideoState(enabled: boolean): void {
    this.store.videoEnabled = enabled;
  }

  updateAudioState(enabled: boolean): void {
    this.store.audioEnabled = enabled;
  }

  updateScreenShareState(enabled: boolean): void {
    this.store.screenShareEnabled = enabled;
  }

  updateVideoTiles(tiles: VideoTileState): void {
    this.store.videoTiles = tiles;
  }

  updateAudioLevel(level: number): void {
    this.store.audioLevel = level;
  }

  updateAttendeeCount(count: number): void {
    this.store.attendeeCount = count;
  }
}

// Event Listeners with Pinia store integration
function setupCleanupEventListeners(): void {
  const meetingManager = new MeetingManager();

  // Handle page unload
  window.addEventListener("beforeunload", async (_event: BeforeUnloadEvent) => {
    try {
      await meetingManager.endMeeting();
    } catch (error) {
      console.error("Error during beforeunload cleanup:", error);
    }
  });

  // Handle browser tab close
  window.addEventListener("unload", async (_event: Event) => {
    try {
      await meetingManager.endMeeting();
    } catch (error) {
      console.error("Error during unload cleanup:", error);
    }
  });

  // Handle meeting end button
  const endMeetingBtn = document.getElementById(
    "endMeetingBtn"
  ) as HTMLButtonElement;
  if (endMeetingBtn) {
    endMeetingBtn.addEventListener("click", async (_event: MouseEvent) => {
      try {
        await meetingManager.endMeeting();
        console.log("Meeting ended successfully");
        // You can also emit events or navigate here
      } catch (error) {
        console.error("Error ending meeting:", error);
      }
    });
  }
}

// Vue Composition API hook for cleanup
import { onBeforeUnmount } from "vue";

function useMeetingCleanup() {
  const meetingManager = new MeetingManager();

  // Cleanup when component unmounts
  onBeforeUnmount(async () => {
    try {
      if (meetingManager.isInMeeting) {
        await meetingManager.endMeeting();
      }
    } catch (error) {
      console.error("Error during component cleanup:", error);
    }
  });

  return {
    meetingManager,
    endMeeting: () => meetingManager.endMeeting(),
    isInMeeting: meetingManager.isInMeeting,
    isVideoEnabled: meetingManager.isVideoEnabled,
    isAudioEnabled: meetingManager.isAudioEnabled,
    isScreenShareEnabled: meetingManager.isScreenShareEnabled,
  };
}

// Enhanced error handling with store state
async function safeDeviceCleanup(): Promise<void> {
  const meetingManager = new MeetingManager();

  try {
    // Primary cleanup method
    await meetingManager.endMeeting();
  } catch (error) {
    console.error("Primary cleanup failed:", error);

    // Fallback: Force stop all media tracks
    try {
      const devices: MediaDeviceInfo[] =
        await navigator.mediaDevices.enumerateDevices();
      console.log("Available devices after cleanup:", devices.length);

      // Additional fallback logic
      await forceStopAllMediaTracks();

      // Reset store state as last resort
      meetingManager.resetMeetingState();
    } catch (fallbackError) {
      console.error("Fallback cleanup also failed:", fallbackError);
    }
  }
}

// Force stop all media tracks (nuclear option)
async function forceStopAllMediaTracks(): Promise<void> {
  try {
    const videoElements: NodeListOf<HTMLVideoElement> =
      document.querySelectorAll("video");
    const audioElements: NodeListOf<HTMLAudioElement> =
      document.querySelectorAll("audio");

    [...videoElements, ...audioElements].forEach(
      (element: HTMLMediaElement) => {
        if (element.srcObject instanceof MediaStream) {
          element.srcObject.getTracks().forEach((track: MediaStreamTrack) => {
            track.stop();
          });
          element.srcObject = null;
        }
      }
    );
  } catch (error) {
    console.error("Force cleanup failed:", error);
  }
}

// Usage Example with Pinia store
async function initializeMeetingWithStore(
  meetingSession: DefaultMeetingSession,
  attendeeId: string
): Promise<MeetingManager> {
  try {
    const meetingManager = new MeetingManager();

    // Initialize meeting in store
    await meetingManager.startMeeting(meetingSession, attendeeId);

    // Setup cleanup event listeners
    setupCleanupEventListeners();

    return meetingManager;
  } catch (error) {
    console.error("Failed to initialize meeting:", error);
    throw error;
  }
}

export {
  MeetingManager,
  endMeetingAndReleaseDevices,
  releaseMediaStreams,
  setupCleanupEventListeners,
  useMeetingCleanup,
  safeDeviceCleanup,
  forceStopAllMediaTracks,
  initializeMeetingWithStore,
};

