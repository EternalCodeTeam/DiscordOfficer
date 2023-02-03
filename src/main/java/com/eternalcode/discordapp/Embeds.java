package com.eternalcode.discordapp;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.time.Instant;

public class Embeds {
    public EmbedBuilder error;
    public EmbedBuilder success;

    public Embeds() {
        error = new EmbedBuilder();
        error.setThumbnail("https://i.imgur.com/xWeyzSR.png");
        error.setFooter("© All icons created by Freepik - Flaticon");
        error.setColor(Color.decode("#e01947"));
        error.setTimestamp(Instant.now());

        success = new EmbedBuilder();
        success.setThumbnail("https://i.imgur.com/joHhxFq.png");
        success.setFooter("© All icons created by Freepik - Flaticon");
        success.setColor(Color.decode("#00ff77"));
        success.setTimestamp(Instant.now());
    }
}
