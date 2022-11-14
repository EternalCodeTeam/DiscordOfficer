const { QueueRepeatMode } = require("discord-player");
module.exports = async (interaction, client) => {
    const queue = eternalPlayer.getQueue(interaction.guild.id);

    if (!queue || !queue.playing)
        return interaction.reply({
            content: `Your queue is empty`,
            ephemeral: true
        });

    return interaction.reply({
        content: queue.setRepeatMode(queue.repeatMode === 0 ? QueueRepeatMode.QUEUE : QueueRepeatMode.OFF) ? `Repeat mode was ${queue.repeatMode === 0 ? "disabled" : "enabled"}` : `Internal application error, try again later`,
        ephemeral: true
    });
};
