package com.eternalcode.discordapp.ticket;

import com.eternalcode.discordapp.config.AppConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

import java.awt.*;

public class TicketButtonController extends ListenerAdapter {

    AppConfig appConfig;

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("create_ticket")) {
            long ticketCategoryId = appConfig.ticketCategoryId;
            Category ticketCategory = event.getGuild().getCategoryById(ticketCategoryId);
            Member member = event.getMember();
            assert member != null;

            if (event.getGuild().getTextChannelsByName(member.getId(), true).isEmpty()) {
                event.reply("Posiadasz juz otwarty ticket bratku essa");
            }

            ChannelAction<TextChannel> ticketAction = ticketCategory.createTextChannel(member.getId());
            TextChannel ticket = ticketAction.complete();

            EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Twój ticket został utworzony")
                .setColor(Color.GREEN)
                .setDescription("Tutaj możesz zgłaszać swoje problemy.");

            ticket.sendMessageEmbeds(embedBuilder.build())
                .queue();

            event.reply("Twój ticket został utworzony.").setEphemeral(true).queue();
        }
    }
}
