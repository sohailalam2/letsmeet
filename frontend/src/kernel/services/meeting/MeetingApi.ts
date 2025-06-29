import { LETSMEET_API_BASE_URL } from "@/kernel";
import type {
  MeetingResponse,
  AttendeeJoinResponse,
  MeetingResponseList,
  GeneratedMeetingInfoDTO,
  MeetingVideoInfoDTO,
  AttendeesInfoResponse,
} from "./types";

const BASE_URL = LETSMEET_API_BASE_URL;

const handleFetch = async <T>(
  input: RequestInfo,
  options?: RequestInit
): Promise<T> => {
  const res = await fetch(input, options);

  if (!res.ok) {
    throw new Error(`API error: ${res.status}`);
  }

  const text = await res.text();

  if (!text) {
  console.log('response was not there');

  }

  try {
    return JSON.parse(text) as T;
  } catch (err) {
    console.error("Failed to parse JSON:", err, "Raw response:", text);
    throw new Error("Invalid JSON in response");
  }
};


export const getAllMeetings = (): Promise<MeetingResponseList> => {
  const token = localStorage.getItem("access_token");
  return handleFetch(`${BASE_URL}/meetings`, {
    method: "GET",
    headers: { Authorization: token ? `Bearer ${token}` : "" },
  });
};

export const getMeetingById = (id: string): Promise<MeetingResponse> => {
  const token = localStorage.getItem("access_token");
  return handleFetch(`${BASE_URL}/meetings/${id}`, {
    method: "GET",
    headers: { Authorization: token ? `Bearer ${token}` : "" },
  });
};

export const getJoinInfoByMeetingId = (id: string): Promise<AttendeeJoinResponse> => {
  const token = localStorage.getItem("access_token");
  return handleFetch(`${BASE_URL}/meetings/${id}/join`, {
    method: "GET",
    headers: { Authorization: token ? `Bearer ${token}` : "" },
  });
};

export const getAllAttendeesByMeetingId = (id: string): Promise<AttendeesInfoResponse> => {
  const token = localStorage.getItem("access_token");
  return handleFetch(`${BASE_URL}/meetings/${id}/attendees`, {
    method: "GET",
    headers: { Authorization: token ? `Bearer ${token}` : "" },
  });
};

export const createMeeting = (
  externalMeetingId: string
): Promise<MeetingResponse> => {
  const token = localStorage.getItem("access_token");
  return handleFetch(`${BASE_URL}/meetings/create`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: token ? `Bearer ${token}` : "",
    },
    body: JSON.stringify({ external_meeting_id: externalMeetingId }),
  });
};

export const joinMeeting = (id: string): Promise<AttendeeJoinResponse> => {
  const token = localStorage.getItem("access_token");
  return handleFetch(`${BASE_URL}/meetings/${id}/join`, {
    method: "POST",
    headers: { Authorization: token ? `Bearer ${token}` : "" },
  });
};

export const leaveMeeting = (id: string): Promise<void> => {
  const token = localStorage.getItem("access_token");
  return handleFetch(`${BASE_URL}/meetings/${id}/leave`, {
    method: "POST",
    headers: { Authorization: token ? `Bearer ${token}` : "" },
  });
};

export const endMeeting = (id: string): Promise<void> => {
  const token = localStorage.getItem("access_token");
  return handleFetch(`${BASE_URL}/meetings/${id}/end`, {
    method: "POST",
    headers: { Authorization: token ? `Bearer ${token}` : "" },
  });
};

export const getSummary = (id: string): Promise<GeneratedMeetingInfoDTO> => {
  const token = localStorage.getItem("access_token");
  return handleFetch(`${BASE_URL}//meetings/${id}/summary`, {
    method: "GET",
    headers: { Authorization: token ? `Bearer ${token}` : "" },
  });
};
export const getRecording = (id: string): Promise<MeetingVideoInfoDTO> => {
  const token = localStorage.getItem("access_token");
  return handleFetch(`${BASE_URL}/meetings/${id}/recording`, {
    method: "GET",
    headers: { Authorization: token ? `Bearer ${token}` : "" },
  });
};
export const removeAttendee = (
  meetingId: string,
  attendeeId: string
): Promise<void> => {
  const token = localStorage.getItem("access_token");
  return handleFetch(`${BASE_URL}/${meetingId}/remove/${attendeeId}`, {
    method: "DELETE",
    headers: { Authorization: token ? `Bearer ${token}` : "" },
  });
};
