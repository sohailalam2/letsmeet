import type {
  AudioVideoFacade,
  DefaultDeviceController,
  DefaultMeetingSession,
} from "amazon-chime-sdk-js";
import { defineStore } from "pinia";
import { reactive, ref } from "vue";
import type { SpotLightTileInfo, StagingDevices, VideoTileInfo } from "./types";
import type { AttendeesInfo } from "@/kernel";

export const MeetingSessionStore = defineStore("meetingsession", () => {
  const user = ref<string | null>(null);
  const meetings = ref<[] | null>([]);
  const currentMeeting = ref<string | null>(null);
  const meetingSession = ref<DefaultMeetingSession | null>(null);
  const deviceController = ref<DefaultDeviceController | null>(null);
  const audioVideo = ref<AudioVideoFacade | null>(null);
  const attendeeId = ref<string | null>(null);
  const videoEnabled = ref<boolean>(false);
  const audioEnabled = ref<boolean>(false);
  const screenShareEnabled = ref<boolean | null>(false);
  let videoTiles = ref<{ [tileId: number]: VideoTileInfo }>({});
  const stagingMeeting = ref<string | null>(null);
  const stagingDevices = reactive<StagingDevices>({
    audioInput: "",
    audioOutput: "",
    videoInput: "",
  });
  const stagingPreviewStarted = ref<boolean | null>(false);
  const stagingAudioEnabled = ref<boolean | null>(true);
  const stagingVideoEnabled = ref<boolean | null>(true);
  const audioLevel = ref<number | null>(0);
  const attendeeCount = ref<number>(0);
  const allParticipants = ref<AttendeesInfo[]>([]);
  const spotLightVideoTile = ref<number | null>(null);
  const spotLightVideoTileInfo = ref<SpotLightTileInfo | null>(null);

  return {
    user,
    meetings,
    currentMeeting,
    meetingSession,
    stagingDevices,
    audioVideo,
    allParticipants,
    attendeeId,
    videoEnabled,
    audioEnabled,
    deviceController,
    screenShareEnabled,
    videoTiles,
    stagingMeeting,
    stagingPreviewStarted,
    stagingAudioEnabled,
    stagingVideoEnabled,
    audioLevel,
    spotLightVideoTile,
    spotLightVideoTileInfo,
    attendeeCount,
  };
});
