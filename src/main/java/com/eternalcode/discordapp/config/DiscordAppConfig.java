package com.eternalcode.discordapp.config;

import net.dzikoysk.cdn.entity.Description;
import net.dzikoysk.cdn.source.Resource;
import net.dzikoysk.cdn.source.Source;

import java.awt.*;
import java.io.File;

public class DiscordAppConfig implements ReloadableConfig {

    @Description("# The token of the bot")
    public String token = "PASTE_TOKEN_HERE";

    @Description("# The ID of the owner of the bot")
    public long topOwnerId = 852920601969950760L;

/*    @Description("# Default embed colors")
    public Color defaultEmbedColor = Color.CYAN;
    public Color errorEmbedColor = Color.RED;*/

    @Override
    public Resource resource(File folder) {
        return Source.of(folder, "config.cdn");
    }
}
