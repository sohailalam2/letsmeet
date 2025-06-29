import {
  createMeeting,
  getAllMeetings,
  getMeetingById,
  joinMeeting,
  leaveMeeting,
  endMeeting,
  getSummary,
  getRecording,
  removeAttendee,
  getJoinInfoByMeetingId,
  getAllAttendeesByMeetingId,
} from "./MeetingApi";

import type {
  MeetingResponse,
  MeetingResponseList,
  AttendeeJoinResponse,
  GeneratedMeetingInfoDTO,
  MeetingVideoInfoDTO,
  AttendeesInfoResponse,
} from "./types";

export const MeetingService = {
  getAll(): Promise<MeetingResponseList> {
    return getAllMeetings();
  },

  getById(id: string): Promise<MeetingResponse> {
    return getMeetingById(id);
  },

  getJoinInfoByMeetingId(id: string): Promise<AttendeeJoinResponse> {
    return getJoinInfoByMeetingId(id);
  },

  getAllAttendeesByMeetingId(id: string): Promise<AttendeesInfoResponse> {
    return getAllAttendeesByMeetingId(id);
  },

  create(externalMeetingId: string): Promise<MeetingResponse> {
    return createMeeting(externalMeetingId);
  },

  join(id: string): Promise<AttendeeJoinResponse> {
    return joinMeeting(id);
  },

  leave(id: string): Promise<void> {
    return leaveMeeting(id);
  },

  end(id: string): Promise<void> {
    return endMeeting(id);
  },
  getSummary(id: string): Promise<GeneratedMeetingInfoDTO> {
    return getSummary(id);
  },

  getRecording(id: string): Promise<MeetingVideoInfoDTO> {
    return getRecording(id);
  },

  removeAttendee(meetingId: string, attendeeId: string): Promise<void> {
    return removeAttendee(meetingId, attendeeId);
  },
};
