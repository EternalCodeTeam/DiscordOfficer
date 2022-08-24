module.exports = async (interaction, client) => {
    const queue = eternalPlayer.getQueue(interaction.guild.id);

    if(!queue || !queue.playing || !queue.tracks[0]) {
        return interaction.reply({
            content: `Queue is empty`,
            ephemeral: true
        });
    }

    return interaction.reply({
        content: queue.shuffle() ? `Shuffled queue with ${queue.tracks.length} tracks` : `Error with shuffle :cry:`,
        ephemeral: true
    })
}