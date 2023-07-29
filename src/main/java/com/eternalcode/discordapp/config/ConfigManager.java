package com.eternalcode.discordapp.config;

import com.eternalcode.discordapp.composer.InstantComposer;
import net.dzikoysk.cdn.Cdn;
import net.dzikoysk.cdn.CdnFactory;
import net.dzikoysk.cdn.reflect.Visibility;

import java.io.File;
import java.time.Instant;

public class ConfigManager {

    private static final Cdn CDN = CdnFactory
            .createYamlLike()
            .getSettings()
            .withComposer(Instant.class, new InstantComposer())
            .build();

    private final File folder;

    public ConfigManager(String directory) {
        this.folder = new File(directory);
    }

    public <T extends CdnConfig> T load(T dataFile) {
        CDN.load(dataFile.resource(this.folder), dataFile)
                .orThrow(RuntimeException::new);

        CDN.render(dataFile, dataFile.resource(this.folder))
                .orThrow(RuntimeException::new);

        return dataFile;
    }

    public <T extends CdnConfig> void save(T config) {
        CDN.render(config, config.resource(this.folder))
                .orThrow(RuntimeException::new);
    }
}
