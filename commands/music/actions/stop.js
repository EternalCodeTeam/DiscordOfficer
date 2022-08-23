module.exports = async (interaction, client) => {
    const queue = eternalPlayer.getQueue(interaction.guild.id);

    if(!queue || !queue.playing) {
        return interaction.reply({
            content: `Queue is empty`,
            ephemeral: true
        });
    }

    queue.destroy(true)
    return interaction.reply({
        content: `Queue was stopped`,
        ephemeral: true
    })
}