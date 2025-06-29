package com.yourkoder.letsmeet.domain.trello.service;

import com.yourkoder.letsmeet.config.ApplicationConfig;
import com.yourkoder.letsmeet.domain.auth.model.User;
import com.yourkoder.letsmeet.domain.auth.repository.UserRepository;
import com.yourkoder.letsmeet.domain.auth.service.exception.AuthorizationException;
import com.yourkoder.letsmeet.domain.trello.service.exception.TrelloResourceNotFoundException;
import com.yourkoder.letsmeet.iam.sdk.util.keymanagement.ParameterNotFoundException;
import com.yourkoder.letsmeet.iam.sdk.util.keymanagement.ParameterStoreService;
import com.yourkoder.letsmeet.util.http.client.TrelloClient;
import com.yourkoder.letsmeet.util.http.client.dto.TrelloBoardDTO;
import com.yourkoder.letsmeet.util.http.client.dto.TrelloListDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@JBossLog
public class UserTrelloService {

    @Inject
    @RestClient
    TrelloClient trello;

    @Inject
    UserRepository userRepo;

    @Inject
    ParameterStoreService parameterStoreService;

    @Inject
    ApplicationConfig config;

    public List<TrelloBoardDTO> getUserTrelloBoards(String userID) throws AuthorizationException,
            ParameterNotFoundException {
        User user = userRepo.findById(userID)
                .orElseThrow(() -> new AuthorizationException("Unauthorized"));
        String trelloTokenPath = user.getTrelloTokenPath();
        if (trelloTokenPath == null) {
            throw new AuthorizationException("User is not authorized to access trello");
        }
        String token = parameterStoreService.getKey(trelloTokenPath);

        return trello.getBoards(config.trello().apiKey(), token);
    }

    public List<TrelloListDTO> getUserTrelloBoardLists(
            String userID,
            String boardID
    ) throws AuthorizationException, ParameterNotFoundException {
        User user = userRepo.findById(userID)
                .orElseThrow(() -> new AuthorizationException("Unauthorized"));
        String trelloTokenPath = user.getTrelloTokenPath();
        if (trelloTokenPath == null) {
            throw new AuthorizationException("User is not authorized to access trello");
        }
        String token = parameterStoreService.getKey(trelloTokenPath);

        return trello.getLists(boardID, config.trello().apiKey(), token);
    }

    public User saveUserTrelloInfo(String userID, String boardID, String listID) throws AuthorizationException,
            ParameterNotFoundException, TrelloResourceNotFoundException {
        User user = userRepo.findById(userID)
                .orElseThrow(() -> new AuthorizationException("Unauthorized"));

        String trelloTokenPath = user.getTrelloTokenPath();
        if (trelloTokenPath == null) {
            throw new AuthorizationException("User is not authorized to access trello");
        }
        String token = parameterStoreService.getKey(trelloTokenPath);

        Map<String, TrelloBoardDTO> boards = trello.getBoards(config.trello().apiKey(), token)
                .stream().collect(Collectors.toMap(
                        TrelloBoardDTO::getId,
                        entry -> entry
                ));
        Map<String, TrelloListDTO> lists = trello.getLists(boardID, config.trello().apiKey(), token)
                .stream().collect(Collectors.toMap(
                        TrelloListDTO::getId,
                        entry -> entry
                ));

        if (!boards.containsKey(boardID)) {
            throw new TrelloResourceNotFoundException("Board does not exist.");
        }
        if (!lists.containsKey(listID)) {
            throw new TrelloResourceNotFoundException("Board list does not exist.");
        }

        user.setTrelloBoardID(boardID);
        user.setTrelloListID(listID);
        user.setTrelloBoardName(boards.get(boardID).getName());
        user.setTrelloListName(lists.get(listID).getName());
        userRepo.save(user);
        return user;
    }

    public User getUserTrelloInfo(String userID) throws AuthorizationException {
        User user = userRepo.findById(userID)
                .orElseThrow(() -> new AuthorizationException("Unauthorized"));

        String trelloTokenPath = user.getTrelloTokenPath();
        if (trelloTokenPath == null) {
            throw new AuthorizationException("User is not authorized to access trello");
        }
        return user;
    }
}
