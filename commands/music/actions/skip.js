module.exports = async (interaction, client) => {
    const queue = eternalPlayer.getQueue(interaction.guild.id);

    if (!queue || !queue.playing) {
        return interaction.reply({
            content: `Queue is empty`,
            ephemeral: true
        });
    }

    return interaction.reply({
        content: queue.skip() ? `Skipped song` : `Error with skipping :cry:`,
        ephemeral: true
    });
};