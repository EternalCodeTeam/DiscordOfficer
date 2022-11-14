module.exports = async (interaction, client) => {
    const queue = eternalPlayer.getQueue(interaction.guild.id);

    if (!queue || !queue.playing || queue.tracks.length < 1)
        return interaction.reply({
            content: `Queue is empty`,
            ephemeral: true
        });

    await queue.clear();
    return interaction.reply({
        content: `Queue was cleaned`,
        ephemeral: true
    });
};
