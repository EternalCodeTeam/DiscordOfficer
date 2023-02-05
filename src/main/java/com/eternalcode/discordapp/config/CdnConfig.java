package com.eternalcode.discordapp.config;

import net.dzikoysk.cdn.source.Resource;

import java.io.File;

public interface CdnConfig {

    Resource resource(File folder);

}
