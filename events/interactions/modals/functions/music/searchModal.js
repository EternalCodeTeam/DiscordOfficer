const {QueryType} = require("discord-player");
const {EmbedBuilder} = require("discord.js");
module.exports = async (modal) => {
    if(modal.customId === "search-music-modal") {
        await modal.deferReply({
            ephemeral: true
        });

        const search = await eternalPlayer.search(modal.getTextInputValue("music-title"), {
            requestedBy: modal.member,
            searchEngine: QueryType.AUTO
        });

        if(!search || !search.tracks.length)
            return modal.editReply({
                content: `Sorry, typed title not found`,
            });

        const queue = await eternalPlayer.createQueue(modal.guild.id, {
            metadata: modal.channel
        })

        const embed = new EmbedBuilder()
        embed.setColor("#FFFFFF");
        embed.setAuthor({ name: `Search result for ${modal.getTextInputValue("music-title")}`})
        embed.setDescription(`
        ${search.tracks.slice(0, 20).map((song, i) => `**#${i+1}** - ${song.title} | ${song.author}`).join("\n")}
        \nType number of result to select song, or cancel to exit from menu
    `)

        await modal.editReply({
            embeds: [embed]
        })

        const searchCollector = modal.channel.createMessageCollector({
            time: 15000,
            errors: [ "time" ],
            filter: m => m.author.id === modal.member.id
        });

        searchCollector.on("collect", async (q) => {
            if(q.content.toLowerCase() === "cancel") {
                return modal.editReply({
                    content: `Successfully canceled action`
                })
            }

            const selected = parseInt(q.content);

            if(!selected || selected <= 0 || selected > search.tracks.slice(0, 20))
                await modal.editReply({
                    content: `Provide good number, but i not found ${selected}`
                })

            searchCollector.stop();

            try {
                if(!queue.connection)
                    await queue.connect(modal.member.voice.channel);
            } catch {
                await eternalPlayer.deleteQueue(modal.guild.id)
                await modal.editReply({
                    content: `Are u on voice channel? :thinking:`
                })
            }

            await modal.editReply({
                content: `Loading selected song...`,
                ephemeral: true
            })

            queue.addTrack(search.tracks.at(selected - 1));
            if(!queue.playing)
                await queue.play();

            await q.delete();
            return modal.editReply({
                content: "Selected song successfully loaded"
            })
        })

        searchCollector.on("end", async (msg, res) => {
            if(res === "time")
                return modal.editReply({
                    content: `Time for select expired`
                })
        })
    }
}