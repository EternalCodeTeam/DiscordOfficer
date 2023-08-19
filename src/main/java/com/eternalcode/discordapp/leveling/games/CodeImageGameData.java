package com.eternalcode.discordapp.leveling.games;

import com.eternalcode.discordapp.config.CdnConfig;
import net.dzikoysk.cdn.source.Resource;
import net.dzikoysk.cdn.source.Source;

import java.io.File;
import java.time.Instant;

public class CodeImageGameData implements CdnConfig {
    public String code = "";

    public Instant lastUpdated = Instant.now();

    public boolean isUsed = false;

    public boolean gameActive = false;

    @Override
    public Resource resource(File folder) {
        return Source.of(folder, "games" + File.separator + "imageCode.dat");
    }
}
