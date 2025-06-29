package com.yourkoder.letsmeet.processmanager.slack;

import com.slack.api.Slack;
import com.slack.api.SlackConfig;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.Blocks;
import com.slack.api.model.block.ContextBlockElement;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.BlockCompositions;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.BlockElements;
import com.slack.api.util.http.SlackHttpClient;
import com.yourkoder.letsmeet.domain.auth.model.User;
import com.yourkoder.letsmeet.domain.meet.model.GeneratedMeetingData;
import com.yourkoder.letsmeet.domain.meet.model.Meeting;
import com.yourkoder.letsmeet.domain.meet.service.MeetingService;
import com.yourkoder.letsmeet.domain.meet.valueobject.ActionItem;
import com.yourkoder.letsmeet.processmanager.config.ApplicationConfig;
import com.yourkoder.letsmeet.processmanager.meet.valueobject.Transcript;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
@JBossLog
@RegisterForReflection
public class SlackService {

    @Inject
    MeetingService meetingService;

    private final SlackHttpClient slackHttpClient;

    private final Slack slack;

    private final String channelId;

    private final String botToken;

    {
        SlackConfig config = new SlackConfig();
        config.setHttpClientCallTimeoutMillis(15000);
        config.setHttpClientReadTimeoutMillis(15000);
        config.setHttpClientWriteTimeoutMillis(15000);
        this.slackHttpClient = SlackHttpClient.buildSlackHttpClient(config);
    }

    public SlackService(ApplicationConfig config) {
        this.slack = Slack.getInstance(slackHttpClient);
        this.botToken = config.slack().botToken();
        this.channelId = config.slack().channelId();
    }

