package com.eternalcode.discordapp.data;

import com.eternalcode.discordapp.composer.InstantComposer;
import com.eternalcode.discordapp.config.CdnConfig;
import net.dzikoysk.cdn.Cdn;
import net.dzikoysk.cdn.CdnFactory;

import java.io.File;
import java.time.Instant;

public class YamlFilesManager {

    private static final Cdn CDN = CdnFactory
            .createYamlLike()
            .getSettings()
            .withComposer(Instant.class, new InstantComposer())
            .build();

    private final File folder;

    public YamlFilesManager(String directory) {
        this.folder = new File(directory);
    }

    public <T extends CdnConfig> void load(T dataFile) {
        CDN.load(dataFile.resource(this.folder), dataFile)
                .orThrow(RuntimeException::new);

        CDN.render(dataFile, dataFile.resource(this.folder))
                .orThrow(RuntimeException::new);
    }

    public <T extends CdnConfig> void save(T config) {
        CDN.render(config, config.resource(this.folder))
                .orThrow(RuntimeException::new);
    }
}
