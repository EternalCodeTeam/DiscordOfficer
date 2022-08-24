const {QueryType} = require("discord-player");
module.exports = async (modal) => {
    if(modal.customId === "play-music-modal") {
        await modal.deferReply({ ephemeral: true });

        const musicAddress = modal.getTextInputValue("music-url");

        const music = await eternalPlayer.search(musicAddress, {
            requestedBy: modal.member,
            searchEngine: QueryType.AUTO
        });

        if(!music || !music.tracks.length) {
            return modal.editReply({
                content: `Music ${musicAddress} was not found :<`,
            });
        }
        const queue = await eternalPlayer.createQueue(modal.guild, {
            metadata: modal.channel
        });

        try {
            if(!queue.connection) await queue.connect(modal.member.voice.channel);
        } catch {
            await eternalPlayer.deleteQueue(modal.guild.id);
            return modal.editReply({
                content: `First maybe u join to voice channel :|`,
            });
        }

        await modal.editReply({
            content: `Please wait... I loading your ${music.playlist ? 'playlist' : 'song'}`,
        });

        music.playlist ? queue.addTracks(music.tracks) : queue.addTrack(music.tracks.at(0));

        if(!queue.playing) {
            await queue.play();
        }

        return modal.editReply({
            content: `Your ${music.playlist ? 'playlist' : 'song'} loaded!`,
        })
    }
}