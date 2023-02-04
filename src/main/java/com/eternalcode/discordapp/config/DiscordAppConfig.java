package com.eternalcode.discordapp.config;

import net.dzikoysk.cdn.entity.Description;
import net.dzikoysk.cdn.source.Resource;
import net.dzikoysk.cdn.source.Source;

import java.io.File;

public class DiscordAppConfig implements CdnConfig {

    @Description("# The token of the bot")
    public String token = "PASTE_TOKEN_HERE";

    @Description("# The ID of the owner of the bot")
    public long topOwnerId = 852920601969950760L;

    @Override
    public Resource resource(File folder) {
        return Source.of(folder, "config.cdn");
    }
}
