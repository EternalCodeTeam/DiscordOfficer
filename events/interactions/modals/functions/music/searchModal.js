const { QueryType } = require("discord-player");
const { EmbedBuilder } = require("discord.js");
module.exports = async (modal) => {
    if (modal.customId !== "search-music-modal")
        return;

    const title = modal.getTextInputValue("search-music-modal");

    const search = await eternalPlayer.search(title, {
        requestedBy: modal.member,
        searchEngine: QueryType.AUTO
    });

    if (!search || !search.tracks.length)
        return modal.reply({
            content: `Sorry, typed title not found`,
            ephemeral: true
        });

    const queue = eternalPlayer.getQueue(modal.guild.id) ? eternalPlayer.getQueue(modal.guild.id) : await eternalPlayer.createQueue(modal.guild.id, {
        metadata: modal.channel
    });

    const embed = new EmbedBuilder();
    embed.setColor("#FFFFFF");
    embed.setAuthor({ name: `Search result for ${title}` });
    embed.setDescription(`
        ${search.tracks.slice(0, 20).map((song, i) => `**#${i++}** - ${song.title} | ${song.author}`).join("\n")}
        \nType number of result to select song, or cancel to exit from menu
    `);

    await modal.reply({
        embeds: [embed],
        ephemeral: true
    });

    const searchCollector = modal.channel.createMessageCollector({
        time: 15000,
        errors: ["time"],
        filter: m => m.author.id === modal.member.id
    });

    searchCollector.on("collect", async (q) => {
        if (q.content.toLowerCase() === "cancel") {
            return modal.reply({
                content: `Successfully canceled action`,
                ephemeral: true
            });
        }

        const selected = parseInt(q.content);

        if (!selected || selected <= 0 || selected > search.tracks.slice(0, 20))
            await modal.reply({
                content: `Provide good number, but i not found ${selected}`,
                ephemeral: true
            });

        searchCollector.stop();

        try {
            if (!queue.connection)
                await queue.connect(modal.member.voice.channel);
        } catch {
            await eternalPlayer.deleteQueue(modal.guild.id);
            await modal.reply({
                content: `Are u on voice channel? :thinking:`,
                ephemeral: true
            });
        }

        await modal.reply({
            content: `Loading selected song...`,
            ephemeral: true
        });

        queue.addTrack(search.tracks[q.content - 1]);
        if (!queue.playing)
            await queue.play();

        await modal.reply({
            content: "Selected song successfully loaded",
            ephemeral: true
        });
    });

    searchCollector.on("end", async (msg, res) => {
        if (res === "time")
            await modal.reply({
                content: `Time for select expired`,
                ephemeral: true
            });
    });
};