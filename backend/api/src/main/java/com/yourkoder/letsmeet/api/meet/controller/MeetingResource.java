package com.yourkoder.letsmeet.api.meet.controller;

import com.yourkoder.letsmeet.api.meet.dto.request.MeetingRequestDTO;
import com.yourkoder.letsmeet.api.meet.dto.response.AttendeeJoinResponseDTO;
import com.yourkoder.letsmeet.api.meet.dto.response.GeneratedMeetingInfoDTO;
import com.yourkoder.letsmeet.api.meet.dto.response.MeetingAttendeeListResponseDTO;
import com.yourkoder.letsmeet.api.meet.dto.response.MeetingResponseDTO;
import com.yourkoder.letsmeet.api.meet.dto.response.MeetingResponseListDTO;
import com.yourkoder.letsmeet.api.meet.dto.response.MeetingVideoInfoDTO;
import com.yourkoder.letsmeet.domain.auth.service.exception.AuthorizationException;
import com.yourkoder.letsmeet.domain.meet.service.MeetingAssetsService;
import com.yourkoder.letsmeet.domain.meet.service.MeetingService;
import com.yourkoder.letsmeet.domain.meet.service.exception.FailedToEndMeetingException;
import com.yourkoder.letsmeet.domain.meet.service.exception.MeetingAttendeeNotFoundException;
import com.yourkoder.letsmeet.domain.meet.service.exception.MeetingNotFoundException;
import com.yourkoder.letsmeet.domain.meet.service.exception.MeetingResourceNotFoundException;
import com.yourkoder.letsmeet.domain.meet.valueobject.AttendeeJoinInfo;
import com.yourkoder.letsmeet.domain.meet.valueobject.GeneratedMeetingInfo;
import com.yourkoder.letsmeet.domain.meet.valueobject.MeetingInfo;
import com.yourkoder.letsmeet.domain.meet.valueobject.UserAttendeeInfo;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.jbosslog.JBossLog;

import java.util.List;

