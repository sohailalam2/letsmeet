export interface MeetingResponseList {
  meetings: MeetingResponse[];
}

export interface MeetingResponse {
  id: string;
  external_meeting_id: string;
  media_region: string;
  media_placement: MediaPlacement;
  host_user_id: string;
  has_host_joined: boolean;
  is_host: boolean;
  created_at: number;
  started_at: number;
  ended_at: number;
  is_ended: boolean;
  waiting_users: string[];
  participants: string[];
  join_url: string;
}

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

export interface MeetingJoinRequest {
  meeting_id: string;
  display_name: string;
}

export interface AttendeeJoinResponse {
  ready: boolean;
  user_id: string;
  attendee_id: string;
  started_at: number;
  join_token: string;
}
export interface AttendeesInfoResponse {
  id: string;
  attendees: AttendeesInfo[];
}
export interface AttendeesInfo {
  sub: string;
  name: string;
  attendee_id: string;
  picture: string;
}
export interface MeetingVideoInfoDTO {
  id: string;
  signed_url: string;
}

export interface GeneratedMeetingInfoDTO {
  id: string;
  summary: MeetingSummaryDTO;
  action_items: ActionItem[];
}

export interface MeetingSummaryDTO {
  summary: string;
  meeting_notes: string[];
}

export interface ActionItem {
  task: string;
  assignee: string;
  assignee_name: string;
  assignee_email: string;
  "detailed-breakdown": string[];
  due_date: string;
}
