package com.eternalcode.discordapp.config;

import net.dzikoysk.cdn.entity.Description;
import net.dzikoysk.cdn.source.Resource;
import net.dzikoysk.cdn.source.Source;

import java.io.File;

public class DiscordAppConfig implements CdnConfig {

    @Description("# The token of the bot")
    public String token = System.getenv("OFFICER_TOKEN") != null ? System.getenv("OFFICER_TOKEN") : "PASTE_TOKEN_HERE";

    @Description("# The ID of the owner of the bot")
    public long topOwnerId = System.getenv("OFFICER_OWNER") != null ? Long.parseLong(System.getenv("OFFICER_OWNER")) : 852920601969950760L;

    @Override
    public Resource resource(File folder) {
        return Source.of(folder, "config.cdn");
    }
}
