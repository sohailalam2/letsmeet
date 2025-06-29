import type { AudioVideoFacade, MeetingSession } from "amazon-chime-sdk-js";

export interface MediaPlacement {
  audio_host_url: string;
  audio_fallback_url: string;
  signaling_url: string;
  turn_control_url: string;
  screen_data_url: string;
  screen_viewing_url: string;
  screen_sharing_url: string;
  event_ingestion_url: string;
}

export interface Meeting {
  id: string;
  media_region: string;
  media_placement: MediaPlacement;
}

export interface JoinInfo {
  attendee_id: string;
  join_token: string;
}

export interface ChimeState {
  meetingSession: MeetingSession | null;
  audioVideo: AudioVideoFacade | null;
}