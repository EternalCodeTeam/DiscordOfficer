package com.eternalcode.discordapp.feature.automessages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import net.dzikoysk.cdn.entity.Contextual;
import net.dzikoysk.cdn.entity.Description;

@Contextual
public class AutoMessagesConfig {

    @Description("# How often all auto messages should be sent (e.g. PT1H for 1 hour)")
    public Duration interval = Duration.ofHours(1);

    @Description("# How many recent messages to check for duplicates (prevents spam)")
    public int duplicateCheckCount = 10;

    @Description({
        "# List of automatic messages",
        "# Each entry can have multiple message variants - bot will pick randomly"
    })
    public List<AutoMessagesEntry> entries = new ArrayList<>(List.of(
        new AutoMessagesEntry(
            1025826334435455047L,
            List.of(
                "🔥 Make your messages in Minecraft a MASTERPIECE - try the new notification generator now! 👉 https://www.eternalcode.pl/notification-generator",
                "✨ Create stunning Minecraft notifications with our generator! Check it out: https://www.eternalcode.pl/notification-generator"
            )
        )
    ));

    @Contextual
    public static class AutoMessagesEntry {
        @Description("# Discord channel ID where messages will be sent")
        public long channelId;

        @Description({
            "# List of message variants - bot will randomly choose one",
            "# Having multiple variants makes the bot feel more natural"
        })
        public List<String> messages;

        public AutoMessagesEntry() {
            // Default constructor for CDN
        }

        public AutoMessagesEntry(long channelId, List<String> messages) {
            this.channelId = channelId;
            this.messages = new ArrayList<>(messages);
        }

        // Backwards compatibility - single message
        public AutoMessagesEntry(long channelId, String message) {
            this(channelId, List.of(message));
        }
    }
}

