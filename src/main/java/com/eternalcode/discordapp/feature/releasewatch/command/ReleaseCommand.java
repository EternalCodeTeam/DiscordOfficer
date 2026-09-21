package com.eternalcode.discordapp.feature.releasewatch.command;

import com.eternalcode.discordapp.feature.releasewatch.ReleaseWatchService;
import com.eternalcode.discordapp.feature.releasewatch.command.child.LinkAddChild;
import com.eternalcode.discordapp.feature.releasewatch.command.child.LinkRemoveChild;
import com.eternalcode.discordapp.feature.releasewatch.command.child.SendChild;
import com.eternalcode.discordapp.feature.releasewatch.command.child.WatchAddChild;
import com.eternalcode.discordapp.feature.releasewatch.command.child.WatchListChild;
import com.eternalcode.discordapp.feature.releasewatch.command.child.WatchRemoveChild;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

public class ReleaseCommand extends SlashCommand {

    public ReleaseCommand(ReleaseWatchService releaseWatchService) {
        this.name = "release";
        this.help = "Manage GitHub release watches";

        this.children = new SlashCommand[]{
            new WatchAddChild(releaseWatchService),
            new WatchRemoveChild(releaseWatchService),
            new WatchListChild(releaseWatchService),
            new LinkAddChild(releaseWatchService),
            new LinkRemoveChild(releaseWatchService),
            new SendChild(releaseWatchService)
        };
    }

    @Override
    public void execute(SlashCommandEvent event) {
        /* This method is empty because uses children for sub-commands. */
    }
}
