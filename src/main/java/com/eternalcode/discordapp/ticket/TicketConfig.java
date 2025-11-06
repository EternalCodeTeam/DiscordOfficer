package com.eternalcode.discordapp.ticket;

import com.eternalcode.discordapp.config.CdnConfig;
import java.io.File;
import java.time.Duration;
import java.util.List;
import net.dzikoysk.cdn.entity.Contextual;
import net.dzikoysk.cdn.entity.Description;
import net.dzikoysk.cdn.source.Resource;
import net.dzikoysk.cdn.source.Source;

@Contextual
public class TicketConfig implements CdnConfig {

    @Description("# ID roli staff (może zarządzać ticketami)")
    public long staffRoleId = 1144038849211793599L;

    @Description("# ID kategorii gdzie będą tworzone kanały ticketów")
    public long categoryId = 1144038885320572938L;

    @Description("# Maksymalna liczba aktywnych ticketów na użytkownika")
    public int maxTicketsPerUser = 3;

    @Description("# Po jakim czasie nieaktywne tickety mają być automatycznie zamknięte (w godzinach)")
    public Duration autoCloseAfterHours = Duration.ofDays(7);

    @Description("# ID kanału gdzie będą wysyłane transkrypty")
    public long transcriptChannelId = 1144038937996824779L;

    @Description("# Konfiguracja wiadomości")
    public MessageConfig messages = new MessageConfig();

    @Description("# Konfiguracja embedów")
    public EmbedConfig embeds = new EmbedConfig();

    @Description("# Domyślne kategorie ticketów")
    public List<TicketCategoryConfig> defaultCategories = List.of(
        new TicketCategoryConfig(
            "SUPPORT",
            "🩺",
            "Support",
            "Potrzebujesz pomocy? Otwórz ticket support!",
            true,
            1),
        new TicketCategoryConfig("BUG_REPORT", "🐛", "Bug Report", "Znalazłeś błąd? Zgłoś go tutaj!", true, 1),
        new TicketCategoryConfig(
            "FEATURE_REQUEST",
            "💡",
            "Feature Request",
            "Masz pomysł na nową funkcję?",
            true,
            1),
        new TicketCategoryConfig("GENERAL", "💬", "General", "Ogólne pytania i rozmowy", true, 1),
        new TicketCategoryConfig("COMPLAINT", "⚠️", "Complaint", "Chcesz zgłosić skargę?", true, 1)
    );

    public Duration getAutoCloseDuration() {
        return this.autoCloseAfterHours;
    }

    public List<TicketCategoryConfig> getEnabledCategories() {
        return defaultCategories.stream()
            .filter(category -> category.enabled)
            .toList();
    }

    public TicketCategoryConfig getCategoryById(String id) {
        return defaultCategories.stream()
            .filter(category -> category.id.equals(id))
            .findFirst()
            .orElse(null);
    }

    @Override
    public Resource resource(File folder) {
        return Source.of(folder, "ticket.yml");
    }

    @Contextual
    public static class MessageConfig {
        @Description("# Wiadomość wysyłana przy tworzeniu ticketu")
        public String ticketCreated = "🎫 Twój ticket został utworzony! Opisz swój problem, a nasz zespół Ci pomoże.";

        @Description("# Wiadomość wysyłana gdy użytkownik ma za dużo ticketów")
        public String tooManyTickets =
            "❌ Masz już zbyt wiele aktywnych ticketów. Zamknij jeden z istniejących przed utworzeniem nowego.";
    }

    @Contextual
    public static class EmbedConfig {
        @Description("# Kolor embedów (hex)")
        public String color = "#00ff77";

        @Description("# URL thumbnail dla embedów")
        public String thumbnail = "https://i.imgur.com/QkNxIL3.png";

        @Description("# URL footer icon")
        public String footerIcon = "";

        @Description("# Tekst footer")
        public String footerText = "DiscordOfficer Ticket System";

        @Description("# Czy pokazywać timestamp")
        public boolean showTimestamp = true;
    }

    @Contextual
    public static class TicketCategoryConfig {
        @Description("# ID kategorii (unikalny identyfikator)")
        public String id = "TICKET";

        @Description("# Emoji dla kategorii")
        public String emoji = "🎫";

        @Description("# Nazwa wyświetlana")
        public String displayName = "Ticket";

        @Description("# Opis kategorii")
        public String description = "Otwórz ticket w tej kategorii";

        @Description("# Czy kategoria jest aktywna")
        public boolean enabled = true;

        @Description("# Maksymalna liczba ticketów w tej kategorii na użytkownika")
        public int maxPerUser = 1;

        public TicketCategoryConfig() {}

        public TicketCategoryConfig(
            String id,
            String emoji,
            String displayName,
            String description,
            boolean enabled,
            int maxPerUser
        ) {
            this.id = id;
            this.emoji = emoji;
            this.displayName = displayName;
            this.description = description;
            this.enabled = enabled;
            this.maxPerUser = maxPerUser;
        }

        public String getButtonId() {
            return "ticket_" + id.toLowerCase();
        }
    }
}
