package com.eternalcode.discordapp.config;

import net.dzikoysk.cdn.Cdn;
import net.dzikoysk.cdn.CdnFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class DiscordAppConfigManager {

    private static final Cdn CDN = CdnFactory.createYamlLike().getSettings().build();

    private final Set<CdnConfig> configs = new HashSet<>();
    private final File folder;

    public DiscordAppConfigManager(File folder) {
        this.folder = folder;
    }

    public <T extends CdnConfig> void load(T config) {
        CDN.load(config.resource(this.folder), config)
                .orThrow(RuntimeException::new);

        CDN.render(config, config.resource(this.folder))
                .orThrow(RuntimeException::new);

        this.configs.add(config);
    }
}