@Path("/api/v1/meetings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@JBossLog
public class MeetingResource {
    @Inject
    MeetingService meetingService;

    @Inject
    MeetingAssetsService meetingAssetsService;

    @Context
    SecurityContext securityContext;

    @POST
    @Path("/create")
    public MeetingResponseDTO createMeeting(MeetingRequestDTO requestDTO) {
        String userID = getUserId();
        MeetingInfo meeting = meetingService.createMeeting(userID, requestDTO.getExternalMeetingID());
        return MeetingResponseDTO.from(meeting);
    }

    @GET
    @Path("/{meeting_id}")
    public MeetingResponseDTO getMeeting(@PathParam("meeting_id") String meetingID) {
        String userID = getUserId();
        try {
            MeetingInfo meeting = meetingService.getMeetingInfo(meetingID, userID);
            return MeetingResponseDTO.from(meeting);
        } catch (MeetingNotFoundException e) {
            LOG.error(e.getMessage(), e);
            throw new BadRequestException(e.getErrorCodedMessage());
        }
    }
 
    @GET
    @Path("/{meeting_id}/summary")
    public GeneratedMeetingInfoDTO getMeetingSummary(@PathParam("meeting_id") String meetingID) {
        String userID = getUserId();
        try {
            GeneratedMeetingInfo generatedMeetingInfo = meetingService.getGeneratedMeetingInfo(meetingID, userID);
            meetingService.getUsersByIds(meetingID);
            return GeneratedMeetingInfoDTO.from(
                    generatedMeetingInfo.getGeneratedMeetingData(), generatedMeetingInfo.getUsers());
        } catch (MeetingNotFoundException | AuthorizationException | MeetingResourceNotFoundException e) {
            LOG.error(e.getMessage(), e);
            throw new BadRequestException(e.getErrorCodedMessage());
        }
    }

    @GET
    @Path("/{meeting_id}/recording")
    public MeetingVideoInfoDTO getMeetingRecording(@PathParam("meeting_id") String meetingID) {
        String userID = getUserId();
        try {
            String signedURL = meetingAssetsService.getMeetingRecording(meetingID, userID);
            return MeetingVideoInfoDTO.builder()
                    .meetingID(meetingID).signedURL(signedURL)
                    .build();
        } catch (MeetingNotFoundException | AuthorizationException | MeetingResourceNotFoundException e) {
            LOG.error(e.getMessage(), e);
            throw new BadRequestException(e.getErrorCodedMessage());
        }
    }

    @GET
    public MeetingResponseListDTO getAllMeetingsForUser() {
        String userID = getUserId();
        List<MeetingInfo> meetings = meetingService.getAllMeetings(userID);
        return MeetingResponseListDTO.builder()
                .meetings(meetings.stream().map(MeetingResponseDTO::from).toList())
                .build();
    }

    @GET
    @Path("/all")
    public MeetingResponseListDTO getAllMeetings() {
        List<MeetingInfo> meetings = meetingService.getAllMeetings();
        return MeetingResponseListDTO.builder()
                .meetings(meetings.stream().map(MeetingResponseDTO::from).toList())
                .build();
    }

    @POST
    @Path("/{meeting_id}/join")
    public AttendeeJoinResponseDTO joinMeeting(@PathParam("meeting_id") String meetingID) {
        String userID = getUserId();
        try {
            AttendeeJoinInfo attendeeJoinInfo = meetingService.joinMeeting(meetingID, userID);
            return AttendeeJoinResponseDTO.from(attendeeJoinInfo);
        } catch (MeetingNotFoundException | AuthorizationException e) {
            LOG.error(e.getMessage(), e);
            throw new BadRequestException(e.getErrorCodedMessage());
        }
    }

    @GET
    @Path("/{meeting_id}/join")
    public AttendeeJoinResponseDTO getJoinInfo(@PathParam("meeting_id") String meetingID) {
        String userID = getUserId();
        try {
            AttendeeJoinInfo attendeeJoinInfo = meetingService.getAttendee(meetingID, userID);
            return AttendeeJoinResponseDTO.from(attendeeJoinInfo);
        } catch (MeetingAttendeeNotFoundException | MeetingNotFoundException e) {
            LOG.error(e.getMessage(), e);
            throw new BadRequestException(e.getErrorCodedMessage());
        }
    }

    @GET
    @Path("/{meeting_id}/attendees")
    public MeetingAttendeeListResponseDTO getMeetingAttendees(@PathParam("meeting_id") String meetingID) {
        String userID = getUserId();
        try {
            List<UserAttendeeInfo> meetingAttendees = meetingService.getMeetingAttendees(meetingID, userID);

            return MeetingAttendeeListResponseDTO.builder()
                    .meetingID(meetingID)
                    .attendees(meetingAttendees)
                    .build();

        } catch (MeetingNotFoundException | AuthorizationException | MeetingResourceNotFoundException e) {
            LOG.error(e.getMessage(), e);
            throw new BadRequestException(e.getErrorCodedMessage());
        }
    }

    @POST
    @Path("/{meeting_id}/leave")
    public void leave(@PathParam("meeting_id") String meetingID) {
        String userID = getUserId();
        try {
            meetingService.leaveMeeting(meetingID, userID);
        } catch (MeetingNotFoundException e) {
            LOG.error(e.getMessage(), e);
            throw new BadRequestException(e.getErrorCodedMessage());
        }
    }

    @DELETE
    @Path("/{meeting_id}/remove/{attendee_id}")
    public void removeAttendeeFromMeeting(
            @PathParam("meeting_id") String meetingID,
            @PathParam("attendee_id") String attendeeUserID
    ) {
        String userID = getUserId();
        try {
            meetingService.removeAttendee(meetingID, userID, attendeeUserID);
        } catch (MeetingNotFoundException | AuthorizationException e) {
            LOG.error(e.getMessage(), e);
            throw new BadRequestException(e.getErrorCodedMessage());
        }
    }

    @POST
    @Path("/{meeting_id}/end")
    public void end(@PathParam("meeting_id") String meetingID) {
        String userID = getUserId();
        try {
            meetingService.endMeeting(meetingID, userID);
        } catch (FailedToEndMeetingException e) {
            LOG.error(e.getMessage(), e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        } catch (AuthorizationException | MeetingNotFoundException e) {
            LOG.error(e.getMessage(), e);
            throw new BadRequestException(e.getErrorCodedMessage());
        }
    }

    private String getUserId() {
        if (securityContext == null) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return securityContext.getUserPrincipal().getName();
    }
}
