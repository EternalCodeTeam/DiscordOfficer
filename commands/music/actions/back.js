module.exports = async (interaction, client) => {
    const queue = eternalPlayer.getQueue(interaction.guild.id);

    if (!queue || !queue.playing)
        return interaction.reply({
            content: `Queue is empty`,
            ephemeral: true
        });

    if (!queue.previousTracks[1])
        return interaction.reply({
            content: `Actually playing song is first in queue :C`,
            ephemeral: true
        });

    await queue.back();

    return interaction.reply({
        content: `I launched the last one song`,
        ephemeral: true
    });
};