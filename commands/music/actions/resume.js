module.exports = async (interaction, client) => {
    const queue = eternalPlayer.getQueue(interaction.guild.id);

    if (!queue || !queue.playing)
        return interaction.reply({
            content: `Queue is empty`,
            ephemeral: true
        });

    return interaction.reply({
        content: queue.setPaused(false) ? `Song: ${queue.current.title} was resumed` : `Queue is now resumed`,
        ephemeral: true
    });
};
