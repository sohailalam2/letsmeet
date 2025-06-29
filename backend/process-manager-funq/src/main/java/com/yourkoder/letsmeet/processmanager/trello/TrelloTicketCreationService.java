package com.yourkoder.letsmeet.processmanager.trello;

import com.yourkoder.letsmeet.domain.auth.model.User;
import com.yourkoder.letsmeet.domain.auth.repository.UserRepository;
import com.yourkoder.letsmeet.domain.auth.service.exception.AuthorizationException;
import com.yourkoder.letsmeet.domain.meet.model.Meeting;
import com.yourkoder.letsmeet.domain.meet.valueobject.ActionItem;
import com.yourkoder.letsmeet.processmanager.config.ApplicationConfig;
import com.yourkoder.letsmeet.processmanager.util.keymanagement.ParameterStoreService;
import com.yourkoder.letsmeet.processmanager.util.keymanagement.exception.ParameterNotFoundException;
import com.yourkoder.letsmeet.util.http.client.TrelloClient;
import com.yourkoder.letsmeet.util.http.client.dto.CardDto;
import com.yourkoder.letsmeet.util.http.client.dto.TrelloListDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
@JBossLog
public class TrelloTicketCreationService {

    @Inject
    @RestClient
    TrelloClient trello;

    @Inject
    UserRepository userRepo;

    @Inject
    ParameterStoreService parameterStoreService;

    @Inject
    ApplicationConfig config;

    public CardDto createTicket(
            User user,
            ActionItem actionItem,
            Meeting meeting
    ) throws AuthorizationException,
            ParameterNotFoundException {
        if (user == null) {
            user = new User();
        }

        String listId = user.getTrelloListID();
        if (user.getTrelloTokenPath() != null) {
            String token = parameterStoreService.getKey(user.getTrelloTokenPath());
            return createCard(actionItem, meeting, user, config.trello().apiKey(), token, listId);
        } else {
            String boardId = config.trello().defaultBoardId();
            List<TrelloListDTO> lists = trello.getLists(boardId,
                    config.trello().apiKey(), config.trello().defaultToken());
            listId = getListId(boardId, user, lists);
            return createCard(actionItem, meeting, user,
                    config.trello().apiKey(), config.trello().defaultToken(), listId);
        }
    }

    private String getListId(String boardId, User user, List<TrelloListDTO> lists) {
        if (user.getName() != null) {
            for (TrelloListDTO list : lists) {
                if (list.getName().equals(user.getName())) {
                    return  list.getId();
                }
            }
            TrelloListDTO list = trello.createList(user.getName(), boardId,
                    config.trello().apiKey(), config.trello().defaultToken());
            return list.getId();
        }
        return config.trello().defaultListId();
    }

    private @NotNull CardDto createCard(
            ActionItem actionItem,
            Meeting meeting,
            User user,
            String apiKey,
            String token,
            String listId
    ) {
        String title = actionItem.getTask();
        List<String> items = actionItem.getDetailedBreakdown();
        StringBuilder desc = new StringBuilder();
        if (items != null && !items.isEmpty()) {
            desc.append("\n\n");  // blank line before bullet list
            for (String item : items) {
                desc.append("- ").append(item).append("\n");
            }
        }
        CardDto card;
        Optional<String> dueIso = parseDueDate(actionItem.getDueDate());
        if (dueIso.isPresent()) {
            card = trello.createCard(apiKey, token, listId, title, desc.toString(), dueIso.get());
        } else {
            card = trello.createCard(apiKey, token, listId, title, desc.toString());
        }
        LOG.infof("Created Trello card %s in list %s", card.getId(), card.getListId());
        String commentText = getCommentText(meeting, user);
        try {
            Map<String, Object> stringObjectMap = trello.addComment(card.getId(), apiKey, token, commentText);
            LOG.infof("Added comment [%s] to card %s", stringObjectMap, card.getId());
        } catch (Exception e) {
            LOG.error("Failed to add comment to task card on Trello.", e);
        }
        return card;
    }

    private Optional<String> parseDueDate(String dueStr) {
        try {
            if (dueStr == null) {
                return Optional.empty();
            }
            LocalDate localDate = LocalDate.parse(dueStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            LocalDateTime localDateTime = localDate.atTime(23, 59);
            ZonedDateTime zdt = localDateTime.atZone(ZoneOffset.UTC);
            return Optional.of(zdt.format(DateTimeFormatter.ISO_INSTANT));
        } catch (DateTimeParseException e) {
            LOG.error("Failed to parse meeting action item due date: [%s].".formatted(dueStr), e);
            return Optional.empty();
        }
    }

    private static @NotNull String getCommentText(Meeting meeting, User user) {
        final String commentUserText;
        if (user.getEmail() != null || user.getName() != null) {
            commentUserText = "This task has been assigned to %s.".formatted(
                    user.getName() != null ? user.getName() : user.getEmail());
        } else {
            commentUserText = "";
        }
        return """
                This ticket is created for the meeting: %s.
                meeting ID: %s
                %s""".formatted(meeting.getExternalMeetingId(), meeting.getMeetingId(), commentUserText);
    }
}