    public void sendMeetingSummary(
            GeneratedMeetingData info,
            Meeting meeting,
            Optional<Transcript> transcription,
            Map<String, User> userMap
    ) throws Exception {
        MethodsClient methods = slack.methods(botToken);

        List<LayoutBlock> blocks = new ArrayList<>();

        // Format meeting metadata
        String externalId = meeting.getExternalMeetingId();
        String internalId = meeting.getMeetingId();

        String createdAt = formatEpoch(meeting.getCreatedAt());
        String startedAt = meeting.getStartedAt() != null ? formatEpoch(meeting.getStartedAt()) : "N/A";
        String endedAt = meeting.getEndedAt() != null ? formatEpoch(meeting.getEndedAt()) : "N/A";

        // Header: External Meeting ID (highlighted)
        blocks.add(Blocks.header(header -> header.text(PlainTextObject.builder()
                        .text("📡 *Meeting: %s*".formatted(externalId))
                .build())));

        // Meeting Metadata Section
        blocks.add(Blocks.section(section -> section.fields(List.of(
                BlockCompositions.markdownText("*📅 Date:* " + createdAt)
        ))));

        // Move metadata to a context block (grey and small by default)
        List<ContextBlockElement> meetingMetadataElements = new ArrayList<>(
                List.of(
                        BlockCompositions.markdownText("🆔 `" + internalId + "`"),
                        BlockCompositions.markdownText("▶️ Started: " + startedAt),
                        BlockCompositions.markdownText("⏹️ Ended: " + endedAt)
                )
        );
        if (meeting.getEndedAt() != null && meeting.getStartedAt() != null) {
            String meetingDuration = getMeetingDuration(
                    meeting.getEndedAt().getEpochSecond() - meeting.getStartedAt().getEpochSecond());
            meetingMetadataElements.add(BlockCompositions.markdownText("⏱️ Duration: " + meetingDuration));
        }
        blocks.add(Blocks.context(ctx -> ctx.elements(meetingMetadataElements)));

        // Optional Transcription
        transcription.ifPresent(text -> {
            blocks.add(Blocks.divider());
            blocks.add(Blocks.section(section -> section.text(
                    BlockCompositions.markdownText("*🗣️ Transcription:* \n" + truncate(text.getValue(), 3000))
            )));
        });

        // Summary
        if (info.getMeetingSummary() != null) {
            blocks.add(Blocks.divider());
            blocks.add(Blocks.header(header -> header.text(PlainTextObject.builder()
                            .text("📝 Meeting Summary")
                    .build())));

            blocks.add(Blocks.section(section -> section.text(
                    BlockCompositions.markdownText("*Summary:* " + info.getMeetingSummary().getSummary())
            )));

            List<String> notes = info.getMeetingSummary().getMeetingNotes();
            if (notes != null && !notes.isEmpty()) {
                StringBuilder notesText = new StringBuilder();
                for (String note : notes) {
                    notesText.append("• ").append(note).append("\n");
                }
                blocks.add(Blocks.section(section -> section.text(
                        BlockCompositions.markdownText("*Notes:*\n" + notesText)
                )));
            }
        }
        // Action Items
        if (info.getActionItems() != null && !info.getActionItems().isEmpty()) {
            blocks.add(Blocks.divider());
            blocks.add(Blocks.header(header -> header.text(PlainTextObject.builder()
                    .text("Action Items").build())));

            for (ActionItem item : info.getActionItems()) {
                String assigneeId = item.getAssignee();
                String email = getUserEmail(userMap, assigneeId);
                String username = getUsername(userMap, assigneeId);
                String profilePic = getUserProfilePic(userMap, assigneeId);

                StringBuilder itemText = new StringBuilder("*Task:* " + item.getTask() + "\n");
                itemText.append("*Assignee:* ").append(email != null
                        ? email : username != null
                        ? username : assigneeId != null
                        ? assigneeId : "Unassigned").append("\n");
                itemText.append("*Due Date:* ")
                        .append(item.getDueDate() != null ? item.getDueDate() : "Not set")
                        .append("\n");

                if (item.getDetailedBreakdown() != null && !item.getDetailedBreakdown().isEmpty()) {
                    itemText.append("*Breakdown:*\n");
                    for (String subTask : item.getDetailedBreakdown()) {
                        itemText.append("   - ").append(subTask).append("\n");
                    }
                }

                blocks.add(Blocks.section(section -> section.text(
                        BlockCompositions.markdownText(itemText.toString())
                )));

                if (profilePic != null) {
                    blocks.add(Blocks.context(context -> context.elements(List.of(
                            BlockElements.image(img -> img.imageUrl(profilePic).altText("assignee pic")),
                            BlockCompositions.markdownText("_Assigned to:_ %s".formatted(
                                    username != null ? username : email
                            ))
                    ))));
                }
            }
        }

        ChatPostMessageRequest message = ChatPostMessageRequest.builder()
                .channel(channelId)
                .blocks(blocks)
                .text("Meeting Summary for " + externalId)
                .build();

        ChatPostMessageResponse response = methods.chatPostMessage(message);
        if (!response.isOk()) {
            throw new RuntimeException("Slack API error: " + response.getError());
        }

    }

    private String getUserEmail(Map<String, User> userMap, String userId) {
        if (userId == null || !userMap.containsKey(userId)) {
            return userId;
        }
        User user = userMap.get(userId);
        return user.getEmail();
    }

    private String getUsername(Map<String, User> userMap, String userId) {
        if (userId == null || !userMap.containsKey(userId)) {
            return userId;
        }
        User user = userMap.get(userId);
        return user.getName();
    }

    private String getUserProfilePic(Map<String, User> userMap, String userId) {
        if (userId == null || !userMap.containsKey(userId)) {
            return null;
        }
        User user = userMap.get(userId);
        return user.getPicture(); // Assumes this is a valid public URL or null
    }

    private String formatEpoch(Instant instant) {
        if (instant == null) {
            return "N/A";
        }
        return instant.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String truncate(String text, int limit) {
        return text.length() <= limit ? text : text.substring(0, limit) + "…";
    }

    private String getMeetingDuration(long durationInSeconds) {
        long totalMinutes = durationInSeconds / 60;
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }

}
