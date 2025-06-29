package com.yourkoder.letsmeet.api.trello.controller;

import com.yourkoder.letsmeet.api.trello.dto.request.UserTrelloInfoRequestDTO;
import com.yourkoder.letsmeet.api.trello.dto.response.UserTrelloInfoResponseDTO;
import com.yourkoder.letsmeet.domain.auth.model.User;
import com.yourkoder.letsmeet.domain.auth.service.exception.AuthorizationException;
import com.yourkoder.letsmeet.domain.trello.service.UserTrelloService;
import com.yourkoder.letsmeet.domain.trello.service.exception.TrelloResourceNotFoundException;
import com.yourkoder.letsmeet.iam.sdk.util.keymanagement.ParameterNotFoundException;
import com.yourkoder.letsmeet.util.http.client.dto.TrelloBoardDTO;
import com.yourkoder.letsmeet.util.http.client.dto.TrelloListDTO;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
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

@Path("/api/v1/trello")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@JBossLog
public class TrelloResource {

    @Inject
    UserTrelloService service;

    @Context
    SecurityContext securityContext;

    @GET
    @Path("/boards")
    public List<TrelloBoardDTO> getUserBoards() {
        String userID = getUserId();
        try {
            return service.getUserTrelloBoards(userID);
        } catch (AuthorizationException e) {
            LOG.error(e.getMessage(), e);
            throw new BadRequestException(e.getErrorCodedMessage());
        } catch (ParameterNotFoundException e) {
            LOG.error(e.getMessage(), e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/boards/{board_id}/lists")
    public List<TrelloListDTO> getUserBoardLists(@PathParam("boardId") String boardId) {
        String userID = getUserId();
        try {
            return service.getUserTrelloBoardLists(userID, boardId);
        } catch (AuthorizationException e) {
            LOG.error(e.getMessage(), e);
            throw new BadRequestException(e.getErrorCodedMessage());
        } catch (ParameterNotFoundException e) {
            LOG.error(e.getMessage(), e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    public UserTrelloInfoResponseDTO saveUserTrelloInfo(UserTrelloInfoRequestDTO requestDTO) {
        String userID = getUserId();
        try {
            User user = service.saveUserTrelloInfo(userID, requestDTO.getBoardID(), requestDTO.getListID());
            return UserTrelloInfoResponseDTO.from(user);
        } catch (AuthorizationException | TrelloResourceNotFoundException e) {
            LOG.error(e.getMessage(), e);
            throw new BadRequestException(e.getErrorCodedMessage());
        } catch (ParameterNotFoundException e) {
            LOG.error(e.getMessage(), e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    public UserTrelloInfoResponseDTO getUserTrelloInfo() {
        String userID = getUserId();
        try {
            User user = service.getUserTrelloInfo(userID);
            return UserTrelloInfoResponseDTO.from(user);
        } catch (AuthorizationException e) {
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
