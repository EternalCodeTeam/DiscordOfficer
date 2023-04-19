package com.eternalcode.discordapp.data;

import com.eternalcode.discordapp.config.CdnConfig;
import net.dzikoysk.cdn.Cdn;
import net.dzikoysk.cdn.CdnFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class DataManager {

    private static final Cdn CDN = CdnFactory.createYamlLike().getSettings().build();

    private final Set<CdnConfig> dataFiles = new HashSet<>();
    private final File folder;

    public DataManager(File folder) {
        this.folder = folder;
    }

    public <T extends CdnConfig> void load(T dataFile) {
        CDN.load(dataFile.resource(this.folder), dataFile)
                .orThrow(RuntimeException::new);

        CDN.render(dataFile, dataFile.resource(this.folder))
                .orThrow(RuntimeException::new);

        this.dataFiles.add(dataFile);
    }

    public <T extends CdnConfig> void save(T config) {
        CDN.render(config, config.resource(this.folder))
                .orThrow(RuntimeException::new);
    }
}
