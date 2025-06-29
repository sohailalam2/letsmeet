package com.yourkoder.letsmeet.util.http.client;

import com.yourkoder.letsmeet.util.http.client.dto.CardDto;
import com.yourkoder.letsmeet.util.http.client.dto.TrelloBoardDTO;
import com.yourkoder.letsmeet.util.http.client.dto.TrelloListDTO;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;
import java.util.Map;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(baseUri = "https://api.trello.com/1")
public interface TrelloClient {
    @GET
    @Path("/members/me/boards")
    List<TrelloBoardDTO> getBoards(
            @QueryParam("key") String key,
            @QueryParam("token") String token);

    @GET
    @Path("/boards/{boardId}/lists")
    List<TrelloListDTO> getLists(
            @PathParam("boardId") String boardId,
            @QueryParam("key") String key,
            @QueryParam("token") String token);

    @POST
    @Path("/cards")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    CardDto createCard(
            @QueryParam("key") String key,
            @QueryParam("token") String token,
            @FormParam("idList") String listId,
            @FormParam("name") String name,
            @FormParam("desc") String description);

    @POST
    @Path("/cards")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    CardDto createCard(
            @QueryParam("key") String key,
            @QueryParam("token") String token,
            @FormParam("idList") String listId,
            @FormParam("name") String name,
            @FormParam("desc") String description,
            @FormParam("due") String dueIso8601  // optional
    );

    @POST
    @Path("/lists")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    TrelloListDTO createList(
            @FormParam("name") String name,
            @FormParam("idBoard") String boardId,
            @QueryParam("key") String key,
            @QueryParam("token") String token
    );

    @POST
    @Path("/cards/{cardId}/actions/comments")
    Map<String, Object> addComment(
            @PathParam("cardId") String cardId,
            @QueryParam("key") String key,
            @QueryParam("token") String token,
            @QueryParam("text") String text
    );
}
